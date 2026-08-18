package com.ogidazepam.search_service.websites.justjoinit.util;

import com.ogidazepam.search_service.model.request.AnalyzeRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class JustJoinItUriBuilder {

    private final String baseUrl = "https://justjoin.it/api/candidate-api/offers?";

    public String buildUri(AnalyzeRequest request){
        return baseUrl +
                buildCategoryParam(request.technology()) +
                buildWorkModes(request.workMode()) +
                buildExperienceLevel(request.experience()) +
                buildPagination();
    }

    private String buildCategoryParam(String technology){
        return "categories=" + JustJoinItUriParamsValues.technologyMap().get(technology) + "&";
    }

    private String buildExperienceLevel(List<String> experience){
        Map<String, String> experienceMap = JustJoinItUriParamsValues.experienceLevelMap();

        StringBuilder stringBuilder = new StringBuilder();

        for (String level : experience){
            stringBuilder
                    .append("experienceLevels=")
                    .append(experienceMap.get(level))
                    .append("&");
        }
        return stringBuilder.toString();
    }

    private String buildWorkModes(List<String> workModes){
        if (workModes == null || workModes.isEmpty()){
            return "";
        }

        Map<String, String> workModesMap = JustJoinItUriParamsValues.workModesMap();

        StringBuilder stringBuilder = new StringBuilder();

        for (String mode : workModes){
            stringBuilder
                    .append("remoteWorkOptions=")
                    .append(workModesMap.get(mode))
                    .append("&");
        }

        return stringBuilder.toString();
    }

    private String buildPagination(){
        return "sortBy=publishedAt&orderBy=descending&from=0&itemsCount=100";
    }
}
