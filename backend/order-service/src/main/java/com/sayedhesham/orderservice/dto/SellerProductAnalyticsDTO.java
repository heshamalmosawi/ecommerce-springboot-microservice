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
public class SellerProductAnalyticsDTO {
    @Id
    private String productId;
    private String productName;
    private Integer unitsSold;         // Total quantity sold
    private Integer orderCount;        // Number of orders containing this product
    private Double totalRevenue;       // Total revenue from this product
}
