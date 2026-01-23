package com.sayedhesham.orderservice.exceptions;

public class ServiceCommunicationException extends RuntimeException {
    public ServiceCommunicationException(String message) {
        super(message);
    }
}