package com.ogidazepam.search_service.websites.pracujpl.service;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.utils.RedisCacheService;
import com.ogidazepam.search_service.websites.pracujpl.client.PracujPlClient;
import com.ogidazepam.search_service.websites.pracujpl.mapper.PracujPlOfferMapper;
import com.ogidazepam.search_service.websites.pracujpl.model.offer.*;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.pracujpl.util.PracujPlUriBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class PracujPlJobSearcher implements JobSearcher{

    private final PracujPlClient pracujPlClient;
    private final PracujPlOfferMapper offerMapper;
    private final PracujPlUriBuilder uriBuilder;
    private final RedisCacheService redisCacheService;

    public PracujPlJobSearcher(PracujPlClient pracujPlClient, PracujPlOfferMapper offerMapper, PracujPlUriBuilder uriBuilder, RedisCacheService redisCacheService) {
        this.pracujPlClient = pracujPlClient;
        this.offerMapper = offerMapper;
        this.uriBuilder = uriBuilder;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void search(CreatedTaskEvent event, Consumer<JobOffer> onFoundJob) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());

        pracujPlClient.fetchOffersUrls(uri)
                        .forEach(url -> {
                            JobOffer cachedJobOffer = redisCacheService.getJobOfferFromCache(url);
                            if (cachedJobOffer != null){
                                onFoundJob.accept(cachedJobOffer);
                            } else{
                                PracujPlOfferData offerData = pracujPlClient.fetchOffer(url);
                                JobOffer jobOffer = offerMapper.mapToJobOffer(offerData, event.taskId());

                                onFoundJob.accept(jobOffer);
                                redisCacheService.writeJobOfferToCache(jobOffer);
                            }
                        });
    }
}
