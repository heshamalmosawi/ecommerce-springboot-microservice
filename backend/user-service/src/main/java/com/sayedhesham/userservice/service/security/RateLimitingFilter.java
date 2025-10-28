package com.sayedhesham.userservice.service.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter implements Filter {

    private static class RequestInfo {

        AtomicInteger requestCount = new AtomicInteger(0);
        long timestamp = System.currentTimeMillis();
    }
    // Map to store request counts per IP address
    private final Map<String, RequestInfo> requestCountsPerIpAddress = new ConcurrentHashMap<>();

    // Maximum requests allowed per minute
    private static final int MAX_REQUESTS_PER_MINUTE = 15;
    private static final long ONE_MINUTE = 60 * 1000; // in milliseconds

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String clientIpAddress = httpServletRequest.getRemoteAddr();
        RequestInfo requestInfo = requestCountsPerIpAddress.computeIfAbsent(clientIpAddress, k -> new RequestInfo());

        synchronized (requestInfo) {
            long currentTime = System.currentTimeMillis();

            // If one minute has passed, reset the counter and timestamp
            if (currentTime - requestInfo.timestamp > ONE_MINUTE) {
                requestInfo.requestCount.set(0);
                requestInfo.timestamp = currentTime;
            }

            // Increment request count
            if (requestInfo.requestCount.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                httpServletResponse.setStatus(429);
                httpServletResponse.getWriter().write("Too many requests. Please try again later.");
                return;
            }
        }

        // Allow the request to proceed
        chain.doFilter(request, response);

    }
}
