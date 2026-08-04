package com.ogidazepam.search_service.justjoinit.service;

import com.ogidazepam.search_service.justjoinit.client.JustJoinItClient;
import com.ogidazepam.search_service.justjoinit.model.*;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JustJoinItJobSearcher implements JobSearcher {

    private static final String JOB_URL = "https://justjoin.it/job-offer/";

    private final JustJoinItClient justJoinItClient;

    public JustJoinItJobSearcher(JustJoinItClient justJoinItClient) {
        this.justJoinItClient = justJoinItClient;
    }

    @Override
    public List<JobOffer> search() {
        return justJoinItClient.fetchJobOffers().stream()
                .map(offer -> {
                    JustJoinItJobDetails jobDetails = justJoinItClient
                            .fetchJobOffersDetails(offer.getSlug());

                    return toJobOffer(offer, jobDetails);
                })
                .toList();
    }

    private JobOffer toJobOffer(JustJoinItJobOffer jobOffer, JustJoinItJobDetails jobDetails){

        List<String> requiredSkills = jobOffer.getRequiredSkills()
                .stream()
                .map(JustJoinItJobRequiredSkill::name)
                .toList();

        List<String> niceToHaveSkills = jobOffer.getNiceToHaveSkills()
                .stream()
                .map(JustJoinItJobNiceToHaveSkill::name)
                .toList();

        List<String> languages = jobOffer.getLanguages()
                .stream()
                .map(JustJoinItJobLanguages::code)
                .toList();

        List<String> cities = jobOffer.getLocations()
                .stream()
                .map(JustJoinItJobLocation::city)
                .toList();

        return JobOffer.builder()
                .url(JOB_URL + jobOffer.getSlug())
                .jobTitle(jobOffer.getTitle())
                .companyName(jobOffer.getCompanyName())
                .jobDescription(jobDetails.body())
                .employmentType(jobOffer.getWorkingTime())
                .remote(jobOffer.getWorkplaceType().equals("remote"))
                .workModes(List.of(jobOffer.getWorkplaceType()))
                .experienceLevel(jobOffer.getExperienceLevel())
                .requiredSkills(requiredSkills)
                .niceToHaveSkills(niceToHaveSkills)
                .languages(languages)
                .cities(cities)
                .expiresAt(jobOffer.getExpiredAt())
                .build();
    }
}
