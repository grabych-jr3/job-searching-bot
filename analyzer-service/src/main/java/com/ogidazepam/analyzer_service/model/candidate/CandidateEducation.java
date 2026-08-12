package com.ogidazepam.analyzer_service.model.candidate;

import lombok.Builder;

import java.time.YearMonth;

@Builder
public record CandidateEducation(
        String universityName,
        String major,
        String degree,
        YearMonth started,
        YearMonth finishedOrExpected
) {
}
