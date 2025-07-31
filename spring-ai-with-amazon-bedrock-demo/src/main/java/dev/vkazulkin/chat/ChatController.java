package dev.vkazulkin.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class ChatController {

	private final ChatClient chatClient;

	public ChatController(ChatClient.Builder builder) {

		var options = ToolCallingChatOptions.builder()
				// .model("amazon.nova-lite-v1:0")
				// .temperature(0.6)
				.maxTokens(50).build();
		this.chatClient = builder.defaultOptions(options).build();
	}

	/**
	 * A basic example of how to use the chat client to pass a message and call the
	 * LLM
	 * 
	 * @param message
	 * @return
	 */
	@GetMapping("/info")
	public String info(@RequestParam(value = "message", defaultValue = "Give me some general information about AWS Serverless") String message) {
		return this.chatClient.prompt()
			   .user(message)
			   .call()
			   .content(); // short for getResult().getOutput().getContent();
	}

	/**
	 * Take in a topic as a request parameter and use that param in the user message
	 * 
	 * @param topic
	 * @return
	 */
	@GetMapping("/info-by-topic")
	public String infoByTopic(@RequestParam String topic) {
		return this.chatClient.prompt()
				.user(u -> u.text("Give me some general information about {topic}")
				.param("topic", topic))
				.call()
				.content();
	}

	/**
	 * Take in a topic as a request parameter and use that param in the user message
	 * Info is delivered in non blocking manner 
	 * 
	 * @param topic
	 * @return
	 */
	@GetMapping("/stream-info-by-topic")
	public Flux<String> streamInfoByTopic(@RequestParam String topic) {
		return this.chatClient.prompt()
				.user(u -> u.text("Give me some general information about {topic}")
				.param("topic", topic))
				.stream()
				.content();
	}
}