package com.sayedhesham.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;
import feign.RequestInterceptor;

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
     * Add request interceptor to log all Feign requests
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            System.out.println("[FeignClient] Request - Method: " + requestTemplate.method() + 
                             ", URL: " + requestTemplate.url() + 
                             ", Target: " + requestTemplate.feignTarget());
            System.out.println("[FeignClient] Headers: " + requestTemplate.headers());
        };
    }
}
