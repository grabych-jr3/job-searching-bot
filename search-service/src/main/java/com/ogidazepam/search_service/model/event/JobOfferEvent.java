package com.ogidazepam.search_service.model.event;

import com.ogidazepam.search_service.model.JobOffer;

public record JobOfferEvent(
        String taskId,
        Long customerId,
        String cvHash,
        JobOffer offer,
        EventType type
) {
    public enum EventType {
        OFFER,
        SEARCH_FINISHED
    }

    public static JobOfferEvent offer(String taskId, Long customerId, String cvHash, JobOffer offer){
        return new JobOfferEvent(taskId, customerId, cvHash, offer, EventType.OFFER);
    }

    public static JobOfferEvent finishedOffer(String taskId, Long customerId, String cvHash){
        return new JobOfferEvent(taskId, customerId, cvHash, null, EventType.SEARCH_FINISHED);
    }
}
