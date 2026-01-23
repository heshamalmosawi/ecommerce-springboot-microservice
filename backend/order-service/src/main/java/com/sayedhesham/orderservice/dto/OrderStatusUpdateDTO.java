package com.sayedhesham.orderservice.dto;

import com.sayedhesham.orderservice.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateDTO {
    @NotNull(message = "Status is required")
    private Order.OrderStatus status;
    
    private String reason;
}