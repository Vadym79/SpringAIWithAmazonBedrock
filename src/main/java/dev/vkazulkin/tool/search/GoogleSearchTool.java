package dev.vkazulkin.tool.search;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoogleSearchTool {
	
	@Value("${GOOGLE_CUSTOM_SEARCH_API_KEY}")
	private String gooleSearchAPIKey;
	
	@Value("${CX}")
	private String cx;
	
	private static final RestClient restClient=RestClient.create();
	
	public GoogleSearchTool() {
	}

	@Tool(name="Google_Search_Tool", description = "Search for the conference list for one or more topics provided")
    public String search(@ToolParam(description = "Search for conference list for the topics provided in the user's prompt. Only include the conference time zone but don't include any conference dates in the search") String topic) {
    	System.out.println("search topic "+topic);
		return restClient.get()
    			  .uri("https://www.googleapis.com/customsearch/v1?key="+gooleSearchAPIKey+"&cx="+cx+"&q="+topic)
    			  .retrieve()
    			  .body(String.class);
    }
}
