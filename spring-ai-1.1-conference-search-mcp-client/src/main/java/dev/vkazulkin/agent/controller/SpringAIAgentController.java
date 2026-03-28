package dev.vkazulkin.agent.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import reactor.core.publisher.Flux;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;


@RestController
public class SpringAIAgentController {

	private static final Logger logger = LoggerFactory.getLogger(SpringAIAgentController.class);

	private final ChatClient chatClient;

	@Value("${cognito.user.pool.name}")
	private String USER_POOL_NAME;

	@Value("${cognito.user.pool.client.name}")
	private String USER_POOL_CLIENT_NAME;

	@Value("${amazon.bedrock.agentcore.runtime.id}")
	private String AGENTCORE_RUNTIME_ID;

	@Value("${aws.region}")
	private String AWS_REGION;

	@Value("${aws.account.id}")
	private String AWS_ACCOUNT_ID;
	
	@Value("${secrets.manager.secret.name}")
	private String SECRET_NAME;

	private static final CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
			.region(Region.US_EAST_1).build();

	// Create a Secrets Manager client
	private static final SecretsManagerClient client = SecretsManagerClient.builder().region(Region.US_EAST_1).build();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public SpringAIAgentController(ChatClient.Builder builder, ChatMemory chatMemory) {
		var options = ToolCallingChatOptions.builder().model("amazon.nova-lite-v1:0")
				// .model("amazon.nova-pro-v1:0")
				// .model("anthropic.claude-3-5-sonnet-20240620-v1:0")
				.maxTokens(2000).build();

		this.chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.defaultOptions(options)
				// .defaultSystem(SYSTEM_PROMPT)
				.build();

	}

	/**
	 * agrentcore runtime ping endpoint
	 * 
	 * @return health status
	 */
	@GetMapping("/ping")
	public String ping() {
		return "{\"status\": \"healthy\"}";
	}

	/**
	 * GET method which has a prompt as an input parameter and outputs the agent response synchronously
	 * 
	 * @param prompt - prompt
	 * @return agent answer
	 */
	@GetMapping(value = "/conference-search-sync", consumes = "text/plain")
	public String conferenceSearchSync(@RequestParam String prompt) {
		logger.info("invocations endpoint with prompt: " + prompt);
		String token = getAuthToken();
		try (var client = McpClient.sync(getMcpClientTransport(token)).build()) {
			client.initialize();
			var toolsResult = client.listTools();
			for (var tool : toolsResult.tools()) {
				logger.info("tool found " + tool);
			}       
			var syncMcpToolCallbackProvider = SyncMcpToolCallbackProvider.builder().mcpClients(client).build();
			return this.chatClient.prompt().user(prompt).toolCallbacks(syncMcpToolCallbackProvider.getToolCallbacks())
					.call().content();
		}
	}

	/**
	 *  GET method which has a prompt as an input parameter and outputs the agent response asynchronously
	 * 
	 * @param prompt - prompt
	 * @return asynchronous agent answer
	 */
	@GetMapping(value = "/conference-search", consumes = "text/plain")
	public Flux<String> conferenceSearch(@RequestParam String prompt) {
		logger.info("invocations endpoint with prompt: " + prompt);

		String token = getAuthToken();
		if (token == null) {
			throw new RuntimeException("can't obtain authorization token");
		}
		var client = McpClient.async(getMcpClientTransport(token)).build();
		client.initialize();
		var toolsResult = client.listTools(); 
		for (var tool : toolsResult.block().tools()) { 
			logger.info("tool found " + tool); 
		}
		 
		var asyncMcpToolCallbackProvider = AsyncMcpToolCallbackProvider.builder().mcpClients(client)
				/*
				 * .toolFilter(new McpToolFilter() {
				 * 
				 * @Override public boolean test(McpConnectionInfo info, Tool tool) { return
				 * tool.name().toLowerCase().contains("order"); } })
				 */
				.build();

		var content = this.chatClient.prompt().user(prompt)
				.toolCallbacks(asyncMcpToolCallbackProvider.getToolCallbacks()).stream().content();

		// client.close();
		return content;
	}

