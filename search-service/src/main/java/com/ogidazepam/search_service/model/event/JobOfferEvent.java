package com.ogidazepam.search_service.model.event;

import com.ogidazepam.search_service.model.JobOffer;

public record JobOfferEvent(
        String taskId,
        JobOffer offer,
        EventType type
) {
    public enum EventType {
        OFFER,
        SEARCH_FINISHED
    }

    public static JobOfferEvent offer(String taskId, JobOffer offer){
        return new JobOfferEvent(taskId, offer, EventType.OFFER);
    }

    public static JobOfferEvent finishedOffer(String taskId){
        return new JobOfferEvent(taskId, null, EventType.SEARCH_FINISHED);
    }
}
