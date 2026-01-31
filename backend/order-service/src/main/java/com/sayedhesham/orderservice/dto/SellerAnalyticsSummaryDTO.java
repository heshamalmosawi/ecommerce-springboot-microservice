package com.sayedhesham.orderservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAnalyticsSummaryDTO {
    private Double totalRevenue;                              // Total revenue across all products
    private Integer totalOrders;                              // Number of orders containing seller's products
    private Integer totalUnitsSold;                           // Total units sold across all products
    private Integer productCount;                             // Number of unique products sold
    private DateRangeDTO dateRange;                           // Applied date filter
    private List<SellerProductAnalyticsDTO> bestSellingProducts;   // Top 5 by units sold
    private List<SellerProductAnalyticsDTO> topRevenueProducts;    // Top 5 by revenue
}
