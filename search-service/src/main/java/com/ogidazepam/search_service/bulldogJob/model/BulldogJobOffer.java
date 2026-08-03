package com.ogidazepam.search_service.bulldogJob.model;

import java.time.OffsetDateTime;
import java.util.List;

public record BulldogJobOffer(
        String details,
        String employmentType,
        OffsetDateTime endsAt,
        String experienceLevel,
        Integer minExperienceInYears,
        String position,
        Boolean remote,
        String requirements,
        List<String> technologyTags,
        List<String> workModes,
        BulldogJobCompany company,
        List<JobLocation> locations
) {
}
