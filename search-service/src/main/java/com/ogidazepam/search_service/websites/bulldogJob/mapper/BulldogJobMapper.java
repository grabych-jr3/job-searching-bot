package com.ogidazepam.search_service.websites.bulldogJob.mapper;

import com.ogidazepam.search_service.utils.HtmlCleaner;
import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobMetaData;
import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobNextData;
import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobOffer;
import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobJobLocation;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.mapper.JobOfferMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BulldogJobMapper implements JobOfferMapper<BulldogJobNextData> {

    @Override
    public JobOffer mapToJobOffer(BulldogJobNextData nextData, String taskId) {
        BulldogJobMetaData metaData = nextData.props().pageProps().metaData();
        BulldogJobOffer job = nextData.props().pageProps().data().job();

        return JobOffer.builder()
                .taskId(taskId)
                .source("BulldogJob")
                .id(nextData.id())
                .url(metaData.canonicalUrl())
                .jobTitle(metaData.title())
                .companyName(job.company().name())
                .jobDescription(HtmlCleaner.cleanHtml(job.details()))
                .requirements(HtmlCleaner.cleanHtml(job.requirements()))
                .employmentType(wrapNullableToList(job.employmentType()))
                .workModes(job.workModes())
                .experienceLevel(wrapNullableToList(job.experienceLevel()))
                .requiredSkills(job.technologyTags())
                .cities(mapCities(job.locations()))
                .build();
    }

    private List<String> mapCities(List<BulldogJobJobLocation> jobJobLocations){
        if (jobJobLocations == null) {
            return List.of();
        }

        return jobJobLocations.stream()
                .map(j -> j.location().cityEn())
                .toList();
    }
}