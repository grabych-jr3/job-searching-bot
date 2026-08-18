package com.ogidazepam.search_service.websites.bulldogJob.util;

import com.ogidazepam.search_service.model.request.AnalyzeRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BulldogJobUriBuilder {

    private final String baseUrl = "https://bulldogjob.pl/companies/jobs/s/";

    public String buildUri(AnalyzeRequest request){
        return baseUrl +
                buildExperienceLevel(request.experience()) +
                "/" +
                buildCategory(request.technology());

    }

    public String buildExperienceLevel(List<String> experience){
        Map<String, String> experienceMap = BulldogJobUriParamsValues.experienceLevelMap();

        String result = experience.stream()
                .map(experienceMap::get)
                .collect(Collectors.joining(","));

        return "experienceLevel," + result;
    }

    public String buildCategory(String technology){
        return "skills," + BulldogJobUriParamsValues.technologyMap().get(technology);
    }
}
