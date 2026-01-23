package com.sayedhesham.orderservice.dto;

import com.sayedhesham.orderservice.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponseDTO {
    private String orderId;
    private Order.OrderStatus oldStatus;
    private Order.OrderStatus newStatus;
    private String message;
}