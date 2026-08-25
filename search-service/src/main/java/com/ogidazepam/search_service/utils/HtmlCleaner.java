package com.ogidazepam.search_service.utils;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

public class HtmlCleaner {

    public static String cleanHtml(String html){
        if (html == null || html.isBlank()) return "";
        return Jsoup.parse(html).text();
    }
}