	/**
	 * returns streamable http mcp client transport
	 * 
	 * @param token -bearer authorization token
	 * @return streamable http mcp client transport
	 */
	private McpClientTransport getMcpClientTransport(String token) {
		String AGENTCORE_RUNTIME_MCP_URL = "https://bedrock-agentcore." + AWS_REGION + ".amazonaws.com/runtimes/"
				+ AGENTCORE_RUNTIME_ID + "/invocations?qualifier=DEFAULT&accountId=" + AWS_ACCOUNT_ID;

		logger.info("MCP URL: " + AGENTCORE_RUNTIME_MCP_URL);
		String headerValue = "Bearer " + token;
		var webClientBuilder = WebClient.builder()
				.defaultHeader("Authorization", headerValue)
				.defaultHeader("accept","application/json, text/event-stream")
		        .defaultHeader("Content-Type","application/json");
		return WebClientStreamableHttpTransport
				.builder(webClientBuilder)
				.endpoint(AGENTCORE_RUNTIME_MCP_URL).build();
	}

	/**
	 * returns authorization token required by the mcp client
	 * 
	 * @return authorization token
	 */
	private String getAuthToken() {
		var userPool = getUserPool();
		logger.info("user pool " + userPool);
		if (userPool == null) {
			throw new RuntimeException("cognito user pool with the name " + USER_POOL_NAME + " is not found");
		}

		var userPoolClient = getUserPoolClient(userPool);

		logger.info("user pool client " + userPoolClient);

		if (userPoolClient == null) {
			throw new RuntimeException(
					"cognito user pool client with the name " + USER_POOL_CLIENT_NAME + " is not found");
		}
		String authToken = getAuthToken(userPoolClient.clientId());
		logger.info("auth token " + authToken);

		return authToken;
	}

	/**
	 * returns authentication token
	 * 
	 * @param clientId - pool client id
	 * @return authentication token
	 */
	private String getAuthToken(String clientId) {
		try {
			var credentials=getCredentials();
			var initiateAuthRequest = InitiateAuthRequest
					.builder().authFlow(AuthFlowType.USER_PASSWORD_AUTH).clientId(clientId)
					.authParameters(Map
							.of("USERNAME", credentials.username(), "PASSWORD", credentials.password))
					.build();
			var initiateAuthResponse = cognitoClient.initiateAuth(initiateAuthRequest);
			return initiateAuthResponse.authenticationResult().accessToken();
		} catch (Exception e) {
			logger.error("can't retrieve auth token ", e);
			return null;
		}

	}

	/**
	 * returns credentials for getting the auth token from cognito
	 * 
	 * @return credentials for getting the auth token from cognito
	 */
	private Credentials getCredentials() throws Exception {
		var getSecretValueRequest = GetSecretValueRequest.builder().secretId(SECRET_NAME).build();
		var getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

		return objectMapper.readValue(getSecretValueResponse.secretString(), Credentials.class);
	}

	/**
	 * returns cognito user pool with specific user name
	 * 
	 * @return cognito user pool with specific user name
	 */
	private UserPoolDescriptionType getUserPool() {
		try {
			var request = ListUserPoolsRequest.builder().maxResults(10).build();
			var response = cognitoClient.listUserPools(request);
			for (var userPool : response.userPools()) {
				logger.info("User pool " + userPool.name() + ", User ID " + userPool.id());
				if (userPool.name().equals(USER_POOL_NAME)) {
					return userPool;
				}
			}

		} catch (CognitoIdentityProviderException e) {
			logger.error("error occured with the message: ", e.getMessage());
		}
		return null;
	}

	/**
	 * returns cognito user pool client for the given cognito user pool
	 * 
	 * @param userPool - cognito user pool
	 * @return cognito user pool client for the given cognito user pool
	 */
	private UserPoolClientDescription getUserPoolClient(UserPoolDescriptionType userPool) {
		try {
			var request = ListUserPoolClientsRequest.builder().userPoolId(userPool.id()).maxResults(10).build();

			var response = cognitoClient.listUserPoolClients(request);
			for (var userPoolClient : response.userPoolClients()) {
				logger.info("User Pool Client Name " + userPoolClient.clientName() + ", User Pool Client ID "
						+ userPoolClient.clientId());
				if (userPoolClient.clientName().equals(USER_POOL_CLIENT_NAME)) {
					return userPoolClient;
				}
			}
		} catch (CognitoIdentityProviderException e) {
			logger.error("error occured with the message: ", e.getMessage());
		}
		return null;
	}

	private record Credentials(String username, String password) {
	}
}