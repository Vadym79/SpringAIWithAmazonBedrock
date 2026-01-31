package dev.vkazulkin.conference;

import java.time.LocalDate;
import java.util.Set;

public record Conference (String name, Set<String> topics, String homepage, 
		LocalDate startDate, LocalDate endDate, String city, String linkToCallforPapers) {

}


