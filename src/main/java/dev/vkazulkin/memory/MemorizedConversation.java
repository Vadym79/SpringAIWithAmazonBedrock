package dev.vkazulkin.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemorizedConversation {

	private final ChatClient chatClient;

	public MemorizedConversation(ChatClient.Builder builder, ChatMemory chatMemory) {
		this.chatClient = builder
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
	}

	@GetMapping("/memorized-chat")
	public String home(@RequestParam String message) {
		return this.chatClient.prompt()
				.user(message)
				.call()
				.content();
	}

}
