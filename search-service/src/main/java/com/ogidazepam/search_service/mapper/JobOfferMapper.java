package com.ogidazepam.search_service.mapper;

import com.ogidazepam.search_service.model.JobOffer;

public interface JobOfferMapper<T> {
    JobOffer mapToJobOffer(T job, String taskId);
}
