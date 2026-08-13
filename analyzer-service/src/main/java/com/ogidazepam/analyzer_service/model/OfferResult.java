package com.ogidazepam.analyzer_service.model;

public record OfferResult(
        String url,
        int score,
        String reason
) {
}
