package com.ogidazepam.job_api_service.model;

public record OfferResult(
        String jobTitle,
        String companyName,
        String url,
        int score,
        String reason
) {
}
