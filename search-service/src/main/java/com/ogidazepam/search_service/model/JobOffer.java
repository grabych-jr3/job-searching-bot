package com.ogidazepam.search_service.model;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JobOffer(
        String source,
        String id,

        String url,
        String jobTitle,

        String companyName,
        String jobDescription,
        String requirements,
        List<String> employmentType,
        List<String> workModes,

        List<String> experienceLevel,
        List<String> requiredSkills,
        List<String> niceToHaveSkills,

        List<String> cities
) {
}
