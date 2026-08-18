package com.ogidazepam.analyzer_service.model;

public record OfferResult(
        String jobTitle,
        String url,
        int score,
        String reason
) {
}
