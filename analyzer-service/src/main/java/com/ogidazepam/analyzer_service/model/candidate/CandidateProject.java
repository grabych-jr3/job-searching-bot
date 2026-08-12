package com.ogidazepam.analyzer_service.model.candidate;

import lombok.Builder;

import java.util.List;

@Builder
public record CandidateProject(
        String name,
        List<String> technologies,
        String description
) {
}
