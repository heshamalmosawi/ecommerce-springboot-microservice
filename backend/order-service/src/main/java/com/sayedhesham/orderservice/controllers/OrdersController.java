package com.sayedhesham.orderservice.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sayedhesham.orderservice.dto.OrderDTO;
import com.sayedhesham.orderservice.dto.OrderStatusResponseDTO;
import com.sayedhesham.orderservice.dto.OrderStatusUpdateDTO;
import com.sayedhesham.orderservice.dto.PurchaseSummaryDTO;
import com.sayedhesham.orderservice.dto.ReorderResponseDTO;
import com.sayedhesham.orderservice.dto.SellerAnalyticsSummaryDTO;
import com.sayedhesham.orderservice.model.Order;
import com.sayedhesham.orderservice.service.OrderSagaOrchestrator;
import com.sayedhesham.orderservice.service.OrderService;
import com.sayedhesham.orderservice.service.Utils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/")
class OrdersController {

    private static final Logger log = LoggerFactory.getLogger(OrdersController.class);

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
            throw new IllegalArgumentException("Invalid status value. Allowed values: PENDING, PROCESSING, SHIPPED, DELIVERED, FAILED, CANCELLED");
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
     * Get seller's orders (orders containing seller's products)
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @param sortBy Sort field (default createdAt)
     * @param sortDir Sort direction asc/desc (default desc)
     * @param status Filter by order status (optional)
     * @param startDate Start date for filtering (ISO format: YYYY-MM-DD)
     * @param endDate End date for filtering (ISO format: YYYY-MM-DD)
     * @return Page of orders containing seller's products
     */
    @GetMapping("/seller")
    public ResponseEntity<Object> getSellerOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            if (page < 0) throw new IllegalArgumentException("Page parameter must be non-negative");
            if (size <= 0 || size > 100) throw new IllegalArgumentException("Size parameter must be between 1 and 100");

            String[] allowedFields = {"createdAt", "totalPrice", "status"};
            if (!java.util.Arrays.asList(allowedFields).contains(sortBy)) throw new IllegalArgumentException("Invalid sortBy parameter");
            
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Order.OrderStatus orderStatus = null;
            if (status != null && !status.trim().isEmpty()) {
                orderStatus = parseOrderStatus(status);
            }
            
            LocalDateTime start = null;
            LocalDateTime end = null;
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = parseDateToStartOfDay(startDate);
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = parseDateToEndOfDay(endDate);
            }
            
            if (start != null && end != null && start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must be before or equal to end date");
            }
            
            Page<Order> orders = orderService.getSellerOrders(orderStatus, start, end, pageable);
            return ResponseEntity.ok(orders);
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
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
    
    /**
     * Get seller's sales analytics summary
     * Shows best-selling products and revenue for authenticated seller
     * Requires SELLER role
     * 
     * @param status Filter by order status (optional)
     * @param startDate Start date for filtering (ISO format: YYYY-MM-DD)
     * @param endDate End date for filtering (ISO format: YYYY-MM-DD)
     * @return Seller analytics summary with best-selling products and revenue
     */
    @GetMapping("/analytics/seller-summary")
    public ResponseEntity<Object> getSellerAnalytics(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        System.out.println("[OrdersController] /analytics/seller-summary endpoint called");
        System.out.println("[OrdersController] Params - Status: " + status + ", StartDate: " + startDate + ", EndDate: " + endDate);
        
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
            
            System.out.println("[OrdersController] Calling OrderService.getSellerAnalytics");
            SellerAnalyticsSummaryDTO summary = orderService.getSellerAnalytics(
                orderStatus, start, end);
            
            System.out.println("[OrdersController] Successfully retrieved seller analytics");
            return ResponseEntity.ok(summary);
            
        } catch (IllegalStateException ise) {
            System.err.println("[OrdersController] Unauthorized: " + ise.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[OrdersController] Bad request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[OrdersController] Internal server error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while fetching seller analytics: " + e.getMessage());
        }
    }

    /**
     * Update order status (Sellers only)
     * Allows sellers to change order status for orders containing their products
     * Only forward status transitions are allowed
     * 
     * @param orderId The order ID to update
     * @param updateDTO The status update request
     * @return OrderStatusResponseDTO with update details
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Object> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody OrderStatusUpdateDTO updateDTO) {
        try {
            String sellerId = Utils.getCurrentUserId();
            
            OrderStatusResponseDTO response = orderService.updateOrderStatus(
                orderId, 
                updateDTO.getStatus(), 
                sellerId
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iae.getMessage());
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Cancel order (Buyers and Sellers)
     * Allows users to cancel orders in PENDING or PROCESSING status
     * Automatically triggers inventory release for PROCESSING orders
     * 
     * @param orderId The order ID to cancel
     * @param body Optional request body with cancellation reason
     * @return OrderStatusResponseDTO with cancellation details
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Object> cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String userId = Utils.getCurrentUserId();
            String userRole = Utils.getCurrentUserRole();
            String reason = body != null ? body.get("reason") : null;
            
            OrderStatusResponseDTO response = orderService.cancelOrder(
                orderId, 
                userId, 
                userRole, 
                reason
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(iae.getMessage());
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Get items available for reorder from a past order
     * 
     * @param orderId The order ID to reorder from
     * @return ReorderResponseDTO with categorized items
     */
    @GetMapping("/{orderId}/reorder")
    public ResponseEntity<Object> getItemsForReorder(@PathVariable String orderId) {
        try {
            log.info("Reorder request for order: {}", orderId);
            
            ReorderResponseDTO response = orderService.getItemsForReorder(orderId);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException iae) {
            log.warn("Bad request for reorder: {}", iae.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + iae.getMessage());
            
        } catch (IllegalStateException ise) {
            log.warn("Unauthorized reorder attempt: {}", ise.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ise.getMessage());
            
        } catch (Exception e) {
            log.error("Error processing reorder request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while processing your reorder request");
        }
    }

}
