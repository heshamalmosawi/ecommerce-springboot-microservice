package com.sayedhesham.userservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sayedhesham.userservice.model.User;
import com.sayedhesham.userservice.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaEventConsumerService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.media-uploaded}", groupId = "userservice-group")
    public void handleMediaProcessedEvent(String eventJson) {
        try {
            MediaProcessedEvent event = objectMapper.readValue(eventJson, MediaProcessedEvent.class);
            log.info("Processing media processed event for user: {}, action: {}", event.getUserId(), event.getAction());

            if ("avatar".equals(event.getMediaType())) {
                handleAvatarMediaEvent(event);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing media processed event: {}", eventJson, e);
        } catch (Exception e) {
            log.error("Error processing media processed event", e);
        }
    }

    private void handleAvatarMediaEvent(MediaProcessedEvent event) {
        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + event.getUserId()));

        switch (event.getAction()) {
            case "uploaded", "updated" -> {
                user.setAvatarMediaId(event.getMediaId());
                log.info("Updated user {} with avatar media ID: {}", event.getUserId(), event.getMediaId());
            }
            case "deleted" -> {
                user.setAvatarMediaId(null);
                log.info("Removed avatar media ID for user: {}", event.getUserId());
            }
            default -> {
                log.warn("Unknown avatar action: {}", event.getAction());
                return;
            }
        }

        userRepository.save(user);
        log.info("Successfully updated user {} avatar media ID", event.getUserId());
    }

    // Event class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MediaProcessedEvent {

        private String userId;
        private String mediaId;
        private String mediaType;
        private String action;
        private Long timestamp;
    }
}
