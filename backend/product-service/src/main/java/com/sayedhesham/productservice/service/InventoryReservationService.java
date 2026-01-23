package com.sayedhesham.productservice.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sayedhesham.productservice.dto.InventoryReleaseEvent;
import com.sayedhesham.productservice.dto.OrderEvent;
import com.sayedhesham.productservice.dto.ProductReservationRequest;
import com.sayedhesham.productservice.model.Product;
import com.sayedhesham.productservice.repository.ProductRepository;

@Service
public class InventoryReservationService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryReservationService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topic.products.reservation.success}")
    private String productReservationSuccessTopic;

    @Value("${kafka.topic.products.reservation.failed}")
    private String productReservationFailedTopic;

    @Value("${kafka.topic.order.inventory.release}")
    private String orderInventoryReleaseTopic;

    @KafkaListener(topics = "${kafka.topic.order.product.event}", groupId = "productservice-group")
    @Transactional
    public void handleOrderCreatedEvent(String message) {
        try {
            logger.info("Received order created event: {}", message);
            OrderEvent orderEvent = objectMapper.readValue(message, OrderEvent.class);
            
            ProductReservationRequest reservationResponse = processInventoryReservation(orderEvent);
            
            String responseJson = objectMapper.writeValueAsString(reservationResponse);
            
            if (reservationResponse.getStatus() == ProductReservationRequest.ReservationStatus.RESERVED) {
                kafkaTemplate.send(productReservationSuccessTopic, orderEvent.getOrderId(), responseJson);
                logger.info("Sent product reservation success event for order: {}", orderEvent.getOrderId());
            } else {
                kafkaTemplate.send(productReservationFailedTopic, orderEvent.getOrderId(), responseJson);
                logger.info("Sent product reservation failed event for order: {}", orderEvent.getOrderId());
            }
            
        } catch (JsonProcessingException e) {
            logger.error("Error processing order event: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error handling order created event: {}", e.getMessage(), e);
        }
    }

    private ProductReservationRequest processInventoryReservation(OrderEvent orderEvent) {
        ProductReservationRequest reservationResponse = ProductReservationRequest.builder()
                .orderId(orderEvent.getOrderId())
                .productIdToQuantityMap(orderEvent.getProductIdToQuantityMap())
                .status(ProductReservationRequest.ReservationStatus.RESERVED)
                .build();

        // First pass: Validate all products have sufficient stock
        for (Map.Entry<String, Integer> entry : orderEvent.getProductIdToQuantityMap().entrySet()) {
            String productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            try {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

                if (product.getQuantity() < requestedQuantity) {
                    logger.error("Insufficient stock for product: {}, requested: {}, available: {}", 
                            productId, requestedQuantity, product.getQuantity());
                    reservationResponse.setStatus(ProductReservationRequest.ReservationStatus.FAILED);
                    return reservationResponse;
                }
            } catch (Exception e) {
                logger.error("Error validating product: {}", productId, e);
                reservationResponse.setStatus(ProductReservationRequest.ReservationStatus.FAILED);
                return reservationResponse;
            }
        }

        // Second pass: Reserve all products (only if all validations passed)
        for (Map.Entry<String, Integer> entry : orderEvent.getProductIdToQuantityMap().entrySet()) {
            String productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();

            try {
                Product product = productRepository.findById(productId).get();
                product.setQuantity(product.getQuantity() - requestedQuantity);
                productRepository.save(product);
                logger.info("Reserved {} units of product: {}", requestedQuantity, productId);
            } catch (Exception e) {
                logger.error("Error reserving product: {} - This should not happen after validation", productId, e);
                reservationResponse.setStatus(ProductReservationRequest.ReservationStatus.FAILED);
                throw new RuntimeException("Failed to reserve product after validation: " + productId, e);
            }
        }

        return reservationResponse;
    }

    @KafkaListener(topics = "${kafka.topic.order.inventory.release}", groupId = "productservice-group")
    @Transactional
    public void handleInventoryReleaseEvent(String message) {
        try {
            logger.info("Received inventory release event: {}", message);
            InventoryReleaseEvent releaseEvent = objectMapper.readValue(message, InventoryReleaseEvent.class);
            
            if (!"RELEASE".equals(releaseEvent.getAction())) {
                logger.warn("Ignoring invalid action for order {}: {}", releaseEvent.getOrderId(), releaseEvent.getAction());
                return;
            }
            
            releaseInventory(releaseEvent);
            logger.info("Successfully released inventory for order: {}", releaseEvent.getOrderId());
        } catch (JsonProcessingException e) {
            logger.error("Error parsing inventory release event: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error handling inventory release event: {}", e.getMessage(), e);
        }
    }

    private void releaseInventory(InventoryReleaseEvent releaseEvent) {
        for (InventoryReleaseEvent.OrderItem item : releaseEvent.getOrderItems()) {
            String productId = item.getProductId();
            Integer quantity = item.getQuantity();
            
            try {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
                
                product.setQuantity(product.getQuantity() + quantity);
                productRepository.save(product);
                logger.info("Released {} units of product: {}", quantity, productId);
            } catch (Exception e) {
                logger.error("Error releasing inventory for product {}: {}", productId, e.getMessage(), e);
                throw e;
            }
        }
    }
}
