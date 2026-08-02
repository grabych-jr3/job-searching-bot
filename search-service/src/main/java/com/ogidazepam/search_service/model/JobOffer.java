package com.ogidazepam.search_service.model;

import lombok.Builder;

import java.util.List;

@Builder
public record JobOffer(
        String jobDescription,
        String employmentType,
        String experienceLevel,
        String position,
        boolean remote,
        String requirements,
        List<String> technologyTags,
        List<String> cities
) {
}
