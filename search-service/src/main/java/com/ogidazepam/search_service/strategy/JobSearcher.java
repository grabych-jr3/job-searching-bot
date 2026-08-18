package com.ogidazepam.search_service.strategy;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.model.event.CreatedTaskEvent;

import java.util.List;

public interface JobSearcher {

    List<JobOffer> search(CreatedTaskEvent event);
}
