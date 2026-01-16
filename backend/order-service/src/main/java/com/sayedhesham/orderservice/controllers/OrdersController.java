package com.sayedhesham.orderservice.controllers;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sayedhesham.orderservice.dto.OrderDTO;
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

    /** This is to get MY orders */
    @GetMapping
    public ResponseEntity<Object> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            String userId = Utils.getCurrentUserId();
            Page<Order> orders = orderService.getMyOrders(userId, pageable);
            return ResponseEntity.ok(orders);
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ise.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
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

}
