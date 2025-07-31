package dev.vkazulkin.conference;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record Conferences (Set<Conference> conferences) {

	public static void main(String[] args) throws JsonProcessingException {
		
		var conf1= new Conference("AWS Re:Invent", Set.of("AWS Cloud", "Serverless", "Gen AI"),
				"https://reinvent.awsevents.com", LocalDate.of(2025, Month.DECEMBER, 1),
				LocalDate.of(2025, Month.DECEMBER, 5), "Las Vegas", "https://reinvent.awsevents.com/cfp");
		
		var conf2= new Conference("Voxxed Days Belgium", Set.of("Java", "JVM", "Spring", "AI"),
				"https://devoxx.be/", LocalDate.of(2025, Month.OCTOBER, 6),
				LocalDate.of(2025, Month.OCTOBER, 10), "Antwerpen", "https://devoxx.be/cfp");
		
		var conf3= new Conference("PyCon", Set.of("Python", "ML", "AI"),
				"https://pycon.com/", LocalDate.of(2025, Month.NOVEMBER, 4),
				LocalDate.of(2025, Month.NOVEMBER, 6), "Sydney", "https://pycon/cfp");
		
		var conf4= new Conference("Serverless Days Sao Paolo", Set.of("AWS Cloud", "Azure Cloud", "Google Cloud", 
				"Serverless", "Gen AI"),"https://saopaolo-serverlessdays.io", LocalDate.of(2025, Month.NOVEMBER, 8),
				LocalDate.of(2025, Month.NOVEMBER, 8), "Sao Paolo", "https://saopaolo-serverlessdays.io");
		
		var conf5= new Conference("JCON Europe", Set.of("Java", "JVM", "Spring", "AI", "Cloud"),
				"https://2026.europe.jcon.one/", LocalDate.of(2026, Month.APRIL, 20),
				LocalDate.of(2026, Month.APRIL, 23), "Cologne", "https://2026.europe.jcon.one/cfp");
		
		
		var conferences= new Conferences(Set.of(conf1, conf2, conf3, conf4, conf5));
		
		System.out.print(new ObjectMapper().writeValueAsString(conferences));
	
	}

}
