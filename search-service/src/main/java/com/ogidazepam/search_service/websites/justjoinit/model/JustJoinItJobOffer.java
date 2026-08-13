package com.ogidazepam.search_service.websites.justjoinit.model;


import java.util.List;

public record JustJoinItJobOffer(
        String slug,
        String title,
        String workplaceType,
        String workingTime,
        String experienceLevel,
        String companyName,
        List<JustJoinItJobLocation> locations,
        List<JustJoinItJobRequiredSkill> requiredSkills,
        List<JustJoinItJobNiceToHaveSkill> niceToHaveSkills
) {
}