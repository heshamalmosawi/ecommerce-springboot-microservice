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
public class PurchaseSummaryDTO {
    private Double totalSpent;
    private Integer orderCount;
    private Integer productCount;
    private DateRangeDTO dateRange;
    private List<ProductAnalyticsDTO> mostPurchasedProducts;
    private List<ProductAnalyticsDTO> topSpendingProducts;
}
