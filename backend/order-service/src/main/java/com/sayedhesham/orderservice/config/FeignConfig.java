package com.sayedhesham.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {
    
    /**
     * Enable detailed logging for Feign clients
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    /**
     * Add request interceptor to:
     * 1. Automatically propagate Authorization header to downstream services
     * 2. Log all Feign requests
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Propagate Authorization header from incoming request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && !authHeader.isEmpty()) {
                    requestTemplate.header("Authorization", authHeader);
                    System.out.println("[FeignClient] Propagating Authorization header to downstream service");
                }
            }
            
            // Log request details
            System.out.println("[FeignClient] Request - Method: " + requestTemplate.method() + 
                             ", URL: " + requestTemplate.url() + 
                             ", Target: " + requestTemplate.feignTarget());
            System.out.println("[FeignClient] Headers: " + requestTemplate.headers());
        };
    }
}
