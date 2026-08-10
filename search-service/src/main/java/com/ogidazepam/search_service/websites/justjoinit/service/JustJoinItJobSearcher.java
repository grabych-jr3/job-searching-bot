package com.ogidazepam.search_service.websites.justjoinit.service;

import com.ogidazepam.search_service.websites.justjoinit.client.JustJoinItClient;
import com.ogidazepam.search_service.websites.justjoinit.mapper.JustJoinItMapper;
import com.ogidazepam.search_service.websites.justjoinit.model.*;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobData;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobDetails;
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
