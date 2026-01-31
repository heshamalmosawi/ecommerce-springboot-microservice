package com.sayedhesham.orderservice.dto;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalyticsDTO {
    @Id
    private String productId;
    private String productName;
    private Integer orderCount;        // Number of orders containing this product
    private Integer totalQuantity;     // Total units purchased
    private Double totalSpent;         // Total money spent on this product
}
