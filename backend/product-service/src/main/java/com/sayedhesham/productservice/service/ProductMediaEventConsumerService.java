package com.sayedhesham.productservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sayedhesham.productservice.model.Product;
import com.sayedhesham.productservice.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductMediaEventConsumerService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.media-uploaded}", groupId = "productservice-group")
    public void handleMediaProcessedEvent(String eventJson) {
        try {
            MediaProcessedEvent event = objectMapper.readValue(eventJson, MediaProcessedEvent.class);
            log.info("Processing media processed event for product: {}, action: {}", event.getProductId(), event.getAction());

            if ("product_image".equals(event.getMediaType())) {
                handleProductImageMediaEvent(event);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing media processed event: {}", eventJson, e);
        } catch (Exception e) {
            log.error("Error processing media processed event", e);
        }
    }

    private void handleProductImageMediaEvent(MediaProcessedEvent event) {
        Product product = productRepository.findById(event.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + event.getProductId()));

        switch (event.getAction()) {
            case "uploaded" -> {
                if (product.getImageMediaIds() == null) {
                    product.setImageMediaIds(new java.util.ArrayList<>());
                }
                product.getImageMediaIds().add(event.getMediaId());
                log.info("Added image media ID {} to product: {}", event.getMediaId(), event.getProductId());
            }
            case "updated" -> {
                // For updates, we could replace the first image or add as new
                if (product.getImageMediaIds() == null) {
                    product.setImageMediaIds(new java.util.ArrayList<>());
                }
                product.getImageMediaIds().add(event.getMediaId());
                log.info("Updated product {} with new image media ID: {}", event.getProductId(), event.getMediaId());
            }
            case "deleted" -> {
                if (product.getImageMediaIds() != null) {
                    product.getImageMediaIds().remove(event.getMediaId());
                    log.info("Removed image media ID {} from product: {}", event.getMediaId(), event.getProductId());
                }
            }
            default -> {
                log.warn("Unknown product image action: {}", event.getAction());
                return;
            }
        }

        productRepository.save(product);
        log.info("Successfully updated product {} image media IDs", event.getProductId());
    }

    // Event class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MediaProcessedEvent {
        private String productId;
        private String mediaId;
        private String mediaType;
        private String action;
        private Long timestamp;
    }
}
