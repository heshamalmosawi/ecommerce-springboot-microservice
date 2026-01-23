package com.sayedhesham.orderservice.repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.sayedhesham.orderservice.dto.ProductAnalyticsDTO;
import com.sayedhesham.orderservice.dto.PurchaseSummaryDTO;
import com.sayedhesham.orderservice.dto.SellerAnalyticsSummaryDTO;
import com.sayedhesham.orderservice.dto.SellerProductAnalyticsDTO;
import com.sayedhesham.orderservice.model.Order;

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<Order> findByFilters(
            String buyerId,
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        
        Query query = new Query();
        
        // Always filter by buyerId
        query.addCriteria(Criteria.where("buyerId").is(buyerId));
        
        // Add status filter if provided
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }
        
        // Add date range filters if provided
        if (startDate != null && endDate != null) {
            // Both dates provided: createdAt >= startDate AND createdAt <= endDate
            query.addCriteria(Criteria.where("createdAt").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            // Only start date: createdAt >= startDate
            query.addCriteria(Criteria.where("createdAt").gte(startDate));
        } else if (endDate != null) {
            // Only end date: createdAt <= endDate
            query.addCriteria(Criteria.where("createdAt").lte(endDate));
        }
        
        // Get total count for pagination
        long total = mongoTemplate.count(query, Order.class);
        
        // Apply pagination and sorting
        query.with(pageable);
        
        // Execute query
        List<Order> orders = mongoTemplate.find(query, Order.class);
        
        return new PageImpl<>(orders, pageable, total);
    }
    
    @Override
    public PurchaseSummaryDTO getPurchaseAnalytics(
            String buyerId,
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        // Stage 1: Match orders by buyerId and filters
        Criteria criteria = Criteria.where("buyerId").is(buyerId);
        
        if (status != null) {
            criteria.and("status").is(status);
        }
        
        if (startDate != null && endDate != null) {
            criteria.and("createdAt").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            criteria.and("createdAt").gte(startDate);
        } else if (endDate != null) {
            criteria.and("createdAt").lte(endDate);
        }
        
        MatchOperation matchStage = Aggregation.match(criteria);
        
        // Stage 2: Unwind order items to process each product separately
        UnwindOperation unwindStage = Aggregation.unwind("orderItems");
        
        // Stage 3: Group by product to calculate statistics
        GroupOperation groupByProduct = Aggregation.group("orderItems.productId")
            .first("orderItems.productName").as("productName")
            .count().as("orderCount")
            .sum("orderItems.quantity").as("totalQuantity")
            .sum(ArithmeticOperators.Multiply.valueOf("orderItems.price")
                .multiplyBy("orderItems.quantity")).as("totalSpent");
        
        // Stage 4: Sort by orderCount (for most purchased)
        SortOperation sortByOrderCount = Aggregation.sort(Sort.Direction.DESC, "orderCount");
        
        // Execute aggregation
        Aggregation aggregation = Aggregation.newAggregation(
            matchStage,
            unwindStage,
            groupByProduct,
            sortByOrderCount
        );
        
        AggregationResults<ProductAnalyticsDTO> results = 
            mongoTemplate.aggregate(aggregation, "orders", ProductAnalyticsDTO.class);
        
        List<ProductAnalyticsDTO> productAnalytics = results.getMappedResults();
        
        // Calculate overall statistics
        double totalSpent = productAnalytics.stream()
            .mapToDouble(ProductAnalyticsDTO::getTotalSpent)
            .sum();
        
        int orderCount = (int) mongoTemplate.count(
            Query.query(criteria), Order.class);
        
        // Build response
        return PurchaseSummaryDTO.builder()
            .totalSpent(totalSpent)
            .orderCount(orderCount)
            .productCount(productAnalytics.size())
            .mostPurchasedProducts(productAnalytics.stream()
                .sorted(Comparator.comparing(ProductAnalyticsDTO::getOrderCount).reversed())
                .limit(5)
                .collect(Collectors.toList()))
            .topSpendingProducts(productAnalytics.stream()
                .sorted(Comparator.comparing(ProductAnalyticsDTO::getTotalSpent).reversed())
                .limit(5)
                .collect(Collectors.toList()))
            .build();
    }
    
    @Override
    public SellerAnalyticsSummaryDTO getSellerAnalytics(
            List<String> productIds,
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        // If no products, return empty analytics
        if (productIds == null || productIds.isEmpty()) {
            return SellerAnalyticsSummaryDTO.builder()
                .totalRevenue(0.0)
                .totalOrders(0)
                .totalUnitsSold(0)
                .productCount(0)
                .bestSellingProducts(List.of())
                .topRevenueProducts(List.of())
                .build();
        }
        
        // Stage 1: Match orders containing seller's products
        Criteria criteria = Criteria.where("orderItems.productId").in(productIds);
        
        // Add status filter (exclude FAILED orders by default)
        if (status != null) {
            criteria.and("status").is(status);
        } else {
            // Only count successful orders
            criteria.and("status").ne(Order.OrderStatus.FAILED);
        }
        
        // Add date range filters
        if (startDate != null && endDate != null) {
            criteria.and("createdAt").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            criteria.and("createdAt").gte(startDate);
        } else if (endDate != null) {
            criteria.and("createdAt").lte(endDate);
        }
        
        MatchOperation matchStage = Aggregation.match(criteria);
        
        // Stage 2: Unwind order items to process each product separately
        UnwindOperation unwindStage = Aggregation.unwind("orderItems");
        
        // Stage 3: Match again to filter only seller's products after unwind
        MatchOperation matchProductsStage = Aggregation.match(
            Criteria.where("orderItems.productId").in(productIds)
        );
        
        // Stage 4: Group by product to calculate statistics
        GroupOperation groupByProduct = Aggregation.group("orderItems.productId")
            .first("orderItems.productName").as("productName")
            .count().as("orderCount")
            .sum("orderItems.quantity").as("unitsSold")
            .sum(ArithmeticOperators.Multiply.valueOf("orderItems.price")
                .multiplyBy("orderItems.quantity")).as("totalRevenue");
        
        // Stage 5: Sort by units sold (for best-selling)
        SortOperation sortByUnitsSold = Aggregation.sort(Sort.Direction.DESC, "unitsSold");
        
        // Execute aggregation
        Aggregation aggregation = Aggregation.newAggregation(
            matchStage,
            unwindStage,
            matchProductsStage,
            groupByProduct,
            sortByUnitsSold
        );
        
        AggregationResults<SellerProductAnalyticsDTO> results = 
            mongoTemplate.aggregate(aggregation, "orders", SellerProductAnalyticsDTO.class);
        
        List<SellerProductAnalyticsDTO> productAnalytics = results.getMappedResults();
        
        // Calculate overall statistics
        double totalRevenue = productAnalytics.stream()
            .mapToDouble(SellerProductAnalyticsDTO::getTotalRevenue)
            .sum();
        
        int totalUnitsSold = productAnalytics.stream()
            .mapToInt(SellerProductAnalyticsDTO::getUnitsSold)
            .sum();
        
        // Count total orders containing seller's products
        int totalOrders = (int) mongoTemplate.count(
            Query.query(criteria), Order.class);
        
        // Build response
        return SellerAnalyticsSummaryDTO.builder()
            .totalRevenue(totalRevenue)
            .totalOrders(totalOrders)
            .totalUnitsSold(totalUnitsSold)
            .productCount(productAnalytics.size())
            .bestSellingProducts(productAnalytics.stream()
                .sorted(Comparator.comparing(SellerProductAnalyticsDTO::getUnitsSold).reversed())
                .limit(5)
                .collect(Collectors.toList()))
            .topRevenueProducts(productAnalytics.stream()
                .sorted(Comparator.comparing(SellerProductAnalyticsDTO::getTotalRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList()))
            .build();
    }
}
