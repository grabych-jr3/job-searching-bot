package com.ogidazepam.search_service.websites.pracujpl.client;

import com.ogidazepam.search_service.client.JobHttpClient;
import com.ogidazepam.search_service.websites.pracujpl.model.offer.PracujPlOfferData;
import com.ogidazepam.search_service.websites.pracujpl.model.offers.*;
import com.ogidazepam.search_service.websites.pracujpl.model.offers.PracujPlOffersData;
import com.ogidazepam.search_service.websites.pracujpl.model.offers.PracujPlOffersOffer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class PracujPlClient {

    private final ObjectMapper objectMapper;
    private final JobHttpClient jobHttpClient;

    public PracujPlClient(ObjectMapper objectMapper, JobHttpClient jobHttpClient) {
        this.objectMapper = objectMapper;
        this.jobHttpClient = jobHttpClient;
    }

    public PracujPlOfferData fetchOffer(String url){
        JsonNode dataNode = extractData(url);
        return parseData(dataNode, PracujPlOfferData.class);
    }

    public List<String> fetchOffersUrls(String uri){
        JsonNode dataNode = extractData(uri);
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
