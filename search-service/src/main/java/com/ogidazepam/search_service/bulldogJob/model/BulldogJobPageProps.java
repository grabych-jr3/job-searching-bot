package com.ogidazepam.search_service.bulldogJob.model;

public record BulldogJobPageProps(
        String country,
        BulldogJobMetaData metaData,
        BulldogJobData data
) {
}
