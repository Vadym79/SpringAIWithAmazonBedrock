package dev.vkazulkin.conference;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.vkazulkin.tool.zdt.ZonedDateTimeTool;
import dev.vkazulkin.tool.conference.ConferenceSearchTool;
import reactor.core.publisher.Flux;

@RestController
public class ConferenceSearchContoller {
	
    private final ChatClient chatClient;
    private final ZonedDateTimeTool zonedDateTimeTool;
    private final ConferenceSearchTool conferenceSearchTool;
    
    private static final String SYSTEM_PROMPT="""
    		You are only able to answer questions about upcoming technical conferences. 
    		If the provided search term {topic} is not a technical term or is not connected to the conference for the software development, 
    		please respond in the friendly manner that you're not able to provide this information.
    		""";
    
    public ConferenceSearchContoller(ChatClient.Builder builder, ChatMemory chatMemory, 
    		ZonedDateTimeTool zonedDateTimeTool, ConferenceSearchTool conferenceSearchTool) {
		var options = ToolCallingChatOptions.builder()
				 .model("amazon.nova-lite-v1:0")
				 // .model("amazon.nova-pro-v1:0")
				  //.model("anthropic.claude-3-5-sonnet-20240620-v1:0")
				 .maxTokens(2000).build();
		
		this.chatClient = builder
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.defaultOptions(options)
				//.defaultSystem(SYSTEM_PROMPT)
				.build();
        this.zonedDateTimeTool=zonedDateTimeTool;
        this.conferenceSearchTool=conferenceSearchTool;
    }

    @GetMapping("/conference-search-by-topic")
    public Flux<String> conferenceSearchbyTopic(@RequestParam(value = "topic", defaultValue = "Java") String topic,
    		@RequestParam(value = "number_of_months", defaultValue = "6") String numOfMonths) {
    	
    	final String USER_PROMPT= """
    			
    			1. Provide me with the best suggestions to apply for the talk for the {topic} conferences.
    			2. The provided conference start date attribute should be within the next {number_of_months} months. 
    			3. Please include the following conference info in the response only: name, topics, homepage, start and end date, city and call for papers link
    			""";
        return this.chatClient.prompt()
        		 .system(s -> s.text(SYSTEM_PROMPT).param("topic", topic))
        		.user(u -> u.text(USER_PROMPT).param("topic", topic).param("number_of_months", numOfMonths))
                .tools(this.zonedDateTimeTool, this.conferenceSearchTool)
                .stream()
                .content();
    }
    
    @GetMapping("/conference-search")
    public Flux<String> conferenceSearch(@RequestParam(value = "prompt") String prompt) {
       return this.chatClient.prompt()
        		.user(prompt)
                .tools(this.conferenceSearchTool)
                .stream()
                .content();
    }
}