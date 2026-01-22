package com.sayedhesham.orderservice.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sayedhesham.orderservice.dto.OrderDTO;
import com.sayedhesham.orderservice.dto.PurchaseSummaryDTO;
import com.sayedhesham.orderservice.model.Order;
import com.sayedhesham.orderservice.service.OrderSagaOrchestrator;
import com.sayedhesham.orderservice.service.OrderService;
import com.sayedhesham.orderservice.service.Utils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/")
class OrdersController {

    @Autowired
    private OrderSagaOrchestrator orderSagaOrchestrator;

    @Autowired
    private OrderService orderService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * This is to get MY orders
     */
    @GetMapping
    public ResponseEntity<Object> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // Validate page and size parameters
            if (page < 0) throw new IllegalArgumentException("Page parameter must be non-negative");
            if (size <= 0 || size > 100) throw new IllegalArgumentException("Size parameter must be between 1 and 100");

            // Validate sortBy parameter against a whitelist of allowed fields
            String[] allowedFields = {"createdAt", "totalPrice", "status"};
            if (!java.util.Arrays.asList(allowedFields).contains(sortBy)) throw new IllegalArgumentException("Invalid sortBy parameter");
            
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Parse and validate status filter
            Order.OrderStatus orderStatus = null;
            if (status != null && !status.trim().isEmpty()) {
                orderStatus = parseOrderStatus(status);
            }
            
            // Parse and validate date filters
            LocalDateTime start = null;
            LocalDateTime end = null;
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = parseDateToStartOfDay(startDate);
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = parseDateToEndOfDay(endDate);
            }
            
            // Validate date range
            if (start != null && end != null && start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must be before or equal to end date");
            }
            
            String userId = Utils.getCurrentUserId();
            Page<Order> orders = orderService.getMyOrders(userId, pageable, orderStatus, start, end);
            return ResponseEntity.ok(orders);
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Parse and validate order status
     */
    private Order.OrderStatus parseOrderStatus(String status) {
        try {
            return Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value. Allowed values: PENDING, PROCESSING, SHIPPED, DELIVERED, FAILED");
        }
    }
    
    /**
     * Parse ISO date string to LocalDateTime at start of day (00:00:00)
     */
    private LocalDateTime parseDateToStartOfDay(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected ISO date format (YYYY-MM-DD), got: " + dateStr);
        }
    }
    
    /**
     * Parse ISO date string to LocalDateTime at end of day (23:59:59.999999999)
     */
    private LocalDateTime parseDateToEndOfDay(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            return date.atTime(23, 59, 59, 999999999);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected ISO date format (YYYY-MM-DD), got: " + dateStr);
        }
    }

    @PostMapping
    public ResponseEntity<Object> addOrder(@Valid @RequestBody OrderDTO orderDTO) {
        try {
            return ResponseEntity.ok(orderSagaOrchestrator.startOrderSaga(orderDTO));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iae.getMessage());
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Object> getOrderById(@PathVariable String orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (ResponseStatusException rse) {
            return ResponseEntity.status(rse.getStatusCode()).body(rse.getReason());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iae.getMessage());
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
    /**
     * Get user's purchase analytics summary
     * 
     * @param status Filter by order status (optional)
     * @param startDate Start date for filtering (ISO format: YYYY-MM-DD)
     * @param endDate End date for filtering (ISO format: YYYY-MM-DD)
     * @return Purchase summary with analytics
     */
    @GetMapping("/analytics/purchase-summary")
    public ResponseEntity<Object> getPurchaseAnalytics(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // Parse and validate status filter
            Order.OrderStatus orderStatus = null;
            if (status != null && !status.trim().isEmpty()) {
                orderStatus = parseOrderStatus(status);
            }
            
            // Parse and validate date filters
            LocalDateTime start = null;
            LocalDateTime end = null;
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = parseDateToStartOfDay(startDate);
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = parseDateToEndOfDay(endDate);
            }
            
            // Validate date range
            if (start != null && end != null && start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must be before or equal to end date");
            }
            
            String userId = Utils.getCurrentUserId();
            PurchaseSummaryDTO summary = orderService.getPurchaseAnalytics(
                userId, orderStatus, start, end);
            
            return ResponseEntity.ok(summary);
            
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching analytics");
        }
    }

}
