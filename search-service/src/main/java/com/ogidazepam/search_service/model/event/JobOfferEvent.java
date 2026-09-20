package com.ogidazepam.search_service.model.event;

import com.ogidazepam.search_service.model.JobOffer;

public record JobOfferEvent(
        String taskId,
        String cvHash,
        Long customerId,
        JobOffer offer,
        EventType type
) {
    public enum EventType {
        OFFER,
        SEARCH_FINISHED
    }

    public static JobOfferEvent offer(String taskId, String cvHash, Long customerId, JobOffer offer){
        return new JobOfferEvent(taskId, cvHash, customerId, offer, EventType.OFFER);
    }

    public static JobOfferEvent finishedOffer(String taskId, String cvHash, Long customerId){
        return new JobOfferEvent(taskId, cvHash, customerId, null, EventType.SEARCH_FINISHED);
    }
}
