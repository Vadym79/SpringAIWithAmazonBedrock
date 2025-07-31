package dev.vkazulkin.tool.zdt;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZonedDateTimeChatController {
	
    private final ChatClient chatClient;
    private final ZonedDateTimeTool zonedDateTimeTool;

    public ZonedDateTimeChatController(ChatClient.Builder builder, ZonedDateTimeTool zonedDateTimeTool) {
        this.chatClient = builder.build();
        this.zonedDateTimeTool=zonedDateTimeTool;
    }

    @GetMapping("/date-tool")
    public String tools() {
        return this.chatClient.prompt("Get is the current date and time?")
                .tools(this.zonedDateTimeTool)
                .call()
                .content();
    }

}
