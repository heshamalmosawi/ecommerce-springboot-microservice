package com.sayedhesham.orderservice.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReservationRequest {
    private String orderId;
    private Map<String, Integer> productIdToQuantityMap;
    private ReservationStatus status;

    public enum ReservationStatus {
        PENDING,
        RESERVED,
        FAILED
    }
}
