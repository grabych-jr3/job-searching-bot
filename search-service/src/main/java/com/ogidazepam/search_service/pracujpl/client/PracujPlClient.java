package com.ogidazepam.search_service.pracujpl.client;

import com.ogidazepam.search_service.pracujpl.model.offer.PracujPlOfferData;
import com.ogidazepam.search_service.pracujpl.model.offers.*;
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
public class PracujPlClient {

    private static final String NEXT_DATA_SCRIPT_ID = "__NEXT_DATA__";
    private static final String JOBS_API_URL = "https://it.pracuj.pl/praca?et=1%2C3%2C17&itth=38";

    private final ObjectMapper objectMapper;

    public PracujPlClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<PracujPlOfferData> fetchOffers(){
        List<String> urls = fetchOffersUrls();

        List<PracujPlOfferData> offers = new ArrayList<>();
        for (String url : urls){
            JsonNode dataNode = extractData(url);

            offers.add(parseData(dataNode, PracujPlOfferData.class));
        }

        return offers;
    }

    private List<String> fetchOffersUrls(){
        JsonNode dataNode = extractData(JOBS_API_URL);
        PracujPlOffersData data = parseData(dataNode, PracujPlOffersData.class);

        return data
                .groupedOffers()
                .stream()
                .flatMap(group -> group.offers().stream())
                .map(PracujPlOffersOffer::offerAbsoluteUri)
                .toList();
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

    private JsonNode extractData(String url){
        return extractNextDataWithRetry(url)
                .path("props")
                .path("pageProps")
                .path("dehydratedState")
                .path("queries")
                .get(0)
                .path("state")
                .path("data");
    }

    private <T> T parseData(JsonNode query, Class<T> dataType){
        return objectMapper.treeToValue(query, dataType);
    }
}
