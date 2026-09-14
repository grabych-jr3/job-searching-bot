package com.ogidazepam.job_api_service.model.response;

import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApplyResponse(
        Long id,
        String offerUrl,
        String jobTitle,
        String companyName,
        String notes,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {
}
