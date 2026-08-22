package com.ogidazepam.search_service.websites.bulldogJob.client;

import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobNextData;
import com.ogidazepam.search_service.client.JobHttpClient;
import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobProps;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class BulldogJobClient {

    private static final String JOB_URL = "https://bulldogjob.pl/companies/jobs/";
    private static final String SOURCE = "BulldogJob";

    private final ObjectMapper objectMapper;
    private final JobHttpClient jobHttpClient;

    public BulldogJobClient(ObjectMapper objectMapper, JobHttpClient jobHttpClient) {
        this.objectMapper = objectMapper;
        this.jobHttpClient = jobHttpClient;
    }

    public BulldogJobNextData fetchJobOffer(String id){
        return extractJob(id);
    }

    public List<String> fetchJobOfferIds(String uri){
        JsonNode jobs = extractJobs(uri);

        List<String> ids = new ArrayList<>();
        for (JsonNode job : jobs){
            String id = job.path("id").stringValue();
            ids.add(id);
        }
        return ids;
    }

    private JsonNode extractJobs(String uri){
        return jobHttpClient.extractNextDataWithRetry(uri)
                .path("props")
                .path("pageProps")
                .path("jobs");
    }

    private BulldogJobNextData extractJob(String id){
        BulldogJobProps props = objectMapper.treeToValue(
                jobHttpClient.extractNextDataWithRetry(JOB_URL + id)
                        .path("props"),
                BulldogJobProps.class
        );

        return new BulldogJobNextData(id, props);
    }
}
