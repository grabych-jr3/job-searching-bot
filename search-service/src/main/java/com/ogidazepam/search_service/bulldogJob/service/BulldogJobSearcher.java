package com.ogidazepam.search_service.bulldogJob.service;

import com.ogidazepam.search_service.bulldogJob.client.BulldogJobClient;
import com.ogidazepam.search_service.bulldogJob.mapper.BulldogJobMapper;
import com.ogidazepam.search_service.bulldogJob.model.BulldogJobNextData;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BulldogJobSearcher implements JobSearcher {

    private final BulldogJobMapper jobMapper;
    private final BulldogJobClient bulldogJobClient;

    public BulldogJobSearcher(BulldogJobMapper jobMapper, BulldogJobClient bulldogJobClient) {
        this.jobMapper = jobMapper;
        this.bulldogJobClient = bulldogJobClient;
    }

    @Override
    public List<JobOffer> search() {
        List<BulldogJobNextData> jobOffers = bulldogJobClient.fetchJobOffers();

        return jobOffers.stream()
                .map(jobMapper::mapToJobOffer)
                .toList();
    }
}
