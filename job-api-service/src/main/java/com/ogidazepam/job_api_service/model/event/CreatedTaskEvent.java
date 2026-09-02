package com.ogidazepam.job_api_service.model.event;

import com.ogidazepam.job_api_service.model.request.AnalyzeRequest;
import lombok.Builder;

@Builder
public record CreatedTaskEvent(
        String taskId,
        String cvHash,
        AnalyzeRequest analyzeRequest
) {
}
