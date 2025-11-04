package com.tpibackend.orders.exception;

public class OrdersValidationException extends RuntimeException {
    public OrdersValidationException(String message) {
        super(message);
    }
}
