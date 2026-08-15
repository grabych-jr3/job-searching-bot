package com.ogidazepam.job_api_service.model;

public record OfferResult(
        String url,
        int score,
        String reason
) {
}
