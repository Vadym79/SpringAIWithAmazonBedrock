package dev.vkazulkin.agent.sdk;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.AgentArtifact;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.ContainerConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.CreateAgentRuntimeRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.CreateAgentRuntimeResponse;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.UpdateAgentRuntimeRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.UpdateAgentRuntimeResponse;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.NetworkConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.NetworkMode;


public class DeployRuntimeAgent {

	
	private static final String IAM_ROLE_ARN="{IAM_ARN_ROLE}";
	private static final String CONTAINER_URI="{AWS_ACCOUNT_ID}.dkr.ecr.{AWS_REGION}.amazonaws.com/{ECR_REPO}"; 
	
	private static final String CREATE_AGENT_RUNTIME_CONTAINER_URI=CONTAINER_URI+":v1";  //change to your version schema
	private static final String UPDATE_AGENT_RUNTIME_CONTAINER_URI=CONTAINER_URI+":v14"; //change to your version schema
	
	private static final BedrockAgentCoreControlClient bedrockAgentCoreControlClient = BedrockAgentCoreControlClient.builder().region(Region.US_EAST_1)
			.build();
	
	private static void createAgentRuntime() {
		CreateAgentRuntimeRequest request= CreateAgentRuntimeRequest.builder()
				 .agentRuntimeName("agentcore_runtime_spring_ai_demo")
				 .roleArn(IAM_ROLE_ARN)
				 .networkConfiguration(NetworkConfiguration.builder().networkMode(NetworkMode.PUBLIC).build())
				 .agentRuntimeArtifact(AgentArtifact.fromContainerConfiguration(
		          ContainerConfiguration.builder().containerUri(CREATE_AGENT_RUNTIME_CONTAINER_URI).build()))
				 .build();
		CreateAgentRuntimeResponse response= bedrockAgentCoreControlClient.createAgentRuntime(request);
		System.out.println("Create Agent Runtime response: "+response);
	}
	
	private static void updateAgentRuntime() {
		UpdateAgentRuntimeRequest request= UpdateAgentRuntimeRequest.builder()
				 .agentRuntimeId("agentcore_runtime_spring_ai_demo-tD7f1W6RGi")
				 .roleArn(IAM_ROLE_ARN)
				 .networkConfiguration(NetworkConfiguration.builder().networkMode(NetworkMode.PUBLIC).build())
				  .agentRuntimeArtifact(AgentArtifact.fromContainerConfiguration(
		          ContainerConfiguration.builder().containerUri(UPDATE_AGENT_RUNTIME_CONTAINER_URI).build()))
				 .build();
		UpdateAgentRuntimeResponse response= bedrockAgentCoreControlClient.updateAgentRuntime(request);
		System.out.println("Update Agent Runtime response: "+response);
	}

	public static void main(String[] args) throws Exception {

		//createAgentRuntime();
		updateAgentRuntime();
	}

}
