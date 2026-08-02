package com.ogidazepam.search_service.bulldogJob.model;

import java.util.List;

public record BulldogJobOffer(
        String details,
        String employmentType,
        String experienceLevel,
        String position,
        boolean remote,
        String requirements,
        List<String> technologyTags,
        List<JobLocation> locations
) {
}
