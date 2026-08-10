package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSearchService {

    private final List<JobSearcher> jobSearchers;

    public JobSearchService(List<JobSearcher> jobSearchers) {
        this.jobSearchers = jobSearchers;
    }

    public List<JobOffer> searchAll(){
        return jobSearchers.stream()
                .flatMap(s -> s.search().stream())
                .toList();
    }
}
