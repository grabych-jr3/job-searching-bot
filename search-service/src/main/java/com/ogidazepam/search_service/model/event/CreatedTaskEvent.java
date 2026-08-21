package com.ogidazepam.search_service.model.event;

import com.ogidazepam.search_service.model.request.AnalyzeRequest;
import lombok.Builder;

@Builder
public record CreatedTaskEvent(
        String taskId,
        String username,
        AnalyzeRequest analyzeRequest
) {
}
