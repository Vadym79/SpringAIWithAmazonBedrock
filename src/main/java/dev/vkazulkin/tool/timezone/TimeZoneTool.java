package dev.vkazulkin.tool.timezone;

import java.util.TimeZone;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TimeZoneTool {

	@Tool(name = "TimeZone_Tool", description = "Get the current time zone")
	TimeZone getCurrentDateTime() {
		return TimeZone.getDefault();
	}
}


