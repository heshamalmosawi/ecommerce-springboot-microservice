package com.sayedhesham.productservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageEventService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.product.image.upload}")
    private String productImageUploadTopic;

    @Value("${kafka.topic.product.image.update}")
    private String productImageUpdateTopic;

    @Value("${kafka.topic.product.image.delete}")
    private String productImageDeleteTopic;

    public void publishProductImageUploadEvent(String productId, String imageData, String contentType) {
        try {
            ProductImageEvent event = new ProductImageEvent(
                    productId,
                    imageData,
                    contentType,
                    System.currentTimeMillis()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(productImageUploadTopic, productId, eventJson);
            log.info("Published product image upload event for product: {}", productId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing product image upload event for product: {}", productId, e);
            throw new RuntimeException("Failed to publish product image upload event", e);
        }
    }

    public void publishProductImageUpdateEvent(String productId, String imageData, String contentType) {
        try {
            ProductImageEvent event = new ProductImageEvent(
                    productId,
                    imageData,
                    contentType,
                    System.currentTimeMillis()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(productImageUpdateTopic, productId, eventJson);
            log.info("Published product image update event for product: {}", productId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing product image update event for product: {}", productId, e);
            throw new RuntimeException("Failed to publish product image update event", e);
        }
    }

    public void publishProductImageDeleteEvent(String productId, String imageMediaId) {
        try {
            ProductImageDeleteEvent event = new ProductImageDeleteEvent(
                    productId,
                    imageMediaId,
                    System.currentTimeMillis()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(productImageDeleteTopic, productId, eventJson);
            log.info("Published product image delete event for product: {}, imageMediaId: {}", productId, imageMediaId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing product image delete event for product: {}", productId, e);
            throw new RuntimeException("Failed to publish product image delete event", e);
        }
    }

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
}