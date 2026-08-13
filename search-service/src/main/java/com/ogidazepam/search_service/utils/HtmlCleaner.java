package com.ogidazepam.search_service.utils;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class HtmlCleaner {

    public JsonNode cleanHtml(JsonNode node){
        if (node.isObject()) {
            node.properties().forEach(entry -> {
                JsonNode value = entry.getValue();

                if (value.isString()) {
                    ((ObjectNode) node).put(
                            entry.getKey(),
                            Jsoup.parse(value.asString()).text()
                    );
                } else {
                    cleanHtml(value);
                }
            });

        } else if (node.isArray()) {
            for (JsonNode child : node) {
                cleanHtml(child);
            }
        }
        return node;
    }
}
