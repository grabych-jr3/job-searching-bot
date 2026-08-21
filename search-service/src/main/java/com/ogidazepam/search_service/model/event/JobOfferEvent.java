package com.ogidazepam.search_service.model.event;

import com.ogidazepam.search_service.model.JobOffer;

public record JobOfferEvent(
        String taskId,
        String username,
        JobOffer offer,
        EventType type
) {
    public enum EventType {
        OFFER,
        SEARCH_FINISHED
    }

    public static JobOfferEvent offer(String taskId, String username, JobOffer offer){
        return new JobOfferEvent(taskId, username, offer, EventType.OFFER);
    }

    public static JobOfferEvent finishedOffer(String taskId, String username){
        return new JobOfferEvent(taskId, username, null, EventType.SEARCH_FINISHED);
    }
}
