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
	
	private static final String IAM_ROLE_ARN="{IAM_ARN_ROLE}";
	private static final String CONTAINER_URI="{AWS_ACCOUNT_ID}.dkr.ecr.{AWS_REGION}.amazonaws.com/{ECR_REPO}"; 
	
	private static final String CREATE_AGENT_RUNTIME_CONTAINER_URI=CONTAINER_URI+":v1";  //change to your version schema
	private static final String UPDATE_AGENT_RUNTIME_CONTAINER_URI=CONTAINER_URI+":v14"; //change to your version schema
	
	private static final String AGENT_RUNTIME_NAME="{AGENT_RUNTIME_NAME}";
	private static final String AGENT_RUNTIME_ID="{AGENT_RUNTIME_ID}";
	
	private static final BedrockAgentCoreControlClient bedrockAgentCoreControlClient = 
			 BedrockAgentCoreControlClient.builder().region(Region.US_EAST_1).build();
	
	private static void createAgentRuntime() {
		var request= CreateAgentRuntimeRequest.builder()
				 .agentRuntimeName(AGENT_RUNTIME_NAME)
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
				 .agentRuntimeId(AGENT_RUNTIME_ID)
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
