package com.ogidazepam.job_api_service.model.event;

import lombok.Builder;

@Builder
public record CreatedTaskEvent(
        String taskId
) {
}
