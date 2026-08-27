package com.ogidazepam.job_api_service.util.redis;

import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.job_api_service.service.SSENotificationService;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RedisSseSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SSENotificationService sseNotificationService;

    public RedisSseSubscriber(ObjectMapper objectMapper, SSENotificationService sseNotificationService) {
        this.objectMapper = objectMapper;
        this.sseNotificationService = sseNotificationService;
    }

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        AnalyzedOfferEvent event = objectMapper.readValue(message.getBody(), AnalyzedOfferEvent.class);
        String taskId = event.taskId();

        switch (event.type()) {
            case OFFER -> sseNotificationService.sendOffer(taskId, event.offerResult());
            case ANALYSIS_FINISHED -> sseNotificationService.sendCompletion(taskId);
            case ANALYSIS_FAILED -> sseNotificationService.sendFailure(taskId, event.errorMessage());
        }
    }
}
