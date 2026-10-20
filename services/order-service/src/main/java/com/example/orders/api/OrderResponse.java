/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * Licensed under the MIT License; see LICENSE.
 */
package com.example.orders.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Public representation of an order returned by the REST API.
 *
 * @param id           unique order identifier
 * @param customerName customer who placed the order
 * @param item         item being ordered
 * @param quantity     number of items
 * @param unitPrice    price per item
 * @param total        calculated order total
 * @param createdAt    time the order was accepted
 * @param status       current order status
 */
public record OrderResponse(
        String id,
        String customerName,
        String item,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal total,
        Instant createdAt,
        String status) {
}
