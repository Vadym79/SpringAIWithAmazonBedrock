package dev.vkazulkin.agent.sdk;

import java.nio.charset.StandardCharsets;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeRequest;


public class InvokeRuntimeAgent {

	private static final String AGENT_RUNTIME_ARN="{AGENTCORE_RUNTIME_ARN}";
	
	public static void main(String[] args) throws Exception {


		String payload = "{\"prompt\":\"Give me an overview of the order with the id equals 100\"}";
		//String payload = "{\"prompt\":\"Give me an overview of the order with the id equals 200\"}";
		//String payload = "{\"prompt\":\"My name is Vadym. Today is a sunny weather\"}";
		//String payload = "{\"prompt\":\" Can you tell me my name, today's weather and summarize all orders you have information about \"}";
		//String payload = "{\"prompt\":\"I previously told you my name and today's weather. Can you please provide them to me\"}";
		//String payload = "{\"prompt\":\" Can you tell me my name, today's weather? \"}";
		//String payload = "{\"prompt\":\" Please provide me with the summary of all orders you already have information about \"}";
		
		BedrockAgentCoreClient bedrockAgentCoreClient = BedrockAgentCoreClient.builder().region(Region.US_EAST_1)
				.build();

		var invokeAgentRuntimeRequest = InvokeAgentRuntimeRequest.builder()
				.agentRuntimeArn(AGENT_RUNTIME_ARN)
				.qualifier("DEFAULT").contentType("application/json").payload(SdkBytes.fromUtf8String(payload)).build();
		try (var responseStream = bedrockAgentCoreClient
				.invokeAgentRuntime(invokeAgentRuntimeRequest)) {
			var text = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);

			System.out.println(text);
		}

	}

}
