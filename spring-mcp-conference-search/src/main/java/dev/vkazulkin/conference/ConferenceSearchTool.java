package dev.vkazulkin.conference;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class ConferenceSearchTool {

	private final Set<Conference> conferences;

	public ConferenceSearchTool(ObjectMapper objectMapper) {
		objectMapper.registerModule(new JavaTimeModule());
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/conferences.json")) {
			conferences = objectMapper.readValue(inputStream, Conferences.class).conferences();
		} catch (IOException ex) {
			throw new RuntimeException("can't read conferences");
		}

	}

	@Tool(name = "Conference_Search_Tool_By_Topic_And_Date", description = "Search for the conference list for exactly one topic provided and conference dates")
	public Set<Conference> search(@ToolParam(description = "conference topic") String topic,
			@ToolParam(description = " the conference earliest start date") LocalDate earliestStartDate,
			@ToolParam(description = " the conference latest start date") LocalDate latestStartDate) {

		System.out.println("search topic " + topic);
		System.out.println("earliest start date " + earliestStartDate);
		System.out.println("latest start date " + latestStartDate);
		Set<Conference> foundConferences;
		foundConferences = this.conferences.stream().filter(c -> c.topics().contains(topic))
				.filter(c -> c.startDate().isAfter(earliestStartDate) && c.startDate().isBefore(latestStartDate))
				.collect(Collectors.toSet());

		System.out.println("return list of conferences: " + foundConferences);
		return foundConferences;
	}

	@Tool(name = "Conference_Search_Tool_By_Topic", description = "Search for the conference list for exactly one topic provided")
	public Set<Conference> search(@ToolParam(description = "conference topic") String topic) {
		System.out.println("search topic " + topic);
		Set<Conference> foundConferences;
		foundConferences = this.conferences.stream().filter(c -> c.topics().contains(topic))
				.collect(Collectors.toSet());

		System.out.println("return list of conferences: " + foundConferences);
		return foundConferences;
	}

	@Tool(name = "All_Conference_Search_Tool", description = "Get the list of all conferences and answer questions about them")
	public Set<Conference> searchAllConferences() {
		System.out.println("Search for all conferences");
		return this.conferences;
	}

}