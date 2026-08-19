package com.ogidazepam.search_service.websites.justjoinit.service;

import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.websites.justjoinit.client.JustJoinItClient;
import com.ogidazepam.search_service.websites.justjoinit.mapper.JustJoinItMapper;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobData;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobDetails;
import com.ogidazepam.search_service.websites.justjoinit.util.JustJoinItUriBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class JustJoinItJobSearcher implements JobSearcher {

    private static final String SOURCE = "JustJoinIt";

    private final JustJoinItMapper mapper;
    private final JustJoinItUriBuilder uriBuilder;
    private final JustJoinItClient justJoinItClient;
    private final RedisTemplate<String, Boolean> redisTemplate;

    public JustJoinItJobSearcher(JustJoinItMapper mapper, JustJoinItUriBuilder uriBuilder, JustJoinItClient justJoinItClient, RedisTemplate<String, Boolean> redisTemplate) {
        this.mapper = mapper;
        this.uriBuilder = uriBuilder;
        this.justJoinItClient = justJoinItClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void search(CreatedTaskEvent event, Consumer<JobOffer> onFoundJob) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());
        justJoinItClient.fetchJobOffers(uri).stream()
                .filter(offer -> {
                    String key = "processed_offer:" + SOURCE + ":" + offer.slug();
                    return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
                })
                .forEach(offer -> {
                    JustJoinItJobDetails jobDetails = justJoinItClient
                            .fetchJobOffersDetails(offer.slug());

                    JobOffer jobOffer = mapper.mapToJobOffer(new JustJoinItJobData(
                            offer,
                            jobDetails
                    ),
                            event.taskId()
                    );

                    onFoundJob.accept(jobOffer);
                });
    }
}
