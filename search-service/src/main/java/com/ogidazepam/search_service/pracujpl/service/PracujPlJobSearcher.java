package com.ogidazepam.search_service.pracujpl.service;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.pracujpl.client.PracujPlClient;
import com.ogidazepam.search_service.pracujpl.mapper.PracujPlOfferMapper;
import com.ogidazepam.search_service.pracujpl.model.offer.*;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PracujPlJobSearcher implements JobSearcher{

    private final PracujPlClient pracujPlClient;
    private final PracujPlOfferMapper offerMapper;

    public PracujPlJobSearcher(PracujPlClient pracujPlClient, PracujPlOfferMapper offerMapper) {
        this.pracujPlClient = pracujPlClient;
        this.offerMapper = offerMapper;
    }

    @Override
    public List<JobOffer> search() {
        return pracujPlClient.fetchOffers().stream()
                .map(offerMapper::mapToJobOffer)
                .toList();
    }
}
