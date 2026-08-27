package com.ogidazepam.search_service.websites.justjoinit.client;

import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobDetails;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobOffer;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItOffersResponse;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class JustJoinItClient {
    private static final String JOB_API_URL = "https://justjoin.it/api/candidate-api/offers/";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public JustJoinItClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Retryable(
            includes = {
                    HttpClientErrorException.TooManyRequests.class,
                    HttpServerErrorException.class,
                    ResourceAccessException.class
            },
            maxRetries = 5,
            delay = 1000,
            multiplier = 2,
            maxDelay = 10000,
            jitter = 200
    )
    public JustJoinItJobDetails fetchJobOffersDetails(String slug){
        String json = restClient.get()
                .uri(JOB_API_URL + slug)
                .retrieve()
                .body(String.class);

        JsonNode jsonNode = objectMapper.readTree(json);
        return objectMapper.treeToValue(jsonNode, JustJoinItJobDetails.class);
    }

    @Retryable(
            includes = HttpClientErrorException.TooManyRequests.class,
            maxRetries = 5,
            delay = 1000,
            multiplier = 2,
            maxDelay = 10000,
            jitter = 200
    )
    public List<JustJoinItJobOffer> fetchJobOffers(String uri){
        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(JustJoinItOffersResponse.class)
                .data();
    }
}
