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
import java.util.function.Consumer;

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
    public void search(CreatedTaskEvent event, Consumer<JobOffer> onFoundJob) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());

        bulldogJobClient.fetchJobOfferIds(uri)
                .forEach(id -> {
                    BulldogJobNextData job = bulldogJobClient.fetchJobOffer(id);

                    JobOffer jobOffer = jobMapper.mapToJobOffer(job, event.taskId());
                    onFoundJob.accept(jobOffer);
                });
    }
}
