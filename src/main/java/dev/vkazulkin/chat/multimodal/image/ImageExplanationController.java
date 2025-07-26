package dev.vkazulkin.chat.multimodal.image;

import java.io.IOException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageExplanationController {

	private final ChatClient chatClient;
	
	@Value("classpath:/images/vadym-kazulkin-speaking.jpg")
	Resource sampleImage;

	public ImageExplanationController(ChatClient.Builder builder) {
		
		var options = ToolCallingChatOptions.builder()
				.model("amazon.nova-lite-v1:0")
				//.model("amazon.nova-pro-v1:0")
				//.model("amazon.nova-premier-v1:0")
				// .temperature(0.6)
				.maxTokens(100).build();
		this.chatClient = builder.defaultOptions(options).build();
	}

	@GetMapping("/image-to-text")
	public String image() throws IOException {
		return chatClient.prompt().user(u -> u.text("Can you explain me what you see in the provided image?")
				.media(MimeTypeUtils.IMAGE_JPEG, sampleImage)).call().content();
	}

}
