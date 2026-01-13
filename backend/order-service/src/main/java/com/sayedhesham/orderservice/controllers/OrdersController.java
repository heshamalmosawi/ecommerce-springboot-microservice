package com.sayedhesham.orderservice.controllers;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sayedhesham.orderservice.dto.OrderDTO;
import com.sayedhesham.orderservice.service.OrderSagaOrchestrator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/")
class OrdersController {
    
    @Autowired
    private OrderSagaOrchestrator orderSagaOrchestrator;

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
    public ResponseEntity<String> getOrders() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("TODO: Implement me!");
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
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    } 

}
