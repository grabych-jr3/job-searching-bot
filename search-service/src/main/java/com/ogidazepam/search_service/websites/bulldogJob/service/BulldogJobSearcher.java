package com.ogidazepam.search_service.websites.bulldogJob.service;

import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.websites.bulldogJob.client.BulldogJobClient;
import com.ogidazepam.search_service.websites.bulldogJob.mapper.BulldogJobMapper;
import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobNextData;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.bulldogJob.util.BulldogJobUriBuilder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BulldogJobSearcher implements JobSearcher {

    private final BulldogJobMapper jobMapper;
    private final BulldogJobClient bulldogJobClient;
    private final BulldogJobUriBuilder uriBuilder;

    public BulldogJobSearcher(BulldogJobMapper jobMapper, BulldogJobClient bulldogJobClient, BulldogJobUriBuilder uriBuilder) {
        this.jobMapper = jobMapper;
        this.bulldogJobClient = bulldogJobClient;
        this.uriBuilder = uriBuilder;
    }

    @Override
    public List<JobOffer> search(CreatedTaskEvent event) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());

        List<BulldogJobNextData> jobOffers = bulldogJobClient.fetchJobOffers(uri);

        return jobOffers.stream()
                .map(offer -> jobMapper.mapToJobOffer(offer, event.taskId()))
                .toList();
    }
}
