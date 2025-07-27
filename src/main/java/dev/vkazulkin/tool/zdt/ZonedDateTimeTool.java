package dev.vkazulkin.tool.date;

import java.time.LocalDateTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DateTool {
	
    @Tool(name="Date_Tool",description = "Get the current date and time")
    String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
}
