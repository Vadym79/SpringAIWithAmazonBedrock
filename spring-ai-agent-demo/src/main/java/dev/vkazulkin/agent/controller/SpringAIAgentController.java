package dev.vkazulkin.agent.controller;

import io.modelcontextprotocol.client.McpAsyncClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

@RestController
public class SpringAIAgentController {

	private static final Logger logger = LoggerFactory.getLogger(SpringAIAgentController.class);
	
	private final ChatClient chatClient;
	
	@Autowired
	private ObjectMapper mapper;
  
	@Value("${cognito.user.pool.name}")
	private String USER_POOL_NAME; 
	
	@Value("${cognito.user.pool.client.name}")
	private String USER_POOL_CLIENT_NAME;
	
	@Value("${cognito.auth.token.resource.server.id}")
	private String RESOURCE_SERVER_ID;
	
	@Value("${amazon.bedrock.agentcore.gateway.url}")
	private String AGENTCORE_GATEWAY_URL;

	private static final CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
			.region(Region.US_EAST_1).build();

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

	/** agrentcore runtime ping endpoint
	 * 
	 * @return health status
	 */
	@GetMapping("/ping")
	public String ping() {
		return "{\"status\": \"healthy\"}";
	}

	
	/** returns synchronous agent answer
	 * 
	 * @param prompt - prompt 
	 * @return agent answer
	 */
	@PostMapping(value = "/invocationss", consumes = { "*/*" })
	public String invoke(@RequestBody String prompt) {
		logger.info("invocations endpoint with prompt: " + prompt);
		String token = getAuthToken();
		try (McpSyncClient client = McpClient.sync(getMcpClientTransport(token)).build()) {
			client.initialize();
			McpSchema.ListToolsResult toolsResult = client.listTools();

			for (McpSchema.Tool tool : toolsResult.tools()) {
				logger.info("tool found " + tool);
			}

			SyncMcpToolCallbackProvider syncMcpToolCallbackProvider = new SyncMcpToolCallbackProvider(client);

			return this.chatClient.prompt().user(prompt).toolCallbacks(syncMcpToolCallbackProvider.getToolCallbacks())
					.call().content();
		}
	}

	/**
	 * public post agentcore runtime endpoint to receive agent requests
	 * @param prompt - prompt
	 * @return asynchronous agent answer
	 */
	@PostMapping(value = "/invocations", consumes = { "*/*" })
	public Flux<String> invocations(@RequestBody String prompt) {
		logger.info("invocations endpoint with prompt: " + prompt);

		String token = getAuthToken();
		McpAsyncClient client = McpClient.async(getMcpClientTransport(token)).build();
		client.initialize();
		Mono<McpSchema.ListToolsResult> toolsResult = client.listTools();

		for (McpSchema.Tool tool : toolsResult.block().tools()) {
			logger.info("tool found " + tool);
		}

		AsyncMcpToolCallbackProvider syncMcpToolCallbackProvider = new AsyncMcpToolCallbackProvider(client);

		Flux<String> content = this.chatClient.prompt().user(prompt)
				.toolCallbacks(syncMcpToolCallbackProvider.getToolCallbacks()).stream().content();

		client.close();
		return content;
	}

	/** returns streamable http mcp client transport
	 * 
	 * @param token  -bearer authorization token
	 * @return streamable http mcp client transport
	 */
	private McpClientTransport getMcpClientTransport(String token) {
		//String token = "eyJraWQiOiJ4Ynp0ZW5nVnBmdXVhRitzMVhmXC94OHRLczc4TFpsejEwSzJ6eXArOEZXYz0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI2Zjk5dXZnMDNkazNrbnVyc3ZzMWtvNW9kZSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoic2FtcGxlLWFnZW50Y29yZS1nYXRld2F5LWlkXC9nYXRld2F5OndyaXRlIHNhbXBsZS1hZ2VudGNvcmUtZ2F0ZXdheS1pZFwvZ2F0ZXdheTpyZWFkIiwiYXV0aF90aW1lIjoxNzU3MDY5ODY3LCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb21cL3VzLWVhc3QtMV84dm5DbFEwbUQiLCJleHAiOjE3NTcwNzM0NjcsImlhdCI6MTc1NzA2OTg2NywidmVyc2lvbiI6MiwianRpIjoiOGM1ODg2YTctZDhiZS00NmRkLTgyZTItMjFjMTkyYzA2Zjg4IiwiY2xpZW50X2lkIjoiNmY5OXV2ZzAzZGsza251cnN2czFrbzVvZGUifQ.XM1dGwUZbUQnWMYTjcINGGecUHTO23euh-iSPfwm7-vN5fNmPp40L34s-yE2_ESU1qvG8_k6ghAWWHrowLfSRtynDHUNJ8hbdBv5Tn_Z4VWiRyDD9DsGfzepjOmGUuo3xP2GU-HIRtVEJhQLej7CjAs4ZX39XHAYp1PNigUSTOE-tkCQ5HPSeoZCvLeEVQztq1g-QHHq2cf0EXYsGd5nX6LVK9wjKSy0D89tkbaDaKB2DgiZyEgGAw60_-WZ3O8pVxw1KGtlz2AwPW7RmG9XWlf6DvfhOwxZdPDWXnzYnLHvtccBzFd2bWzhINfGImtM7q-sxBepeRTSOh73diuODA";
		String headerValue = "Bearer " + token;
		WebClient.Builder webClientBuilder = WebClient.builder().defaultHeader("Authorization", headerValue);
		return WebClientStreamableHttpTransport.builder(webClientBuilder).endpoint(AGENTCORE_GATEWAY_URL).build();
	}

