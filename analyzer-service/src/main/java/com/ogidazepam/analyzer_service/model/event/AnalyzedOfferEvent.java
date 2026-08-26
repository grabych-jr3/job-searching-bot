package com.ogidazepam.analyzer_service.model.event;

import com.ogidazepam.analyzer_service.model.OfferResult;

public record AnalyzedOfferEvent(
        String taskId,
        Long customerId,
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

    public static AnalyzedOfferEvent offerResult(String taskId, Long customerId, String cvHash, OfferResult offerResult){
        return new AnalyzedOfferEvent(taskId, customerId, cvHash, offerResult, null, EventType.OFFER);
    }

    public static AnalyzedOfferEvent finished(String taskId, Long customerId){
        return new AnalyzedOfferEvent(taskId, customerId, null, null, null, EventType.ANALYSIS_FINISHED);
    }

    public static AnalyzedOfferEvent failed(String taskId, Long customerId, String errorMessage){
        return new AnalyzedOfferEvent(taskId, customerId, null, null, errorMessage, EventType.ANALYSIS_FAILED);
    }
}
