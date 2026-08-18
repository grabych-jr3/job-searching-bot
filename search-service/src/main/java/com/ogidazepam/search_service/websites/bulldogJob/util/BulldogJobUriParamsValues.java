package com.ogidazepam.search_service.websites.bulldogJob.util;

import java.util.HashMap;
import java.util.Map;

public class BulldogJobUriParamsValues {

    public static Map<String, String> technologyMap(){
        Map<String, String> technologyMap = new HashMap<>();
        technologyMap.put("java", "java");
        technologyMap.put("javascript", "javascript");
        technologyMap.put("python", "python");
        technologyMap.put("c", "c++");
        technologyMap.put("net", "net");
        technologyMap.put("go", "go");
        technologyMap.put("php", "php");

        return technologyMap;
    }

    public static Map<String, String> experienceLevelMap(){
        Map<String, String> experienceMap = new HashMap<>();
        experienceMap.put("intern", "intern");
        experienceMap.put("junior", "junior");
        experienceMap.put("mid", "medium");
        experienceMap.put("senior", "senior");

        return experienceMap;
    }
}
