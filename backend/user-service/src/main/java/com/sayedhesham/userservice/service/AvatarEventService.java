package com.sayedhesham.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarEventService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.user.avatar.upload}")
    private String avatarUploadTopic;

    @Value("${kafka.topic.user.avatar.update}")
    private String avatarUpdateTopic;

    @Value("${kafka.topic.user.avatar.delete}")
    private String avatarDeleteTopic;

    public void publishAvatarUploadEvent(String userId, String avatarData, String contentType) {
        try {
            AvatarUploadEvent event = AvatarUploadEvent.builder()
                    .userId(userId)
                    .avatarData(avatarData)
                    .contentType(contentType)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(avatarUploadTopic, userId, eventJson);
            log.info("Published avatar upload event for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing avatar upload event for user: {}", userId, e);
            throw new RuntimeException("Failed to publish avatar upload event", e);
        }
    }

    public void publishAvatarUpdateEvent(String userId, String avatarData, String contentType) {
        try {
            AvatarUploadEvent event = AvatarUploadEvent.builder()
                    .userId(userId)
                    .avatarData(avatarData)
                    .contentType(contentType)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(avatarUpdateTopic, userId, eventJson);
            log.info("Published avatar update event for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing avatar update event for user: {}", userId, e);
            throw new RuntimeException("Failed to publish avatar update event", e);
        }
    }

    public void publishAvatarDeleteEvent(String userId) {
        try {
            AvatarDeleteEvent event = AvatarDeleteEvent.builder()
                    .userId(userId)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(avatarDeleteTopic, userId, eventJson);
            log.info("Published avatar delete event for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing avatar delete event for user: {}", userId, e);
            throw new RuntimeException("Failed to publish avatar delete event", e);
        }
    }

    @Data
    @Builder
    public static class AvatarUploadEvent {
        private String userId;
        private String avatarData;
        private String contentType;
        private Long timestamp;
    }

    @Data
    @Builder
    public static class AvatarDeleteEvent {
        private String userId;
        private Long timestamp;
    }
}