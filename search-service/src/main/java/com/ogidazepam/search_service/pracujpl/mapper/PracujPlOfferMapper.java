package com.ogidazepam.search_service.pracujpl.mapper;

import com.ogidazepam.search_service.mapper.JobOfferMapper;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.pracujpl.model.offer.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PracujPlOfferMapper implements JobOfferMapper<PracujPlOfferData> {

    @Override
    public JobOffer mapToJobOffer(PracujPlOfferData data) {
        PracujPlOfferAttributes attributes = data.attributes();
        PracujPlOfferEmployment employment = data.attributes().employment();
        List<PracujPlOfferTextSection> textSections = data.textSections();

        return JobOffer.builder()
                .url(attributes.offerAbsoluteUrl())
                .jobTitle(attributes.jobTitle())
                .companyName(attributes.displayEmployerName())
                .jobDescription(mapJobDescription(textSections))
                .requirements(mapJobRequirements(textSections))
                .employmentType(mapJobEmploymentTypes(employment.workSchedules()))
                .position(mapJobPositions(employment.positionLevels()))
                .remote(employment.entirelyRemoteWork())
                .workModes(mapJobWorkModes(employment.workModes()))
                .requiredSkills(mapJobRequiredSkills(textSections))
                .niceToHaveSkills(mapJobOptionalSkills(textSections))
                .country(mapJobCountries(attributes.workplaces()))
                .cities(mapJobCities(attributes.workplaces()))
                .expiresAt(data.publicationDetails().expirationDateTimeUtc())
                .build();
    }

    private String mapJobDescription(List<PracujPlOfferTextSection> textSections){
        return textSections.stream()
                .filter(s ->
                        s.sectionType().equals("about-project") ||
                                s.sectionType().equals("responsibilities"))
                .flatMap(s -> s.textElements().stream())
                .collect(Collectors.joining("\n"));
    }

    private String mapJobRequirements(List<PracujPlOfferTextSection> textSections){
        return textSections.stream()
                .filter(s -> s.sectionType().equals("requirements-expected"))
                .flatMap(s -> s.textElements().stream())
                .collect(Collectors.joining("\n"));
    }

    private List<String> mapJobRequiredSkills(List<PracujPlOfferTextSection> textSections){
        return textSections.stream()
                .filter(s -> s.sectionType().equals("technologies-expected"))
                .flatMap(s -> s.textElements().stream())
                .toList();
    }

    private List<String> mapJobOptionalSkills(List<PracujPlOfferTextSection> textSections){
        return textSections.stream()
                .filter(s -> s.sectionType().equals("technologies-optional"))
                .flatMap(s -> s.textElements().stream())
                .toList();
    }

    private List<String> mapJobWorkModes(List<PracujPlOfferWorkMode> workModes){
        return workModes.stream()
                .map(PracujPlOfferWorkMode::name)
                .toList();
    }

    private List<String> mapJobEmploymentTypes(List<PracujPlOfferWorkSchedule> workSchedules){
        return workSchedules.stream()
                .map(PracujPlOfferWorkSchedule::name)
                .toList();
    }

    private List<String> mapJobPositions(List<PracujPlOfferPositionLevel> positionLevels){
        return positionLevels.stream()
                .map(PracujPlOfferPositionLevel::name)
                .toList();
    }

    private List<String> mapJobCities(List<PracujPlOfferWorkplace> workplaces){
        return workplaces.stream()
                .map(w -> w.inlandLocation().location().name())
                .toList();
    }

    private List<String> mapJobCountries(List<PracujPlOfferWorkplace> workplaces){
        return workplaces.stream()
                .map(w -> w.country().name())
                .toList();
    }
}
