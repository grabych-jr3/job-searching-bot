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

    private static final String JOBS_URL = "https://bulldogjob.pl/companies/jobs/s/skills,Java";
    private static final String JOB_URL = "https://bulldogjob.pl/companies/jobs/";
    private static final String SOURCE = "BulldogJob";

    private final ObjectMapper objectMapper;
    private final JobHttpClient jobHttpClient;
    private final RedisTemplate<String, Boolean> redisTemplate;

    public BulldogJobClient(ObjectMapper objectMapper, JobHttpClient jobHttpClient, RedisTemplate<String, Boolean> redisTemplate) {
        this.objectMapper = objectMapper;
        this.jobHttpClient = jobHttpClient;
        this.redisTemplate = redisTemplate;
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
            String id = job.path("id").stringValue();
            String key = "processed_offer:" + SOURCE + ":" + id;

            if (redisTemplate.hasKey(key)) {
                continue;
            }
            ids.add(id);
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
        BulldogJobProps props = objectMapper.treeToValue(
                jobHttpClient.extractNextDataWithRetry(JOB_URL + id)
                        .path("props"),
                BulldogJobProps.class
        );

        return new BulldogJobNextData(id, props);
    }
}
