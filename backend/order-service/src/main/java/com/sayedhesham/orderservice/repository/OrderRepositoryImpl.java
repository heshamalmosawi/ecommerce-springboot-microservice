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
}
