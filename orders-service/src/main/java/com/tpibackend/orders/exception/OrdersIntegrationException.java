package com.tpibackend.orders.exception;

public class OrdersIntegrationException extends RuntimeException {
    public OrdersIntegrationException(String message) {
        super(message);
    }

    public OrdersIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
