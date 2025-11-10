package com.sayedhesham.mediaservice.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sayedhesham.mediaservice.model.Media;
import com.sayedhesham.mediaservice.repository.MediaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarProcessingService {

    private final MediaRepository mediaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.media-uploaded}")
    private String mediaUploadedTopic;

    @KafkaListener(topics = "${kafka.topic.user.avatar.upload}", groupId = "mediaservice-group")
    public void handleAvatarUpload(String eventJson) {
        try {
            AvatarUploadEvent event = objectMapper.readValue(eventJson, AvatarUploadEvent.class);
            log.info("Processing avatar upload for user: {}", event.getUserId());

            String mediaId = processAvatarData(event.getUserId(), event.getAvatarData(), event.getContentType());

            // Update user's avatarMediaId (this would typically be done via User Service API)
            publishMediaProcessedEvent(event.getUserId(), mediaId, "avatar", "uploaded");

            log.info("Successfully processed avatar for user: {}, mediaId: {}", event.getUserId(), mediaId);
        } catch (JsonProcessingException e) {
            log.error("Error parsing avatar upload event: {}", eventJson, e);
        } catch (IOException e) {
            log.error("Error processing avatar upload", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.user.avatar.update}", groupId = "mediaservice-group")
    public void handleAvatarUpdate(String eventJson) {
        try {
            AvatarUploadEvent event = objectMapper.readValue(eventJson, AvatarUploadEvent.class);
            log.info("Processing avatar update for user: {}", event.getUserId());

            String mediaId = processAvatarData(event.getUserId(), event.getAvatarData(), event.getContentType());

            publishMediaProcessedEvent(event.getUserId(), mediaId, "avatar", "updated");

            log.info("Successfully updated avatar for user: {}, mediaId: {}", event.getUserId(), mediaId);
        } catch (JsonProcessingException e) {
            log.error("Error parsing avatar update event: {}", eventJson, e);
        } catch (IOException e) {
            log.error("Error processing avatar update", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.user.avatar.delete}", groupId = "mediaservice-group")
    public void handleAvatarDelete(String eventJson) {
        try {
            AvatarDeleteEvent event = objectMapper.readValue(eventJson, AvatarDeleteEvent.class);
            log.info("Processing avatar deletion for user: {}", event.getUserId());

            // Find and delete user's avatar media
            // This would require knowing the user's current avatarMediaId
            // For now, we'll publish a deletion event
            publishMediaProcessedEvent(event.getUserId(), null, "avatar", "deleted");

            log.info("Successfully processed avatar deletion for user: {}", event.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Error parsing avatar delete event: {}", eventJson, e);
        } catch (Exception e) {
            log.error("Error processing avatar deletion", e);
        }
    }

    private String processAvatarData(String userId, String base64Data, String contentType) throws IOException {
        // Save media record to database with base64 data
        Media media = Media.builder()
                .id(UUID.randomUUID().toString())
                .base64Data(base64Data)
                .contentType(contentType)
                .mediaType("avatar")
                .ownerId(userId)
                .uploadTimestamp(System.currentTimeMillis())
                .build();

        return mediaRepository.save(media).getId();
    }

    private void publishMediaProcessedEvent(String userId, String mediaId, String mediaType, String action) {
        try {
            MediaProcessedEvent event = MediaProcessedEvent.builder()
                    .userId(userId)
                    .mediaId(mediaId)
                    .mediaType(mediaType)
                    .action(action)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(mediaUploadedTopic, userId, eventJson);
            log.info("Published media processed event for user: {}, action: {}", userId, action);
        } catch (JsonProcessingException e) {
            log.error("Error publishing media processed event for user: {}", userId, e);
        }
    }

    // Event classes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvatarUploadEvent {
        private String userId;
        private String avatarData;
        private String contentType;
        private Long timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvatarDeleteEvent {
        private String userId;
        private Long timestamp;
    }

    @lombok.Data
    @lombok.Builder
    public static class MediaProcessedEvent {

        private String userId;
        private String mediaId;
        private String mediaType;
        private String action;
        private Long timestamp;
    }
}
