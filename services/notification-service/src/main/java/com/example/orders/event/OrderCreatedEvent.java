/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * Licensed under the GNU General Public License v3.0; see LICENSE.
 */
package com.example.orders.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Kafka message emitted when an order is created.
 *
 * @param orderId      unique order identifier
 * @param customerName customer who placed the order
 * @param item         item being ordered
 * @param quantity     number of items
 * @param total        calculated order total
 * @param createdAt    time the order was accepted
 */
public record OrderCreatedEvent(
        String orderId,
        String customerName,
        String item,
        int quantity,
        BigDecimal total,
        Instant createdAt) {
}
