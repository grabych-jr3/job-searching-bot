package com.ogidazepam.analyzer_service.model;

public record OfferResult(
        String jobTitle,
        String companyName,
        String url,
        int score,
        String reason
) {
}
