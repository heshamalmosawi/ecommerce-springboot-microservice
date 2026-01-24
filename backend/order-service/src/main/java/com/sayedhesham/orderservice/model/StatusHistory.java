package com.sayedhesham.orderservice.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistory {

    private Order.OrderStatus status;
    private LocalDateTime changedAt;
}
