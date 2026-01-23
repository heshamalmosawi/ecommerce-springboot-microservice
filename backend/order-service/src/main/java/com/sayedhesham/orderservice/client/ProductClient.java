package com.sayedhesham.orderservice.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for communicating with the product-service
 * to retrieve seller's product IDs for analytics
 */
@FeignClient(name = "productservice")
public interface ProductClient {

    /**
     * Get all product IDs for the authenticated seller
     * @param authHeader JWT token with "Bearer " prefix
     * @return List of product IDs owned by the seller
     */
    @GetMapping("/seller/ids")
    List<String> getSellerProductIds(@RequestHeader("Authorization") String authHeader);
}
