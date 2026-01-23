package com.sayedhesham.orderservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sayedhesham.orderservice.dto.PurchaseSummaryDTO;
import com.sayedhesham.orderservice.dto.SellerAnalyticsSummaryDTO;
import com.sayedhesham.orderservice.model.Order;

public interface OrderRepositoryCustom {
    Page<Order> findByFilters(
        String buyerId, 
        Order.OrderStatus status, 
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    PurchaseSummaryDTO getPurchaseAnalytics(
        String buyerId,
        Order.OrderStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
    
    SellerAnalyticsSummaryDTO getSellerAnalytics(
        List<String> productIds,
        Order.OrderStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
}
