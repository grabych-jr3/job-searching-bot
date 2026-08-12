package com.ogidazepam.analyzer_service.model.offer;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JobOffer(
        String url,
        String jobTitle,

        String companyName,
        String jobDescription,
        String requirements,
        List<String> employmentType,
        List<String> position,
        Boolean remote,
        List<String> workModes,

        String experienceLevel,
        Integer experienceInYears,
        List<String> requiredSkills,
        List<String> niceToHaveSkills,
        List<String> languages,

        List<String> country,
        List<String> cities,

        OffsetDateTime expiresAt
) {
}