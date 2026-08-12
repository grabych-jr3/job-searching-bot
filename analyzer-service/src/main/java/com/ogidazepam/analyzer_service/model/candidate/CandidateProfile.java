package com.ogidazepam.analyzer_service.model.candidate;

import lombok.Builder;

import java.security.cert.Certificate;
import java.util.List;

@Builder
public record CandidateProfile(
        String fullName,
        String aboutMe,
        List<String> skills,
        List<CandidateProject> projects,
        List<CandidateEducation> education,
        List<Certificate> certificates,
        List<CandidateLanguage> languages
) {
}
