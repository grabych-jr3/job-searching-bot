package com.ogidazepam.job_api_service.model.event;

import com.ogidazepam.job_api_service.model.OfferResult;

public record AnalyzedOfferEvent(
        String taskId,
        String cvHash,
        OfferResult offerResult,
        String errorMessage,
        EventType type
) {
    public enum EventType {
        OFFER,
        ANALYSIS_FINISHED,
        ANALYSIS_FAILED
    }

    public static AnalyzedOfferEvent offerResult(String taskId, String cvHash, OfferResult offerResult){
        return new AnalyzedOfferEvent(taskId, cvHash, offerResult, null, EventType.OFFER);
    }

    public static AnalyzedOfferEvent finished(String taskId){
        return new AnalyzedOfferEvent(taskId, null, null, null, EventType.ANALYSIS_FINISHED);
    }

    public static AnalyzedOfferEvent failed(String taskId, String errorMessage){
        return new AnalyzedOfferEvent(taskId, null, null, errorMessage, EventType.ANALYSIS_FAILED);
    }
}