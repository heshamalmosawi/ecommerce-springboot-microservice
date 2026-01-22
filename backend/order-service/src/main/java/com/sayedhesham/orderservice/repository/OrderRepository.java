package com.sayedhesham.orderservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.sayedhesham.orderservice.model.Order;

public interface OrderRepository extends MongoRepository<Order, String>, OrderRepositoryCustom {
    List<Order> findByBuyerId(String buyerId);
    Page<Order> findByBuyerId(String buyerId, Pageable pageable);
}
