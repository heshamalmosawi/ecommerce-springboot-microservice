package com.sayedhesham.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnavailableItemDTO {
    private String productId;
    private String productName;
    private Integer requestedQuantity;
    private String reason;
}