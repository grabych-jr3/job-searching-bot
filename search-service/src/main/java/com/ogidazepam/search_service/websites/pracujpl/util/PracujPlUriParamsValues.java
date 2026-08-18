package com.ogidazepam.search_service.websites.pracujpl.util;

import java.util.HashMap;
import java.util.Map;

public class PracujPlUriParamsValues {

    public static Map<String, String> technologyMap(){
        Map<String, String> technologyMap = new HashMap<>();
        technologyMap.put("java", "38");
        technologyMap.put("javascript", "33");
        technologyMap.put("python", "37");
        technologyMap.put("c", "41,54");
        technologyMap.put("net", "75");
        technologyMap.put("go", "50");
        technologyMap.put("php", "40");

        return technologyMap;
    }

    public static Map<String, String> experienceLevelMap(){
        Map<String, String> experienceMap = new HashMap<>();
        experienceMap.put("intern", "1,3");
        experienceMap.put("junior", "17");
        experienceMap.put("mid", "4");
        experienceMap.put("senior", "18");

        return experienceMap;
    }

    public static Map<String, String> workModesMapAtLeastTwo(){
        Map<String, String> workModesMap = new HashMap<>();
        workModesMap.put("full-office", "full-office");
        workModesMap.put("hybrid", "hybrid");
        workModesMap.put("remote", "home-office");

        return workModesMap;
    }

    public static Map<String, String> workModesMapOnlyOne(){
        Map<String, String> workModesMap = new HashMap<>();
        workModesMap.put("full-office", "/praca stacjonarna;wm,full-office?");
        workModesMap.put("hybrid", "/praca hybrydowa;wm,hybrid?");
        workModesMap.put("remote", "/praca zdalna;wm,home-office?");

        return workModesMap;
    }
}
