package com.sayedhesham.orderservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.sayedhesham.orderservice.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByUserId(String userId);
    Page<Product> findByUserId(String userId, Pageable pageable);
}
