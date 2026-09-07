package com.ogidazepam.job_api_service.model.event;

import com.ogidazepam.job_api_service.model.OfferResult;

public record AnalyzedOfferEvent(
        String taskId,
        String cvHash,
        OfferResult offerResult,
        String errorMessage,
        EventType type,
        Boolean isNew
) {
    public enum EventType {
        OFFER,
        ANALYSIS_FINISHED,
        ANALYSIS_FAILED
    }
}