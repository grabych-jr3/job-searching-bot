package com.ogidazepam.search_service.pracujpl.client;

import com.ogidazepam.search_service.client.JobHttpClient;
import com.ogidazepam.search_service.pracujpl.model.offer.PracujPlOfferData;
import com.ogidazepam.search_service.pracujpl.model.offers.*;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class PracujPlClient {

    private static final String JOBS_API_URL = "https://it.pracuj.pl/praca?et=1%2C3%2C17&itth=38";

    private final ObjectMapper objectMapper;
    private final JobHttpClient jobHttpClient;

    public PracujPlClient(ObjectMapper objectMapper, JobHttpClient jobHttpClient) {
        this.objectMapper = objectMapper;
        this.jobHttpClient = jobHttpClient;
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

    private JsonNode extractData(String url){
        return jobHttpClient.extractNextDataWithRetry(url)
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
