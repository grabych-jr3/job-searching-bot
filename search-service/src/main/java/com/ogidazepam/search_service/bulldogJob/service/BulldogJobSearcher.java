package com.ogidazepam.search_service.bulldogJob.service;

import com.ogidazepam.search_service.bulldogJob.model.BulldogJobOffer;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BulldogJobSearcher implements JobSearcher {

    private final ObjectMapper objectMapper;

    public BulldogJobSearcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<JobOffer> search() {
        Document document = connectToPage("https://bulldogjob.pl/companies/jobs/s/skills,Java");

        Element element = document.getElementById("__NEXT_DATA__");
        JsonNode jobs = objectMapper.readTree(element.data())
                .path("props")
                .path("pageProps")
                .path("jobs");

        List<String> ids = new ArrayList<>();

        for (JsonNode job : jobs){
            ids.add(job.path("id").stringValue());
        }

        // Формуємо URL і об'єкт кожної вакансії
        List<BulldogJobOffer> jobOffers = new ArrayList<>();
        for (String id : ids){
            Document offer = connectToPage("https://bulldogjob.pl/companies/jobs/" + id);

            Element jobOfferElement = offer.getElementById("__NEXT_DATA__");
            JsonNode job = objectMapper.readTree(jobOfferElement.data())
                    .path("props")
                    .path("pageProps")
                    .path("data")
                    .path("job");

            BulldogJobOffer o = objectMapper.readValue(job.toString(), BulldogJobOffer.class);
            jobOffers.add(o);
        }

        return jobOffers.stream()
                .map(this::mapToJobOffer)
                .toList();
    }

    private Document connectToPage(String url){
        try {
          return Jsoup.connect(url).get();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private JobOffer mapToJobOffer(BulldogJobOffer bulldogJobOffer){
        List<String> cities = bulldogJobOffer.locations().stream()
                .map(j -> j.location().cityEn())
                .toList();

        return JobOffer.builder()
                .jobDescription(bulldogJobOffer.details())
                .employmentType(bulldogJobOffer.employmentType())
                .experienceLevel(bulldogJobOffer.experienceLevel())
                .position(bulldogJobOffer.position())
                .remote(bulldogJobOffer.remote())
                .requirements(bulldogJobOffer.requirements())
                .technologyTags(bulldogJobOffer.technologyTags())
                .cities(cities)
                .build();
    }
}
