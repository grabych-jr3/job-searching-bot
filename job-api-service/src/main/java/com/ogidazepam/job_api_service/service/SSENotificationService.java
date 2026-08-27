package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.OfferResult;
import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SSENotificationService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String taskId){
        SseEmitter emitter = new SseEmitter(180000L);

        emitters.put(taskId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE completion for taskId: {}", taskId);
            emitters.remove(taskId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE timeout for taskId: {}", taskId);
            emitter.complete();
            emitters.remove(taskId);
        });
        emitter.onError((e) -> {
            log.debug("SSE error for taskId: {}: {}", taskId, e.getMessage());
            emitters.remove(taskId);
        });

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
                log.debug("Failed to send vacancy_analyzed event for taskId: {}. Client likely disconnected.", taskId);
                emitter.completeWithError(e);
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

                emitter.complete();
            } catch (IOException e) {
                log.debug("Failed to send task_completed for taskId: {}", taskId);
                emitter.completeWithError(e);
            }
        }
    }

    public void sendFailure(String taskId, String errorMessage){
        SseEmitter emitter = emitters.get(taskId);
        if (emitter != null){
            try {
                emitter.send(SseEmitter.event()
                        .name("task_failed")
                        .data(errorMessage != null ? errorMessage : "Analysis failed")
                        .build()
                );

                emitter.complete();
            } catch (IOException e) {
                log.debug("Failed to send task_failed for taskId: {}", taskId);
                emitter.completeWithError(e);
            }
        }
    }
}
