package com.ogidazepam.analyzer_service.model.candidate;

import lombok.Builder;

import java.util.List;

@Builder
public record CandidateProfile(
        String fullName,
        String aboutMe,
        String location,
        List<String> skills,
        List<CandidateProject> projects,
        List<CandidateEducation> education,
        List<CandidateCertificate> certificates,
        List<CandidateLanguage> languages
) {
}
