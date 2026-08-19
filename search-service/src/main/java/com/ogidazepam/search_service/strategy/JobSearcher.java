package com.ogidazepam.search_service.strategy;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.model.event.CreatedTaskEvent;

import java.util.List;
import java.util.function.Consumer;

public interface JobSearcher {

    void search(CreatedTaskEvent event, Consumer<JobOffer> onFoundJob);
}
