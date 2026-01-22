package com.sayedhesham.orderservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

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
}
