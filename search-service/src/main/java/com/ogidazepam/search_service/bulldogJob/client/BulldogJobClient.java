package com.ogidazepam.search_service.bulldogJob.client;

import com.ogidazepam.search_service.bulldogJob.model.BulldogJobNextData;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BulldogJobClient {

    private static final String JOBS_URL = "https://bulldogjob.pl/companies/jobs/s/skills,Java";
    private static final String JOB_URL = "https://bulldogjob.pl/companies/jobs/";
    private static final String NEXT_DATA_SCRIPT_ID = "__NEXT_DATA__";

    private final ObjectMapper objectMapper;

    public BulldogJobClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<BulldogJobNextData> fetchJobOffers(){
        List<String> ids = fetchJobOfferIds();

        List<BulldogJobNextData> jobOffers = new ArrayList<>();
        for(String id : ids){
            jobOffers.add(extractJob(id));
        }
        return jobOffers;
    }

    private List<String> fetchJobOfferIds(){
        JsonNode jobs = extractJobs();

        List<String> ids = new ArrayList<>();
        for (JsonNode job : jobs){
            ids.add(job.path("id").stringValue());
        }
        return ids;
    }

    private JsonNode extractNextData(String url) throws IOException{
        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();
        Element element = document.getElementById(NEXT_DATA_SCRIPT_ID);

        if (element == null){
            throw new IllegalStateException("__NEXT_DATA__ not found");
        }

        return objectMapper.readValue(element.data(), JsonNode.class);
    }

    private JsonNode extractNextDataWithRetry(String url){
        int maxAttempts = 5;
        long initialDelay = 1000;

        for (int i = 1; i <= maxAttempts; i++) {
            try {
                return extractNextData(url);
            } catch (HttpStatusException e) {
                if (e.getStatusCode() != 429 || i == maxAttempts) {
                    throw new RuntimeException("HTTP error " + e.getStatusCode() + " fetching " + url, e);
                }
                sleep(initialDelay * (1L << (i - 1)));
            } catch (IOException e) {
                if (i == maxAttempts) {
                    throw new RuntimeException("Failed to fetch data from " + url, e);
                }
            }
        }

        throw new IllegalStateException("Unexpected retry state");
    }

    private void sleep(long delay){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted during retry", e);
        }
    }

    private JsonNode extractJobs(){
        return extractNextDataWithRetry(JOBS_URL)
                .path("props")
                .path("pageProps")
                .path("jobs");
    }

    private BulldogJobNextData extractJob(String id){
        return objectMapper.treeToValue(
                extractNextDataWithRetry(JOB_URL + id),
                BulldogJobNextData.class
        );
    }
}
