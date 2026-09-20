package com.ogidazepam.analyzer_service.model.event;

import com.ogidazepam.analyzer_service.model.offer.JobOffer;

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
}
