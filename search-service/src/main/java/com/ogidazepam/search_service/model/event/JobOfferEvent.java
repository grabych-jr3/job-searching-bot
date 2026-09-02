package com.ogidazepam.search_service.model.event;

import com.ogidazepam.search_service.model.JobOffer;

public record JobOfferEvent(
        String taskId,
        String cvHash,
        JobOffer offer,
        EventType type
) {
    public enum EventType {
        OFFER,
        SEARCH_FINISHED
    }

    public static JobOfferEvent offer(String taskId, String cvHash, JobOffer offer){
        return new JobOfferEvent(taskId, cvHash, offer, EventType.OFFER);
    }

    public static JobOfferEvent finishedOffer(String taskId, String cvHash){
        return new JobOfferEvent(taskId, cvHash, null, EventType.SEARCH_FINISHED);
    }
}
