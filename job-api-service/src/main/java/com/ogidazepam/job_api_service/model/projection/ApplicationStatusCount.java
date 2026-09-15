package com.ogidazepam.job_api_service.model.projection;

import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;

public interface ApplicationStatusCount {
    ApplicationStatus getStatus();
    Long getCount();
}
