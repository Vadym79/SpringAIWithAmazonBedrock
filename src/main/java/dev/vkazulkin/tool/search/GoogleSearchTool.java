package dev.vkazulkin.tool.search;


import org.springframework.ai.tool.annotation.Tool;
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

	@Tool(name="Google_Search_Tool", description = "Provide future Java and AWS cloud conference list where I can apply for the talk")
    public String search() {
    	String query = "provide Java and AWS cloud conference list where I can apply for the talk";
    	return restClient.get()
    			  .uri("https://www.googleapis.com/customsearch/v1?key="+gooleSearchAPIKey+"&cx="+cx+"&q="+query)
    			  .retrieve()
    			  .body(String.class);
    }
}
