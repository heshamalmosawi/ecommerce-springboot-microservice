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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageProcessingService {

    private static final String MEDIA_TYPE_PRODUCT_IMAGE = "product_image";

    private final MediaRepository mediaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.media-uploaded}")
    private String mediaUploadedTopic;

    @KafkaListener(topics = "${kafka.topic.product.image.upload}", groupId = "mediaservice-group")
    public void handleProductImageUpload(String eventJson) {
        try {
            ProductImageEvent event = objectMapper.readValue(eventJson, ProductImageEvent.class);
            log.info("Processing product image upload for product: {}", event.getProductId());

            String mediaId = processProductImageData(event.getProductId(), event.getImageData(), event.getContentType());

            publishMediaProcessedEvent(event.getProductId(), mediaId, MEDIA_TYPE_PRODUCT_IMAGE, "uploaded");

            log.info("Successfully processed product image for product: {}, mediaId: {}", event.getProductId(), mediaId);
        } catch (JsonProcessingException e) {
            log.error("Error parsing product image upload event: {}", eventJson, e);
        } catch (IOException e) {
            log.error("Error processing product image upload", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.product.image.update}", groupId = "mediaservice-group")
    public void handleProductImageUpdate(String eventJson) {
        try {
            ProductImageEvent event = objectMapper.readValue(eventJson, ProductImageEvent.class);
            log.info("Processing product image update for product: {}", event.getProductId());

            String mediaId = processProductImageData(event.getProductId(), event.getImageData(), event.getContentType());

            publishMediaProcessedEvent(event.getProductId(), mediaId, MEDIA_TYPE_PRODUCT_IMAGE, "updated");

            log.info("Successfully updated product image for product: {}, mediaId: {}", event.getProductId(), mediaId);
        } catch (JsonProcessingException e) {
            log.error("Error parsing product image update event: {}", eventJson, e);
        } catch (IllegalArgumentException e) {
            log.error("Error processing product image update due to invalid input: {}", eventJson, e);
        } catch (IOException e) {
            log.error("Error processing product image update due to IO issues", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.product.image.delete}", groupId = "mediaservice-group")
    public void handleProductImageDelete(String eventJson) {
        try {
            ProductImageDeleteEvent event = objectMapper.readValue(eventJson, ProductImageDeleteEvent.class);
            log.info("Processing product image deletion for product: {}, imageMediaId: {}", event.getProductId(), event.getImageMediaId());

            // Delete the media file and database record
            deleteProductImage(event.getImageMediaId());

            publishMediaProcessedEvent(event.getProductId(), event.getImageMediaId(), MEDIA_TYPE_PRODUCT_IMAGE, "deleted");

            log.info("Successfully deleted product image for product: {}, mediaId: {}", event.getProductId(), event.getImageMediaId());
        } catch (JsonProcessingException e) {
            log.error("Error parsing product image delete event: {}", eventJson, e);
        } catch (Exception e) {
            log.error("Error processing product image deletion", e);
        }
    }

    private String processProductImageData(String productId, String base64Data, String contentType) throws IOException {
        // Calculate file size
        long fileSizeBytes = calculateFileSize(base64Data);
        
        // Save media record to database with base64 data
        Media media = Media.builder()
                .id(UUID.randomUUID().toString())
                .base64Data(base64Data)
                .contentType(contentType)
                .mediaType(MEDIA_TYPE_PRODUCT_IMAGE)
                .ownerId(productId)
                .fileName(generateFileName(contentType))
                .fileSizeBytes(fileSizeBytes)
                .uploadTimestamp(System.currentTimeMillis())
                .build();

        return mediaRepository.save(media).getId();
    }

    private void deleteProductImage(String imageMediaId) {
        try {
            Media media = mediaRepository.findById(imageMediaId)
                    .orElseThrow(() -> new RuntimeException("Media not found: " + imageMediaId));

            // Delete database record
            mediaRepository.delete(media);

            log.info("Successfully deleted media: {}", imageMediaId);
        } catch (Exception e) {
            log.error("Error deleting media: {}", imageMediaId, e);
        }
    }

    private void publishMediaProcessedEvent(String productId, String mediaId, String mediaType, String action) {
        try {
            MediaProcessedEvent event = MediaProcessedEvent.builder()
                    .productId(productId)
                    .mediaId(mediaId)
                    .mediaType(mediaType)
                    .action(action)
                    .timestamp(System.currentTimeMillis())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(mediaUploadedTopic, productId, eventJson);
            log.info("Published media processed event for product: {}, action: {}", productId, action);
        } catch (JsonProcessingException e) {
            log.error("Error publishing media processed event for product: {}", productId, e);
        }
    }

    // Event classes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductImageEvent {

        private String productId;
        private String imageData;
        private String contentType;
        private Long timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductImageDeleteEvent {

        private String productId;
        private String imageMediaId;
        private Long timestamp;
    }

    @Data
    @Builder
    public static class MediaProcessedEvent {

        private String productId;
        private String mediaId;
        private String mediaType;
        private String action;
        private Long timestamp;
    }

    private long calculateFileSize(String base64Data) {
        // Remove data URL prefix if present
        String cleanBase64 = base64Data.contains(",") ? 
                base64Data.split(",")[1] : base64Data;
        
        // Calculate actual file size from base64
        return (cleanBase64.length() * 3) / 4;
    }

    private String generateFileName(String contentType) {
        String extension = switch (contentType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        
        return "product_image_" + System.currentTimeMillis() + extension;
    }
}
