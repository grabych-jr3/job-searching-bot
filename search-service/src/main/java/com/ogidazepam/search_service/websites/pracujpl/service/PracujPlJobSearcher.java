package com.ogidazepam.search_service.websites.pracujpl.service;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.websites.pracujpl.client.PracujPlClient;
import com.ogidazepam.search_service.websites.pracujpl.mapper.PracujPlOfferMapper;
import com.ogidazepam.search_service.websites.pracujpl.model.offer.*;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.pracujpl.util.PracujPlUriBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PracujPlJobSearcher implements JobSearcher{

    private final PracujPlClient pracujPlClient;
    private final PracujPlOfferMapper offerMapper;
    private final PracujPlUriBuilder uriBuilder;

    public PracujPlJobSearcher(PracujPlClient pracujPlClient, PracujPlOfferMapper offerMapper, PracujPlUriBuilder uriBuilder) {
        this.pracujPlClient = pracujPlClient;
        this.offerMapper = offerMapper;
        this.uriBuilder = uriBuilder;
    }

    @Override
    public List<JobOffer> search(CreatedTaskEvent event) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());

        return pracujPlClient.fetchOffers(uri).stream()
                .map(offer -> offerMapper.mapToJobOffer(offer, event.taskId()))
                .toList();
    }
}
