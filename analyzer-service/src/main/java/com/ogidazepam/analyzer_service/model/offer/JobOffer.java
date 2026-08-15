package com.ogidazepam.analyzer_service.model.offer;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record JobOffer(
        String taskId,

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