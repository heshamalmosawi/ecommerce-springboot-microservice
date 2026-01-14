package com.sayedhesham.productservice.dto;

import java.util.Map;

import lombok.Data;

@Data
public class OrderEvent {
    private String orderId;
    private Map<String, Integer> productIdToQuantityMap;
}
