package com.sayedhesham.orderservice.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.sayedhesham.orderservice.dto.OrderDTO;
import com.sayedhesham.orderservice.dto.ProductReservationRequest;
import com.sayedhesham.orderservice.model.Order;

@Service
public class OrderSagaOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(OrderSagaOrchestrator.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Value("${kafka.topic.order.product.event}")
    private String orderProductEventTopic;

    @Value("${kafka.topic.order.inventory.release}")
    private String orderInventoryReleaseTopic;

    @Transactional
    public Order startOrderSaga(OrderDTO orderDTO) {
        Order order = orderService.create(orderDTO);
        
        Map<String, Integer> productIdToQuantityMap = new HashMap<>();
        for (com.sayedhesham.orderservice.dto.OrderItemDTO item : orderDTO.getOrderItems()) {
            productIdToQuantityMap.put(item.getProductId(), item.getQuantity());
        }
        
        ProductReservationRequest productReservationRequest = ProductReservationRequest.builder()
                .orderId(order.getId())
                .productIdToQuantityMap(productIdToQuantityMap)
                .status(ProductReservationRequest.ReservationStatus.PENDING)
                .build();
        
        try {
            String requestJson = objectMapper.writeValueAsString(productReservationRequest);
            kafkaTemplate.send(orderProductEventTopic, order.getId(), requestJson);
            logger.info("Sent product reservation request for order: {}", order.getId());
        } catch (JsonProcessingException e) {
            logger.error("Error sending product reservation request for order: {}", order.getId(), e);
        }
        
        return order;
    }

    @KafkaListener(topics = "${kafka.topic.products.reservation.success}", groupId = "orderservice-group")
    public void handleproductReserved(String message) {
        try {
            logger.info("Received product reserved callback: {}", message);
            ProductReservationRequest callback = objectMapper.readValue(message, ProductReservationRequest.class);
            
            if (callback.getStatus() == ProductReservationRequest.ReservationStatus.RESERVED) {
                orderService.updateOrderStatus(callback.getOrderId(), Order.OrderStatus.PROCESSING);
                logger.info("Order {} status updated to PROCESSING", callback.getOrderId());
            }
        } catch (Exception e) {
            logger.error("Error handling product reserved callback: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.products.reservation.failed}", groupId = "orderservice-group")
    public void handleproductFailed(String message) {
        try {
            logger.info("Received product failed callback: {}", message);
            ProductReservationRequest callback = objectMapper.readValue(message, ProductReservationRequest.class);
            
            if (callback.getStatus() == ProductReservationRequest.ReservationStatus.FAILED) {
                orderService.updateOrderStatus(callback.getOrderId(), Order.OrderStatus.FAILED);
                logger.info("Order {} status updated to FAILED", callback.getOrderId());
            }
        } catch (Exception e) {
            logger.error("Error handling product failed callback: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish inventory release event when order is cancelled
     * This allows the product-service to release reserved inventory
     * 
     * @param order The cancelled order
     */
    public void publishInventoryReleaseEvent(Order order) {
        try {
            // Create release event payload
            Map<String, Object> releaseEvent = Map.of(
                "orderId", order.getId(),
                "action", "RELEASE",
                "orderItems", order.getOrderItems().stream()
                    .map(item -> Map.of(
                        "productId", item.getProductId(),
                        "quantity", item.getQuantity()
                    ))
                    .collect(Collectors.toList())
            );
            
            String eventJson = objectMapper.writeValueAsString(releaseEvent);
            kafkaTemplate.send(orderInventoryReleaseTopic, eventJson);
            
            logger.info("Published inventory release event for order: {}", order.getId());
        } catch (JsonProcessingException e) {
            logger.error("Error serializing inventory release event for order {}: {}", 
                     order.getId(), e.getMessage());
            throw new RuntimeException("Failed to publish inventory release event", e);
        }
    }


}
