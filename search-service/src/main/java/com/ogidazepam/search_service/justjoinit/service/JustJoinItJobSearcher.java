package com.ogidazepam.search_service.justjoinit.service;

import com.ogidazepam.search_service.justjoinit.client.JustJoinItClient;
import com.ogidazepam.search_service.justjoinit.mapper.JustJoinItMapper;
import com.ogidazepam.search_service.justjoinit.model.*;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JustJoinItJobSearcher implements JobSearcher {

    private static final String JOB_URL = "https://justjoin.it/job-offer/";

    private final JustJoinItMapper mapper;
    private final JustJoinItClient justJoinItClient;

    public JustJoinItJobSearcher(JustJoinItMapper mapper, JustJoinItClient justJoinItClient) {
        this.mapper = mapper;
        this.justJoinItClient = justJoinItClient;
    }

    @Override
    public List<JobOffer> search() {
        return justJoinItClient.fetchJobOffers().stream()
                .map(offer -> {
                    JustJoinItJobDetails jobDetails = justJoinItClient
                            .fetchJobOffersDetails(offer.getSlug());

                    return mapper.mapToJobOffer(new JustJoinItJobData(
                            offer,
                            jobDetails
                    ));
                })
                .toList();
    }
}
