package com.ogidazepam.search_service.bulldogJob.client;

import com.ogidazepam.search_service.bulldogJob.model.BulldogJobNextData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BulldogJobClient {

    private static final String JOBS_URL = "https://bulldogjob.pl/companies/jobs/s/skills,Java";
    private static final String JOB_URL = "https://bulldogjob.pl/companies/jobs/";
    private static final String NEXT_DATA_SCRIPT_ID = "__NEXT_DATA__";

    private final ObjectMapper objectMapper;

    public BulldogJobClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<BulldogJobNextData> fetchJobOffers(){
        List<String> ids = fetchJobOfferIds();

        List<BulldogJobNextData> jobOffers = new ArrayList<>();
        for(String id : ids){
            BulldogJobNextData nextData = objectMapper.treeToValue(
                    extractNextData(JOB_URL + id),
                    BulldogJobNextData.class
            );

            jobOffers.add(nextData);
        }
        return jobOffers;
    }

    private List<String> fetchJobOfferIds(){
        JsonNode jobs = extractNextData(JOBS_URL)
                .path("props")
                .path("pageProps")
                .path("jobs");

        List<String> ids = new ArrayList<>();
        for (JsonNode job : jobs){
            ids.add(job.path("id").stringValue());
        }
        return ids;
    }

    private JsonNode extractNextData(String url){
        try {
            Document document = Jsoup.connect(url).get();
            Element element = document.getElementById(NEXT_DATA_SCRIPT_ID);

            if (element == null){
                throw new IllegalStateException("__NEXT_DATA__ not found");
            }

            return objectMapper.readTree(element.data());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
