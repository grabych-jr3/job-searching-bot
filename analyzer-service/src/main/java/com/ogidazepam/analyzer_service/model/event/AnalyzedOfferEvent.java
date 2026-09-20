package com.ogidazepam.analyzer_service.model.event;

import com.ogidazepam.analyzer_service.model.OfferResult;

public record AnalyzedOfferEvent(
        String taskId,
        String cvHash,
        Long customerId,
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

    public static AnalyzedOfferEvent offerResult(String taskId, String cvHash, Long customerId, OfferResult offerResult, Boolean isNew){
        return new AnalyzedOfferEvent(taskId, cvHash, customerId, offerResult, null, EventType.OFFER, isNew);
    }

    public static AnalyzedOfferEvent finished(String taskId, Long customerId){
        return new AnalyzedOfferEvent(taskId, null, customerId, null, null, EventType.ANALYSIS_FINISHED, null);
    }

    public static AnalyzedOfferEvent failed(String taskId, Long customerId, String errorMessage){
        return new AnalyzedOfferEvent(taskId, null, customerId, null, errorMessage, EventType.ANALYSIS_FAILED, null);
    }
}
