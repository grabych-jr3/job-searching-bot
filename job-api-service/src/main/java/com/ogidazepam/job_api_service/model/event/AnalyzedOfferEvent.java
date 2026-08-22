package com.ogidazepam.job_api_service.model.event;

import com.ogidazepam.job_api_service.model.OfferResult;

public record AnalyzedOfferEvent(
        String taskId,
        Long customerId,
        OfferResult offerResult,
        EventType type
) {
    public enum EventType {
        OFFER,
        ANALYSIS_FINISHED
    }

    public static AnalyzedOfferEvent offerResult(String taskId, Long customerId, OfferResult offerResult){
        return new AnalyzedOfferEvent(taskId, customerId, offerResult, EventType.OFFER);
    }

    public static AnalyzedOfferEvent finished(String taskId, Long customerId){
        return new AnalyzedOfferEvent(taskId, customerId, null, EventType.ANALYSIS_FINISHED);
    }
}