package com.ogidazepam.analyzer_service.model.candidate;

import lombok.Builder;

@Builder
public record CandidateCertificate(
        String name,
        String organization
) {
}
