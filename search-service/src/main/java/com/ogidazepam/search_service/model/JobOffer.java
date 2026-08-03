package com.ogidazepam.search_service.model;

import lombok.Builder;

import java.util.List;

@Builder
public record JobOffer(
        String url,
        String jobTitle,

        String companyName,
        String jobDescription,
        String requirements,
        String employmentType,
        String position,
        Boolean remote,
        List<String> workModes,

        String experienceLevel,
        Integer experienceInYears,
        List<String> technologyTags,

        String country,
        List<String> cities
) {
}
