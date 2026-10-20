/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * Licensed under the MIT License; see LICENSE.
 */
package com.example.orders.service;

/**
 * Indicates that an order event was not acknowledged by Kafka.
 */
public class OrderEventPublishException extends RuntimeException {

    /**
     * Creates an event-publish exception with its underlying cause.
     *
     * @param message explanation of the publication failure
     * @param cause   underlying Kafka or timeout failure
     */
    public OrderEventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
