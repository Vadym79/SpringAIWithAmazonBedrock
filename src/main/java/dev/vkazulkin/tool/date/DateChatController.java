package dev.vkazulkin.tool.date;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DateChatController {
	
    private final ChatClient chatClient;
    private final DateTool dateTool;

    public DateChatController(ChatClient.Builder builder, DateTool dateTool) {
        this.chatClient = builder.build();
        this.dateTool=dateTool;
    }

    @GetMapping("/date-tool")
    public String tools() {
        return this.chatClient.prompt("Get is the current date and time?")
                .tools(this.dateTool)
                .call()
                .content();
    }

}
