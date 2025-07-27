package dev.vkazulkin.tool.search;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class GoogleSearchController {
	
	@Value("${GOOGLE_CUSTOM_SEARCH_API_KEY}")
	private String gooleSearchAPIKey;
	
	@Value("${CX}")
	private String cx;
	
	private static final RestClient restClient=RestClient.create();
	
	public GoogleSearchController() {
	}

    @GetMapping("/search")
    public void search(@RequestParam(value = "query", defaultValue = "Provide future Java and AWS cloud conference list for 2025 or 2026 where I can apply for the talk") String query) {
    	String result = restClient.get()
    			  .uri("https://www.googleapis.com/customsearch/v1?key="+gooleSearchAPIKey+"&cx="+cx+"&q="+query)
    			  .retrieve()
    			  .body(String.class);
    	System.out.print(result);
    }
}
