package com.ogidazepam.search_service.justjoinit.client;

import com.ogidazepam.search_service.justjoinit.model.JustJoinItJobDetails;
import com.ogidazepam.search_service.justjoinit.model.JustJoinItJobOffer;
import com.ogidazepam.search_service.justjoinit.model.JustJoinItOffersResponse;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class JustJoinItClient {
    private static final String JOBS_API_URL =
            "https://justjoin.it/api/candidate-api/offers?categories=java&experienceLevels=junior&experienceLevels=intern&sortBy=publishedAt&orderBy=descending&from=0&itemsCount=100";
    private static final String JOB_API_URL = "https://justjoin.it/api/candidate-api/offers/";

    private final RestClient restClient;

    public JustJoinItClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Retryable(
            includes = HttpClientErrorException.TooManyRequests.class,
            maxRetries = 5,
            delay = 1000,
            multiplier = 2,
            maxDelay = 10000,
            jitter = 200
    )
    public JustJoinItJobDetails fetchJobOffersDetails(String slug){
        return restClient.get()
                .uri(JOB_API_URL + slug)
                .retrieve()
                .body(JustJoinItJobDetails.class);
    }

    @Retryable(
            includes = HttpClientErrorException.TooManyRequests.class,
            maxRetries = 5,
            delay = 1000,
            multiplier = 2,
            maxDelay = 10000,
            jitter = 200
    )
    public List<JustJoinItJobOffer> fetchJobOffers(){
        return restClient.get()
                .uri(JOBS_API_URL)
                .retrieve()
                .body(JustJoinItOffersResponse.class)
                .data();
    }
}
