/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * All rights reserved; see LICENSE.
 */
package com.example.orders.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Validated request body for creating an order.
 *
 * @param customerName name of the customer placing the order
 * @param item          item being ordered
 * @param quantity      number of items requested
 * @param unitPrice     price per item
 */
public record CreateOrderRequest(
        @NotBlank String customerName,
        @NotBlank String item,
        @Min(1) int quantity,
        @NotNull @Min(1) BigDecimal unitPrice) {
}
