package dev.vkazulkin.tool.conference;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

	private final ObjectMapper objectMapper;

	public ConferenceSearchTool(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.objectMapper.registerModule(new JavaTimeModule());

	}

	@Tool(name="Conference_Search_Tool", description = "Search for the conference list for exactly one topic provided")
    public Set<Conference> search(@ToolParam(description = "conference topic") String topic,
    		@ToolParam(description = " the conference earliest start date") LocalDate earliestStartDate,
    		@ToolParam(description = " the conference latest start date") LocalDate latestStartDate) {
    	
		System.out.println("search topic "+topic);
		System.out.println("earliest start date "+earliestStartDate);
		System.out.println("latest start date "+latestStartDate);
		var conferenceByTopic= new HashSet<Conference>();		
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/conferences.json")) {
			var conferences= this.objectMapper.readValue(inputStream, Conferences.class);
			for(Conference conference: conferences.conferences()) {
				LocalDate startDate= conference.startDate();
				if(startDate.isAfter(earliestStartDate) && startDate.isBefore(latestStartDate)) {
					for(String confTopic: conference.topics()) {		
						if(confTopic.contains(topic)) {
							   conferenceByTopic.add(conference);
						}
					}
			    }
			}
		} 
		catch(IOException ex) {
			throw new RuntimeException("can't read conferences");
		}
		System.out.println("return list of conferences: "+conferenceByTopic);
		return conferenceByTopic;
	  }
}
