package com.ogidazepam.search_service.websites.justjoinit.mapper;

import com.ogidazepam.search_service.utils.HtmlCleaner;
import com.ogidazepam.search_service.websites.justjoinit.model.*;
import com.ogidazepam.search_service.mapper.JobOfferMapper;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.websites.justjoinit.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JustJoinItMapper implements JobOfferMapper<JustJoinItJobData> {
    private static final String JOB_URL = "https://justjoin.it/job-offer/";

    @Override
    public JobOffer mapToJobOffer(JustJoinItJobData job, String taskId) {
        JustJoinItJobOffer jobOffer = job.jobOffer();
        JustJoinItJobDetails jobDetails = job.jobDetails();

        return JobOffer.builder()
                .taskId(taskId)
                .source("JustJoinIt")
                .id(jobOffer.slug())
                .url(JOB_URL + jobOffer.slug())
                .jobTitle(jobOffer.title())
                .companyName(jobOffer.companyName())
                .jobDescription(HtmlCleaner.cleanHtml(jobDetails.body()))
                .employmentType(wrapNullableToList(jobOffer.workingTime()))
                .workModes(wrapNullableToList(jobOffer.workplaceType()))
                .experienceLevel(wrapNullableToList(jobOffer.experienceLevel()))
                .requiredSkills(mapRequiredSkills(jobOffer.requiredSkills()))
                .niceToHaveSkills(mapNiceToHaveSkills(jobOffer.niceToHaveSkills()))
                .cities(mapCities(jobOffer.locations()))
                .build();
    }

    private List<String> mapRequiredSkills(List<JustJoinItJobRequiredSkill> requiredSkills){
        if (requiredSkills == null){
            return List.of();
        }

        return requiredSkills
                .stream()
                .map(JustJoinItJobRequiredSkill::name)
                .toList();
    }

    private List<String> mapNiceToHaveSkills(List<JustJoinItJobNiceToHaveSkill> niceToHaveSkills){
        if (niceToHaveSkills == null){
            return List.of();
        }

        return niceToHaveSkills
                .stream()
                .map(JustJoinItJobNiceToHaveSkill::name)
                .toList();
    }

    private List<String> mapCities(List<JustJoinItJobLocation> locations){
        if (locations == null){
            return List.of();
        }

        return locations
                .stream()
                .map(JustJoinItJobLocation::city)
                .toList();
    }
}
