package com.sayedhesham.orderservice.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

import com.sayedhesham.orderservice.model.Product;

/**
 * Feign client for communicating with the product-service
 * to retrieve seller's product IDs for analytics
 */
@FeignClient(name = "productservice")
public interface ProductClient {

    /**
     * Get all product IDs for the authenticated seller
     * Authorization header is automatically propagated via Feign interceptor
     * @return List of product IDs owned by the seller
     */
    @GetMapping("/seller/ids")
    List<String> getSellerProductIds();
    
    /**
     * Get current product details for multiple products by IDs
     * Batch fetch is more efficient than individual calls
     * 
     * @param ids List of product IDs to fetch
     * @return List of current product data with prices and quantities
     */
    @GetMapping("/batch")
    List<Product> getProductsByIds(@RequestParam("ids") List<String> ids);
}
