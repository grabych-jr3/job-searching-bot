package com.ogidazepam.search_service.client;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class JobHttpClient {

    private static final String NEXT_DATA_SCRIPT_ID = "__NEXT_DATA__";
    private static final int MAX_ATTEMPTS = 5;
    private static final long INITIAL_DELAY = 1000;

    private final ObjectMapper objectMapper;

    public JobHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public JsonNode extractNextDataWithRetry(String url){
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            try {
                return extractNextData(url);
            } catch (HttpStatusException e) {
                if (e.getStatusCode() != 429 || i == MAX_ATTEMPTS) {
                    throw new RuntimeException("HTTP error " + e.getStatusCode() + " fetching " + url, e);
                }
                sleep(INITIAL_DELAY * (1L << (i - 1)));
            } catch (IOException e) {
                if (i == MAX_ATTEMPTS) {
                    throw new RuntimeException("Failed to fetch data from " + url, e);
                }
            }
        }

        throw new IllegalStateException("Unexpected retry state");
    }

    private JsonNode extractNextData(String url) throws IOException {
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

    private void sleep(long delay){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted during retry", e);
        }
    }
}
