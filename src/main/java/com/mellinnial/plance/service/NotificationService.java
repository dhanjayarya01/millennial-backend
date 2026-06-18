package com.mellinnial.plance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RedisProducer redisProducer;

    private static final String NOTIFICATIONS_QUEUE = "millennial:notifications";
    private static final String EMAILS_QUEUE = "millennial:emails";

    public void sendSseNotification(Long userId, Long taskId, String type, String title, String description) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("taskId", taskId);
            payload.put("type", type);
            payload.put("title", title);
            payload.put("message", description);

            redisProducer.pushToQueue(NOTIFICATIONS_QUEUE, payload);
            log.info("Successfully queued SSE notification to Redis for userId {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Could not queue SSE notification to Redis: {}", e.getMessage());
        }
    }

    public void sendSseNotification(String title, String description, String urgency) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("message", description);
            payload.put("type", urgency.toUpperCase());

            redisProducer.pushToQueue(NOTIFICATIONS_QUEUE, payload);
            log.info("Successfully queued general SSE notification to Redis: {}", title);
        } catch (Exception e) {
            log.error("Could not queue general SSE notification to Redis: {}", e.getMessage());
        }
    }

    public void sendEmail(String to, String subject, String html) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", to);
            payload.put("subject", subject);
            payload.put("html", html);

            redisProducer.pushToQueue(EMAILS_QUEUE, payload);
            log.info("Successfully queued email to Redis for {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Could not queue email to Redis: {}", e.getMessage());
        }
    }
}
