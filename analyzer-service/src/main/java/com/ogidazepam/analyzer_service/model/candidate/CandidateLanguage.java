package com.ogidazepam.analyzer_service.model.candidate;

import lombok.Builder;

@Builder
public record CandidateLanguage(
        String name,
        String level
) {
}
