package com.ogidazepam.search_service.websites.justjoinit.mapper;

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
    public JobOffer mapToJobOffer(JustJoinItJobData job) {
        JustJoinItJobOffer jobOffer = job.jobOffer();
        JustJoinItJobDetails jobDetails = job.jobDetails();

        return JobOffer.builder()
                .url(JOB_URL + jobOffer.getSlug())
                .jobTitle(jobOffer.getTitle())
                .companyName(jobOffer.getCompanyName())
                .jobDescription(jobDetails.body())
                .employmentType(List.of(jobOffer.getWorkingTime()))
                .remote(jobOffer.getWorkplaceType().equals("remote"))
                .workModes(List.of(jobOffer.getWorkplaceType()))
                .experienceLevel(jobOffer.getExperienceLevel())
                .requiredSkills(mapRequiredSkills(jobOffer.getRequiredSkills()))
                .niceToHaveSkills(mapNiceToHaveSkills(jobOffer.getNiceToHaveSkills()))
                .languages(mapLanguages(jobOffer.getLanguages()))
                .cities(mapCities(jobOffer.getLocations()))
                .expiresAt(jobOffer.getExpiredAt())
                .build();
    }

    private List<String> mapRequiredSkills(List<JustJoinItJobRequiredSkill> requiredSkills){
        return requiredSkills
                .stream()
                .map(JustJoinItJobRequiredSkill::name)
                .toList();
    }

    private List<String> mapNiceToHaveSkills(List<JustJoinItJobNiceToHaveSkill> niceToHaveSkills){
        return niceToHaveSkills
                .stream()
                .map(JustJoinItJobNiceToHaveSkill::name)
                .toList();
    }

    private List<String> mapLanguages(List<JustJoinItJobLanguages> languages){
        return languages
                .stream()
                .map(JustJoinItJobLanguages::code)
                .toList();
    }

    private List<String> mapCities(List<JustJoinItJobLocation> locations){
        return locations
                .stream()
                .map(JustJoinItJobLocation::city)
                .toList();
    }
}
