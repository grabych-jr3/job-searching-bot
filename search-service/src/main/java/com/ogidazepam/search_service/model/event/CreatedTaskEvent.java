package com.ogidazepam.search_service.model.event;

import lombok.Builder;

@Builder
public record CreatedTaskEvent(
        String status,
        String taskId
) {
}
