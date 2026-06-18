package com.mellinnial.plance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NEXTJS_NOTIF_URL = "http://localhost:3000/api/notifications/send";
    private static final String NEXTJS_EMAIL_URL = "http://localhost:3000/api/email/send";

    public void sendSseNotification(String title, String description, String urgency) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("description", description);
            payload.put("urgency", urgency);

            restTemplate.postForObject(NEXTJS_NOTIF_URL, payload, Map.class);
            log.info("Successfully dispatched SSE notification: {}", title);
        } catch (Exception e) {
            log.warn("Could not dispatch SSE notification to Next.js: {}", e.getMessage());
        }
    }

    public void sendEmail(String to, String subject, String html) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", to);
            payload.put("subject", subject);
            payload.put("html", html);

            restTemplate.postForObject(NEXTJS_EMAIL_URL, payload, Map.class);
            log.info("Successfully dispatched email to: {}", to);
        } catch (Exception e) {
            log.warn("Could not dispatch email notification to Next.js: {}", e.getMessage());
        }
    }
}
