package com.ogidazepam.search_service.websites.bulldogJob.client;

import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobNextData;
import com.ogidazepam.search_service.client.JobHttpClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class BulldogJobClient {

    private static final String JOBS_URL = "https://bulldogjob.pl/companies/jobs/s/skills,Java";
    private static final String JOB_URL = "https://bulldogjob.pl/companies/jobs/";

    private final ObjectMapper objectMapper;
    private final JobHttpClient jobHttpClient;

    public BulldogJobClient(ObjectMapper objectMapper, JobHttpClient jobHttpClient) {
        this.objectMapper = objectMapper;
        this.jobHttpClient = jobHttpClient;
    }

    public List<BulldogJobNextData> fetchJobOffers(){
        List<String> ids = fetchJobOfferIds();

        List<BulldogJobNextData> jobOffers = new ArrayList<>();
        for(String id : ids){
            jobOffers.add(extractJob(id));
        }
        return jobOffers;
    }

    private List<String> fetchJobOfferIds(){
        JsonNode jobs = extractJobs();

        List<String> ids = new ArrayList<>();
        for (JsonNode job : jobs){
            ids.add(job.path("id").stringValue());
        }
        return ids;
    }

    private JsonNode extractJobs(){
        return jobHttpClient.extractNextDataWithRetry(JOBS_URL)
                .path("props")
                .path("pageProps")
                .path("jobs");
    }

    private BulldogJobNextData extractJob(String id){
        return objectMapper.treeToValue(
                jobHttpClient.extractNextDataWithRetry(JOB_URL + id),
                BulldogJobNextData.class
        );
    }
}
