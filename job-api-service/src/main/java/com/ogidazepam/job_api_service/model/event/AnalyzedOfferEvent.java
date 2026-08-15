package com.ogidazepam.job_api_service.model.event;

import com.ogidazepam.job_api_service.model.OfferResult;

public record AnalyzedOfferEvent(
        String taskId,
        OfferResult offerResult,
        EventType type
) {
    public enum EventType {
        OFFER,
        ANALYSIS_FINISHED
    }

    public static AnalyzedOfferEvent offerResult(String taskId, OfferResult offerResult){
        return new AnalyzedOfferEvent(taskId, offerResult, EventType.OFFER);
    }

    public static AnalyzedOfferEvent finished(String taskId){
        return new AnalyzedOfferEvent(taskId, null, EventType.ANALYSIS_FINISHED);
    }
}