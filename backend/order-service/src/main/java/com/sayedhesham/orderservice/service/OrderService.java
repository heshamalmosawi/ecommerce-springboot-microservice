package com.sayedhesham.orderservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sayedhesham.orderservice.client.ProductClient;
import com.sayedhesham.orderservice.dto.DateRangeDTO;
import com.sayedhesham.orderservice.dto.OrderDTO;
import com.sayedhesham.orderservice.dto.OrderItemDTO;
import com.sayedhesham.orderservice.dto.OrderStatusResponseDTO;
import com.sayedhesham.orderservice.dto.PurchaseSummaryDTO;
import com.sayedhesham.orderservice.dto.SellerAnalyticsSummaryDTO;
import com.sayedhesham.orderservice.exceptions.InvalidStatusTransitionException;
import com.sayedhesham.orderservice.exceptions.OrderCannotBeCancelledException;
import com.sayedhesham.orderservice.exceptions.ServiceCommunicationException;
import com.sayedhesham.orderservice.exceptions.UnauthorizedOrderAccessException;
import com.sayedhesham.orderservice.model.Order;
import com.sayedhesham.orderservice.model.OrderItem;
import com.sayedhesham.orderservice.model.Product;
import com.sayedhesham.orderservice.repository.OrderRepository;
import com.sayedhesham.orderservice.repository.ProductRepository;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private OrderRepository orderRepo;
    
    @Autowired
    private ProductClient productClient;

    public Order create(OrderDTO orderDTO) {
        System.out.println("OrderService: Starting order creation");
        String userId = Utils.getCurrentUserId();
        System.out.println("OrderService: User ID - " + userId);

        // Build order items with product details
        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            System.out.println("OrderService: Processing order item");
            // Fetch product details
            Product product = prodRepo.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                    "Product not found: " + itemDTO.getProductId()));

            System.out.println("OrderService: Product found");

            // Validate stock availability
            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: " + product.getName());
            }

            // Create order item with full details
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemDTO.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItems.add(orderItem);
            totalPrice += product.getPrice() * itemDTO.getQuantity();
        }

        // Create and save order
        System.out.println("OrderService: Creating order");
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .buyerId(userId)
                .email(orderDTO.getEmail())
                .internationalPhone(orderDTO.getInternationalPhone())
                .fullName(orderDTO.getFullName())
                .address(orderDTO.getAddress())
                .city(orderDTO.getCity())
                .postalCode(orderDTO.getPostalCode())
                .orderItems(orderItems)
                .totalPrice(totalPrice)
                .status(Order.OrderStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        System.out.println("OrderService: Saving order");
        return orderRepo.save(order);
    }

    public void updateOrderStatus(String orderId, Order.OrderStatus status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        order.setStatus(status);
        orderRepo.save(order);
    }

    public Page<Order> getMyOrders(
            String userId, 
            org.springframework.data.domain.Pageable pageable,
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return orderRepo.findByFilters(userId, status, startDate, endDate, pageable);
    }

    public Order getOrderById(String orderId) {
        String userId = Utils.getCurrentUserId();
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        
        if (!order.getBuyerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this order");
        }
        
        return order;
    }
    
    public PurchaseSummaryDTO getPurchaseAnalytics(
            String userId,
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        PurchaseSummaryDTO summary = orderRepo.getPurchaseAnalytics(
            userId, status, startDate, endDate);
        
        // Add date range to response
        if (startDate != null || endDate != null) {
            summary.setDateRange(DateRangeDTO.builder()
                .start(startDate != null ? startDate.toLocalDate().toString() : null)
                .end(endDate != null ? endDate.toLocalDate().toString() : null)
                .build());
        }
        
        return summary;
    }
    
    /**
     * Get seller analytics by retrieving product IDs from product-service
     * and aggregating order data. Uses automatic token propagation via Feign interceptor.
     * @param status Order status filter (optional)
     * @param startDate Start date for filtering (optional)
     * @param endDate End date for filtering (optional)
     * @return Seller analytics summary
     */
    public SellerAnalyticsSummaryDTO getSellerAnalytics(
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        System.out.println("[OrderService] Starting getSellerAnalytics");
        System.out.println("[OrderService] Filters - Status: " + status + ", StartDate: " + startDate + ", EndDate: " + endDate);
        
        // Get seller's product IDs from product-service (token propagated automatically)
        System.out.println("[OrderService] Calling product-service to get seller's product IDs");
        List<String> productIds = productClient.getSellerProductIds();
        System.out.println("[OrderService] Retrieved " + productIds.size() + " product IDs from product-service");
        
        // Get analytics from repository
        System.out.println("[OrderService] Querying order repository for analytics");
        SellerAnalyticsSummaryDTO summary = orderRepo.getSellerAnalytics(
            productIds, status, startDate, endDate);
        System.out.println("[OrderService] Analytics retrieved successfully");
        
        // Add date range to response
        if (startDate != null || endDate != null) {
            summary.setDateRange(DateRangeDTO.builder()
                .start(startDate != null ? startDate.toLocalDate().toString() : null)
                .end(endDate != null ? endDate.toLocalDate().toString() : null)
                .build());
        }
        
        System.out.println("[OrderService] Returning seller analytics summary");
        return summary;
    }

    /**
     * Get orders where seller has products
     * @param status Order status filter (optional)
     * @param startDate Start date for filtering (optional)
     * @param endDate End date for filtering (optional)
     * @param pageable Pagination parameters
     * @return Page of orders containing seller's products
     */
    public Page<Order> getSellerOrders(
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable) {
        
        System.out.println("[OrderService] Starting getSellerOrders");
        System.out.println("[OrderService] Filters - Status: " + status + ", StartDate: " + startDate + ", EndDate: " + endDate);
        
        List<String> productIds = productClient.getSellerProductIds();
        System.out.println("[OrderService] Retrieved " + productIds.size() + " product IDs from product-service");
        
        Page<Order> orders = orderRepo.findSellerOrders(
            productIds, status, startDate, endDate, pageable);
        System.out.println("[OrderService] Retrieved " + orders.getTotalElements() + " orders containing seller's products");
        
        return orders;
    }

    /**
     * Change order status (for sellers only)
     * 
     * @param orderId The order ID to update
     * @param newStatus The new status to set
     * @param sellerId The seller's user ID
     * @return OrderStatusResponseDTO with old and new status
     * @throws IllegalArgumentException if order doesn't exist
     * @throws UnauthorizedOrderAccessException if seller doesn't own products in order
     * @throws InvalidStatusTransitionException if status transition is invalid
     */
    public OrderStatusResponseDTO updateOrderStatus(String orderId, Order.OrderStatus newStatus, String sellerId) {
        // 1. Fetch order
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        // 2. Validate seller owns products in the order
        if (!isSellerOrderOwner(order, sellerId)) {
            throw new UnauthorizedOrderAccessException(
                "You are not authorized to update this order. You don't own any products in this order."
            );
        }
        
        // 3. Validate status transition
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new InvalidStatusTransitionException(
                String.format("Invalid status transition from %s to %s", order.getStatus(), newStatus)
            );
        }
        
        // 4. Update status
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);
        
        log.info("Order {} status updated from {} to {} by seller {}", 
                 orderId, oldStatus, newStatus, sellerId);
        
        return new OrderStatusResponseDTO(
            orderId, 
            oldStatus, 
            newStatus, 
            "Order status updated successfully"
        );
    }

    /**
     * Cancel order (for buyers and sellers)
     * 
     * @param orderId The order ID to cancel
     * @param userId The user's ID (buyer or seller)
     * @param userRole The user's role (BUYER or SELLER)
     * @param reason Optional cancellation reason
     * @return OrderStatusResponseDTO with cancellation details
     * @throws IllegalArgumentException if order doesn't exist
     * @throws UnauthorizedOrderAccessException if user is not authorized
     * @throws OrderCannotBeCancelledException if order status doesn't allow cancellation
     */
    public OrderStatusResponseDTO cancelOrder(String orderId, String userId, String userRole, String reason) {
        // 1. Fetch order
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        // 2. Validate user authorization
        boolean isAuthorized = false;
        if ("CLIENT".equals(userRole)) {
            isAuthorized = order.getBuyerId().equals(userId);
        } else if ("SELLER".equals(userRole)) {
            isAuthorized = isSellerOrderOwner(order, userId);
        }
        
        if (!isAuthorized) {
            throw new UnauthorizedOrderAccessException(
                "You are not authorized to cancel this order."
            );
        }
        
        // 3. Validate order can be cancelled (PENDING or PROCESSING only)
        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new OrderCannotBeCancelledException(
                String.format("Order with status %s cannot be cancelled. " +
                             "Only PENDING or PROCESSING orders can be cancelled.", 
                             order.getStatus())
            );
        }
        
        // 4. Update status to CANCELLED
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);
        
        // 5. Trigger inventory release if order was PROCESSING
        if (oldStatus == Order.OrderStatus.PROCESSING) {
            orderSagaOrchestrator.publishInventoryReleaseEvent(order);
            log.info("Inventory release event published for cancelled order: {}", orderId);
        }
        
        log.info("Order {} cancelled by user {} (role: {}). Reason: {}", 
                 orderId, userId, userRole, reason);
        
        return new OrderStatusResponseDTO(
            orderId, 
            oldStatus, 
            Order.OrderStatus.CANCELLED, 
            "Order cancelled successfully"
        );
    }

    /**
     * Validate if seller owns any products in the order
     * 
     * @param order The order to check
     * @param sellerId The seller's user ID
     * @return true if seller owns at least one product in the order
     */
    private boolean isSellerOrderOwner(Order order, String sellerId) {
        // Get seller's product IDs via Feign client
        List<String> sellerProductIds;
        try {
            sellerProductIds = productClient.getSellerProductIds();
        } catch (Exception e) {
            log.error("Error fetching seller product IDs: {}", e.getMessage());
            throw new ServiceCommunicationException("Unable to verify product ownership");
        }
        
        // Check if any order item matches seller's products
        return order.getOrderItems().stream()
            .anyMatch(item -> sellerProductIds.contains(item.getProductId()));
    }

    /**
     * Validate status transition (forward only)
     * 
     * @param current Current order status
     * @param target Target order status
     * @return true if transition is valid
     */
    private boolean isValidStatusTransition(Order.OrderStatus current, Order.OrderStatus target) {
        // Define valid forward transitions
        Map<Order.OrderStatus, List<Order.OrderStatus>> validTransitions = Map.of(
            Order.OrderStatus.PENDING, List.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.PROCESSING, List.of(Order.OrderStatus.SHIPPED, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.SHIPPED, List.of(Order.OrderStatus.DELIVERED),
            Order.OrderStatus.DELIVERED, List.of(),  // Terminal state
            Order.OrderStatus.CANCELLED, List.of(),  // Terminal state
            Order.OrderStatus.FAILED, List.of()      // Terminal state
        );
        
        return validTransitions.getOrDefault(current, List.of()).contains(target);
    }

    @Autowired
    @Lazy
    private OrderSagaOrchestrator orderSagaOrchestrator;
}