package com.ogidazepam.search_service.bulldogJob.service;

import com.ogidazepam.search_service.bulldogJob.client.BulldogJobClient;
import com.ogidazepam.search_service.bulldogJob.model.BulldogJobMetaData;
import com.ogidazepam.search_service.bulldogJob.model.BulldogJobNextData;
import com.ogidazepam.search_service.bulldogJob.model.BulldogJobOffer;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
public class BulldogJobSearcher implements JobSearcher {

    private final BulldogJobClient bulldogJobClient;

    public BulldogJobSearcher(BulldogJobClient bulldogJobClient) {
        this.bulldogJobClient = bulldogJobClient;
    }

    @Override
    public List<JobOffer> search() {
        List<BulldogJobNextData> jobOffers = bulldogJobClient.fetchJobOffers();

        return jobOffers.stream()
                .map(this::mapToJobOffer)
                .toList();
    }

    private JobOffer mapToJobOffer(BulldogJobNextData nextData){
        BulldogJobMetaData metaData = nextData.props().pageProps().metaData();
        BulldogJobOffer job = nextData.props().pageProps().data().job();

        List<String> cities = job.locations().stream()
                .map(j -> j.location().cityEn())
                .toList();

        return JobOffer.builder()
                .url(metaData.canonicalUrl())
                .jobTitle(metaData.title())
                .companyName(job.company().name())
                .jobDescription(job.details())
                .requirements(job.requirements())
                .employmentType(job.employmentType())
                .position(job.position())
                .remote(job.remote())
                .workModes(job.workModes())
                .experienceLevel(job.experienceLevel())
                .experienceInYears(job.minExperienceInYears())
                .technologyTags(job.technologyTags())
                .country(nextData.props().pageProps().country())
                .cities(cities)
                .build();
    }
}
