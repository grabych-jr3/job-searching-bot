package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.OfferResult;
import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SSENotificationService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String taskId){
        SseEmitter emitter = new SseEmitter(180000L);

        emitters.put(taskId, emitter);

        emitter.onCompletion(() -> emitters.remove(taskId));
        emitter.onTimeout(() -> emitters.remove(taskId));
        emitter.onError((e) -> emitters.remove(taskId));

        return emitter;
    }

    public void sendOffer(String taskId, OfferResult offerResult){
        SseEmitter emitter = emitters.get(taskId);
        if (emitter != null){
            try {
                emitter.send(SseEmitter.event()
                        .name("vacancy_analyzed")
                        .data(offerResult)
                        .build());
            } catch (IOException e){
                emitters.remove(taskId);
            }
        }
    }

    public void sendCompletion(String taskId){
        SseEmitter emitter = emitters.get(taskId);

        if (emitter != null){
            try {
                emitter.send(SseEmitter.event()
                        .name("task_completed")
                        .data("FINISHED")
                        .build());
            } catch (Exception e){

            }
            finally {
                emitters.remove(taskId);
            }
        }
    }
}
