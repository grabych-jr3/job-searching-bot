package com.ogidazepam.search_service.websites.justjoinit.util;

import java.util.HashMap;
import java.util.Map;

public class JustJoinItUriParamsValues {

    public static Map<String, String> technologyMap(){
        Map<String, String> technologyMap = new HashMap<>();
        technologyMap.put("java", "java");
        technologyMap.put("javascript", "javascript");
        technologyMap.put("python", "python");
        technologyMap.put("c", "c");
        technologyMap.put("net", "net");
        technologyMap.put("go", "go");
        technologyMap.put("php", "php");

        return technologyMap;
    }

    public static Map<String, String> experienceLevelMap(){
        Map<String, String> experienceMap = new HashMap<>();
        experienceMap.put("intern", "intern");
        experienceMap.put("junior", "junior");
        experienceMap.put("mid", "mid");
        experienceMap.put("senior", "senior");

        return experienceMap;
    }

    public static Map<String, String> workModesMap(){
        Map<String, String> workModesMap = new HashMap<>();
        workModesMap.put("full-office", "office");
        workModesMap.put("hybrid", "hybrid");
        workModesMap.put("remote", "remote");

        return workModesMap;
    }
}