	/**
	 * returns authorization token required by the mcp client
	 * @return authorization token
	 */
	private String getAuthToken() {

		UserPoolDescriptionType userPool = getUserPool();
		logger.info("user pool " + userPool);
		UserPoolClientDescription userPoolClient = getUserPoolClient(userPool);
		logger.info("user pool " + userPoolClient);

		UserPoolClientType userPoolClientType = describeUserPoolClient(userPoolClient);

		logger.info("user pool client type " + userPoolClientType);
		String userPoolId = userPool.id();
		userPoolId = userPoolId.replace("_", "");
		String url = "https://" + userPoolId + ".auth." + Region.US_EAST_1.id() + ".amazoncognito.com/oauth2/token";
		logger.info("url: " + url);

		String SCOPE_STRING = RESOURCE_SERVER_ID + "/gateway:read " + RESOURCE_SERVER_ID
				+ "/gateway:write";

		String entity = "grant_type=client_credentials&" + "client_id=" + userPoolClientType.clientId() + "&"
				+ "client_secret=" + userPoolClientType.clientSecret() + "&" + "scope=" + SCOPE_STRING;

		logger.info("entity " + entity);
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			ClassicHttpRequest httpPost = ClassicRequestBuilder.post(url)
					.setHeader("Content-Type", "application/x-www-form-urlencoded").setEntity(entity).build();

			CloseableHttpResponse response = httpClient.execute(httpPost);
			InputStream inputStream = response.getEntity().getContent();
			String responseString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			logger.info("response: " + responseString);

			Map<String, Object> responseMap = mapper.readValue(responseString,
					new TypeReference<Map<String, Object>>() {
					});
			String token = (String) responseMap.get("access_token");
			logger.info("token : " + token);
			return token;
		} catch (IOException e) {
		}
		return null;
	}

	/** returns cognito user pool with specific user name
	 * 
	 * @return cognito user pool with specific user name
	 */
	private UserPoolDescriptionType getUserPool() {
		try {
			ListUserPoolsRequest request = ListUserPoolsRequest.builder().maxResults(10).build();

			ListUserPoolsResponse response = cognitoClient.listUserPools(request);
			for (UserPoolDescriptionType userPool : response.userPools()) {
				logger.info("User pool " + userPool.name() + ", User ID " + userPool.id());
				if (userPool.name().equals(USER_POOL_NAME)) {
					return userPool;
				}
			}

		} catch (CognitoIdentityProviderException e) {

		}
		return null;
	}

	/**returns cognito user pool client for the given cognito user pool
	 * 
	 * @param userPool - cognito user pool 
	 * @return cognito user pool client for the given cognito user pool
	 */
	private UserPoolClientDescription getUserPoolClient(UserPoolDescriptionType userPool) {
		try {
			ListUserPoolClientsRequest request = ListUserPoolClientsRequest.builder().userPoolId(userPool.id())
					.maxResults(10).build();

			ListUserPoolClientsResponse response = cognitoClient.listUserPoolClients(request);
			for (UserPoolClientDescription userPoolClient : response.userPoolClients()) {
				logger.info("User Pool Client Name " + userPoolClient.clientName() + ", User Pool Client ID "
						+ userPoolClient.clientId());
				if (userPoolClient.clientName().equals(USER_POOL_CLIENT_NAME)) {
					return userPoolClient;
				}
			}
		} catch (CognitoIdentityProviderException e) {

		}
		return null;
	}

	/** returns cognito user pool client type for the given cognito user pool client
	 * 
	 * @param userPoolClient- cognito user pool client
	 * @return cognito user pool client type for the given cognito user pool client
	 */
	private static UserPoolClientType describeUserPoolClient(UserPoolClientDescription userPoolClient) {
		DescribeUserPoolClientRequest request = DescribeUserPoolClientRequest.builder()
				.userPoolId(userPoolClient.userPoolId()).clientId(userPoolClient.clientId()).build();
		DescribeUserPoolClientResponse response = cognitoClient.describeUserPoolClient(request);
		Optional<UserPoolClientType> optionalType = response.getValueForField("UserPoolClient",
				UserPoolClientType.class);
		return optionalType.get();
	}
}