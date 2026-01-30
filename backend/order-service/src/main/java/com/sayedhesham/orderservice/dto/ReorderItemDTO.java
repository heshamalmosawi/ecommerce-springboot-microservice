package com.sayedhesham.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderItemDTO {
    private String productId;
    private String productName;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private Double originalPrice;
    private Double currentPrice;
    private String imageUrl;
}