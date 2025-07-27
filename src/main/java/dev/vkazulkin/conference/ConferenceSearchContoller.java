package dev.vkazulkin.conference;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.vkazulkin.tool.date.DateTool;
import dev.vkazulkin.tool.search.GoogleSearchTool;
import dev.vkazulkin.tool.timezone.TimeZoneTool;
import reactor.core.publisher.Flux;

@RestController
public class ConferenceSearchContoller {
	
    private final ChatClient chatClient;
    private final DateTool dateTool;
    private final TimeZoneTool timeZoneTool;
    private final GoogleSearchTool googleSearchTool;
    
    private static final String USER_PROMPT= """
			
			1. Provide me with 5 best suggestions to apply for the talk for the Java or AWS cloud related conference.
			2  Conferences should take place in the my current time zone or the time zone nearby
			3. Conference should start between current date and 6 months from now. 
			4. Please include the following conference info in the response only: name, homepage, date, call for papers link
			5. Please sort the response to present AWS cloud conferences first and then Java conferences
			6. Exlude all conferences which took place in the past.
			7. Please format the response to present each conference info in the separate line 
		
			""";
    
    private static final String SYSTEM_PROMPT="""
    		I'm only able to answer questions about upcoming technical conferences.
    		Please respond to all other questions that you're not able to answer them.
    		""";
    
    public ConferenceSearchContoller(ChatClient.Builder builder, DateTool dateTool, TimeZoneTool timeZoneTool, GoogleSearchTool googleSearchTool) {
		var options = ToolCallingChatOptions.builder()
				 .model("amazon.nova-lite-v1:0")
				  //.model("amazon.nova-pro-v1:0")
				 .maxTokens(2000).build();
		
		this.chatClient = builder.defaultOptions(options).defaultSystem(SYSTEM_PROMPT).build();
        this.dateTool=dateTool;
        this.timeZoneTool=timeZoneTool;
        this.googleSearchTool=googleSearchTool;
    }

    @GetMapping("/conference-search")
    public Flux<String> conferenceSearch(@RequestParam(value = "message", defaultValue = USER_PROMPT) String message) {
        return this.chatClient.prompt(message)
                .tools(this.dateTool, this.timeZoneTool, this.googleSearchTool)
                .stream()
                .content();
    }
}
