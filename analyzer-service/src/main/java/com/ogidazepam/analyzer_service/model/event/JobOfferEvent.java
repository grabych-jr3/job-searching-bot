package com.ogidazepam.analyzer_service.model.event;

import com.ogidazepam.analyzer_service.model.offer.JobOffer;

public record JobOfferEvent(
        String taskId,
        Long customerId,
        JobOffer offer,
        EventType type
) {
    public enum EventType {
        OFFER,
        SEARCH_FINISHED
    }

    public static JobOfferEvent offer(String taskId, Long customerId, JobOffer offer){
        return new JobOfferEvent(taskId, customerId, offer, EventType.OFFER);
    }

    public static JobOfferEvent finishedOffer(String taskId, Long customerId){
        return new JobOfferEvent(taskId, customerId, null, EventType.SEARCH_FINISHED);
    }
}
