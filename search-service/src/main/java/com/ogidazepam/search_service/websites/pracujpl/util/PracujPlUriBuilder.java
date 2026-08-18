package com.ogidazepam.search_service.websites.pracujpl.util;

import com.ogidazepam.search_service.model.request.AnalyzeRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PracujPlUriBuilder {

    private final String baseUrl = "https://it.pracuj.pl/praca";

    public String buildUri(AnalyzeRequest request){
        return baseUrl +
                buildWorkModes(request.workMode()) +
                buildExperienceLevel(request.experience()) +
                "&" +
                buildCategoryParam(request.technology());
    }

    private String buildCategoryParam(String technology){
        return "itth=" + PracujPlUriParamsValues.technologyMap().get(technology);
    }

    private String buildWorkModes(List<String> workModes){
        if (workModes == null || workModes.isEmpty()){
            return "?";
        }

        if (workModes.size() == 1){
            Map<String, String> workModesMap = PracujPlUriParamsValues.workModesMapOnlyOne();
            return workModesMap.get(workModes.getFirst());
        }

        Map<String, String> workModesMap = PracujPlUriParamsValues.workModesMapAtLeastTwo();

        String result = workModes.stream()
                .map(workModesMap::get)
                .collect(Collectors.joining(","));

        return "?wm=" + result + "&";
    }

    private String buildExperienceLevel(List<String> experience){
        Map<String, String> experienceMap = PracujPlUriParamsValues.experienceLevelMap();

        String result = experience.stream()
                .map(experienceMap::get)
                .collect(Collectors.joining(","));

        return "et=" + result;
    }
}
