package dev.vkazulkin.conference;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.vkazulkin.tool.search.GoogleSearchTool;
import dev.vkazulkin.tool.zdt.ZonedDateTimeTool;
import reactor.core.publisher.Flux;

@RestController
public class ConferenceSearchContoller {
	
    private final ChatClient chatClient;
    private final ZonedDateTimeTool zonedDateTimeTool;
    private final GoogleSearchTool googleSearchTool;
    
    private static final String USER_PROMPT= """
			
			1. Provide me with 5 best suggestions to apply for the talk for the {topic} conferences.
			2  Conferences should take place in the current time zone or the time zone nearby
			3. Conferences should start between the current date and the next {number_of_months} months. 
			4. Please provide the information in the response about my current date and time zone that you used for the search
			5. Please include the following conference info in the response only: name, homepage, date, call for papers link
			6. Please format the response to present each conference info in the separate line 
		
			""";
    
    private static final String SYSTEM_PROMPT="""
    		You are only able to answer questions about upcoming technical conferences. 
    		If the provided search term {topic} is not a technical term or is not connected to the conference for the software development, 
    		please respond in the friendly manner that you're not able to provide this information.
    		""";
    
    public ConferenceSearchContoller(ChatClient.Builder builder, ZonedDateTimeTool zonedDateTimeTool, GoogleSearchTool googleSearchTool) {
		var options = ToolCallingChatOptions.builder()
				 .model("amazon.nova-lite-v1:0")
				  //.model("amazon.nova-pro-v1:0")
				 .maxTokens(2000).build();
		
		this.chatClient = builder
				.defaultOptions(options)
				//.defaultSystem(SYSTEM_PROMPT)
				.build();
        this.zonedDateTimeTool=zonedDateTimeTool;
        this.googleSearchTool=googleSearchTool;
    }

    @GetMapping("/conference-search")
    public Flux<String> conferenceSearch(@RequestParam(value = "topic", defaultValue = "Java") String topic,
    		@RequestParam(value = "number_of_months", defaultValue = "6") String numOfMonths) {
        return this.chatClient.prompt()
        		.system(s -> s.text(SYSTEM_PROMPT).param("topic", topic))
        		.user(u -> u.text(USER_PROMPT).param("topic", topic).param("number_of_months", numOfMonths))
                .tools(this.zonedDateTimeTool, this.googleSearchTool)
                .stream()
                .content();
    }
}