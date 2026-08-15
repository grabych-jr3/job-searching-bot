package com.ogidazepam.search_service.strategy;

import com.ogidazepam.search_service.model.JobOffer;

import java.util.List;

public interface JobSearcher {

    List<JobOffer> search(String task_id);
}
