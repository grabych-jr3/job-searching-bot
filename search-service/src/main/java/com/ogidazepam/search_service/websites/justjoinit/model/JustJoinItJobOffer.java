package com.ogidazepam.search_service.websites.justjoinit.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class JustJoinItJobOffer {

    private String url;
    private String slug;
    private String title;
    private String workplaceType;
    private String workingTime;
    private String experienceLevel;
    private String companyName;
    private List<JustJoinItJobLocation> locations;
    private List<JustJoinItJobRequiredSkill> requiredSkills;
    private List<JustJoinItJobNiceToHaveSkill> niceToHaveSkills;
    private List<JustJoinItJobLanguages> languages;
    private OffsetDateTime expiredAt;
}
