package com.ogidazepam.job_api_service.model.event;

import com.ogidazepam.job_api_service.model.OfferResult;

public record AnalyzedOfferEvent(
        String taskId,
        String username,
        OfferResult offerResult,
        EventType type
) {
    public enum EventType {
        OFFER,
        ANALYSIS_FINISHED
    }

    public static AnalyzedOfferEvent offerResult(String taskId, String username, OfferResult offerResult){
        return new AnalyzedOfferEvent(taskId, username, offerResult, EventType.OFFER);
    }

    public static AnalyzedOfferEvent finished(String taskId, String username){
        return new AnalyzedOfferEvent(taskId, username, null, EventType.ANALYSIS_FINISHED);
    }
}