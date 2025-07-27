package dev.vkazulkin.conference;

import java.time.LocalDateTime;

public record Conference (String name, String homepage, LocalDateTime dateTime, String linkToCallforPapers) {
	
}
