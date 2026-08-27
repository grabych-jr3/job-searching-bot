package com.ogidazepam.search_service.mapper;

import com.ogidazepam.search_service.model.JobOffer;

import java.util.List;

public interface JobOfferMapper<T> {
    JobOffer mapToJobOffer(T job, String taskId);

    default <E> List<E> wrapNullableToList(E item) {
        return item != null ? List.of(item) : List.of();
    }
}
