package dev.vkazulkin;

import java.util.List;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import dev.vkazulkin.conference.ConferenceSearchTool;

@SpringBootApplication
public class SpringMcpConferenceSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringMcpConferenceSearchApplication.class, args);
	}

	@Bean
	public List<ToolCallback> conferenceSearchTools(ConferenceSearchTool conferenceSearchTool) {
		return List.of(ToolCallbacks.from(conferenceSearchTool));
	}
}
