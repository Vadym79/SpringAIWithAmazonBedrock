package dev.vkazulkin.tool.conference;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.vkazulkin.conference.Conference;
import dev.vkazulkin.conference.Conferences;

@Component
public class ConferenceSearchTool {
	
	private static final Logger logger = LoggerFactory.getLogger(ConferenceSearchTool.class);
	
	private Set<Conference> conferences;

	public ConferenceSearchTool(ObjectMapper objectMapper) {
		objectMapper.registerModule(new JavaTimeModule());
		this.conferences= this.getAllConferences(objectMapper).conferences();

	}

	@Tool(name="Conference_Search_Tool_By_Topic", description = "Search for the conference list for exactly one topic provided")
    public Set<Conference> search(@ToolParam(description = "conference topic") String topic,
    		@ToolParam(description = " the conference earliest start date") LocalDate earliestStartDate,
    		@ToolParam(description = " the conference latest start date") LocalDate latestStartDate) {
    	
		logger.info("search topic "+topic);
		logger.info("earliest start date "+earliestStartDate);
		logger.info("latest start date "+latestStartDate);
		
		Set<Conference> foundConferences= this.conferences.stream()
		.filter(c -> c.topics().contains(topic))
		.filter(c -> c.startDate().isAfter(earliestStartDate) && c.startDate().isBefore(latestStartDate))
		.collect(Collectors.toSet());
		
		logger.info("return list of conferences: "+foundConferences);
		return foundConferences;
	  }
	
	
	@Tool(name="All_Conference_Search_Tool", description = "Get the list of all conferences and answer questions about them")
    public Set<Conference> searchAllConferences() {
		logger.info("Search for all conferences "+this.conferences);
		return this.conferences;
	  }

	
	private Conferences getAllConferences(ObjectMapper objectMapper) {
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/conferences.json")) {
			return objectMapper.readValue(inputStream, Conferences.class);
		} 
		catch(IOException ex) {
			throw new RuntimeException("can't read conferences");
		}
	}
}