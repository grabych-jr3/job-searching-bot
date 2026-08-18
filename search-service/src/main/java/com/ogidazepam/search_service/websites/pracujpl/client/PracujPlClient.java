package com.ogidazepam.search_service.websites.pracujpl.client;

import com.ogidazepam.search_service.client.JobHttpClient;
import com.ogidazepam.search_service.websites.pracujpl.model.offer.PracujPlOfferData;
import com.ogidazepam.search_service.websites.pracujpl.model.offers.*;
import com.ogidazepam.search_service.websites.pracujpl.model.offers.PracujPlOffersData;
import com.ogidazepam.search_service.websites.pracujpl.model.offers.PracujPlOffersOffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class PracujPlClient {

    private static final String JOBS_API_URL = "https://it.pracuj.pl/praca?et=1%2C3%2C17&itth=38";
    private static final String SOURCE = "PracujPl";

    private final ObjectMapper objectMapper;
    private final JobHttpClient jobHttpClient;
    private final RedisTemplate<String, Boolean> redisTemplate;

    public PracujPlClient(ObjectMapper objectMapper, JobHttpClient jobHttpClient, RedisTemplate<String, Boolean> redisTemplate) {
        this.objectMapper = objectMapper;
        this.jobHttpClient = jobHttpClient;
        this.redisTemplate = redisTemplate;
    }

    public List<PracujPlOfferData> fetchOffers(String uri){
        List<String> urls = fetchOffersUrls(uri);

        List<PracujPlOfferData> offers = new ArrayList<>();
        for (String url : urls){
            JsonNode dataNode = extractData(url);
            PracujPlOfferData offerData = parseData(dataNode, PracujPlOfferData.class);

            offers.add(offerData);
        }

        return offers;
    }

    private List<String> fetchOffersUrls(String uri){
        JsonNode dataNode = extractData(uri);
        PracujPlOffersData data = parseData(dataNode, PracujPlOffersData.class);

        return data
                .groupedOffers()
                .stream()
                .flatMap(group -> group.offers().stream())
                .map(PracujPlOffersOffer::offerAbsoluteUri)
                .filter(url -> {
                    String key = "processed_offer:" + SOURCE + ":" + url;
                    return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
                })
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
