package dev.vkazulkin.tool.zdt;

import java.time.LocalDateTime;
import java.util.TimeZone;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class ZonedDateTimeTool {
	
    @Tool(name="Current_Date_Time_Tool", description = "Provide the current date")
    String getZonedDateTime() {
    	System.out.println("Current_Date_Time_Tool returned: "+LocalDateTime.now().toString());
        return LocalDateTime.now().toString();
    }
    
    @Tool(name="Current_Time_Zone", description = "Provide the current time zone")
    String getTimeZone() {
    	System.out.println("Current_Time_Zone returned: "+TimeZone.getDefault().toString());
        return TimeZone.getDefault().toString();
    }
}
