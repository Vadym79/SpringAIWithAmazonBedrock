package dev.vkazulkin.agent.sdk;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.AgentArtifact;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.ContainerConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.CreateAgentRuntimeRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.UpdateAgentRuntimeRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.NetworkConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.NetworkMode;


public class DeployRuntimeAgent {

	
	private static final String IAM_ROLE_ARN="arn:aws:iam::265634257610:role/service-role/AmazonBedrockAgentCoreRuntimeDefaultServiceRole-q8xp1";
	private static final String CONTAINER_URI="265634257610.dkr.ecr.us-east-1.amazonaws.com/agentcore-runtime-spring-ai-demo"; 
	
	private static final String CREATE_AGENT_RUNTIME_CONTAINER_URI=CONTAINER_URI+":v1";
	private static final String UPDATE_AGENT_RUNTIME_CONTAINER_URI=CONTAINER_URI+":v14";
	
	private static final BedrockAgentCoreControlClient bedrockAgentCoreControlClient = BedrockAgentCoreControlClient.builder().region(Region.US_EAST_1)
			.build();
	
	private static void createAgentRuntime() {
		var request= CreateAgentRuntimeRequest.builder()
				 .agentRuntimeName("agentcore_runtime_spring_ai_demo")
				 .roleArn(IAM_ROLE_ARN)
				 .networkConfiguration(NetworkConfiguration.builder().networkMode(NetworkMode.PUBLIC).build())
				 .agentRuntimeArtifact(AgentArtifact.fromContainerConfiguration(
		          ContainerConfiguration.builder().containerUri(CREATE_AGENT_RUNTIME_CONTAINER_URI).build()))
				 .build();
		var response= bedrockAgentCoreControlClient.createAgentRuntime(request);
		System.out.println("Create Agent Runtime response: "+response);
	}
	
	private static void updateAgentRuntime() {
		var request= UpdateAgentRuntimeRequest.builder()
				 .agentRuntimeId("agentcore_runtime_spring_ai_demo-tD7f1W6RGi")
				 .roleArn(IAM_ROLE_ARN)
				 .networkConfiguration(NetworkConfiguration.builder().networkMode(NetworkMode.PUBLIC).build())
				  .agentRuntimeArtifact(AgentArtifact.fromContainerConfiguration(
		          ContainerConfiguration.builder().containerUri(UPDATE_AGENT_RUNTIME_CONTAINER_URI).build()))
				 .build();
		var response= bedrockAgentCoreControlClient.updateAgentRuntime(request);
		System.out.println("Update Agent Runtime response: "+response);
	}

	public static void main(String[] args) throws Exception {

		//createAgentRuntime();
		updateAgentRuntime();
	}

}
