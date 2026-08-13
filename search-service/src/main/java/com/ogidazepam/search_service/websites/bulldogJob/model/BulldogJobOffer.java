package com.ogidazepam.search_service.websites.bulldogJob.model;

import java.util.List;

public record BulldogJobOffer(
        String details,
        String employmentType,
        String experienceLevel,
        String requirements,
        List<String> technologyTags,
        List<String> workModes,
        BulldogJobCompany company,
        List<BulldogJobJobLocation> locations
) {
}
