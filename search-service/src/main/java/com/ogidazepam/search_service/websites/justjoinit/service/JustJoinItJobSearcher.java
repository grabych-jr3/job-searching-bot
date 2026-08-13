package com.ogidazepam.search_service.websites.justjoinit.service;

import com.ogidazepam.search_service.websites.justjoinit.client.JustJoinItClient;
import com.ogidazepam.search_service.websites.justjoinit.mapper.JustJoinItMapper;
import com.ogidazepam.search_service.websites.justjoinit.model.*;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobData;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobDetails;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JustJoinItJobSearcher implements JobSearcher {

    private static final String JOB_URL = "https://justjoin.it/job-offer/";
    private static final String SOURCE = "JustJoinIt";

    private final JustJoinItMapper mapper;
    private final JustJoinItClient justJoinItClient;
    private final RedisTemplate<String, Boolean> redisTemplate;

    public JustJoinItJobSearcher(JustJoinItMapper mapper, JustJoinItClient justJoinItClient, RedisTemplate<String, Boolean> redisTemplate) {
        this.mapper = mapper;
        this.justJoinItClient = justJoinItClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<JobOffer> search() {
        return justJoinItClient.fetchJobOffers().stream()
                .filter(offer -> {
                    String key = "processed_offer:" + SOURCE + ":" + offer.slug();
                    return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
                })
                .map(offer -> {
                    JustJoinItJobDetails jobDetails = justJoinItClient
                            .fetchJobOffersDetails(offer.slug());

                    return mapper.mapToJobOffer(new JustJoinItJobData(
                            offer,
                            jobDetails
                    ));
                })
                .toList();
    }
}
