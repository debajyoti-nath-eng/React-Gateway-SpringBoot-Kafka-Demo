/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * Licensed under the GNU General Public License v3.0; see LICENSE.
 */
package com.example.orders.service;

import com.example.orders.api.CreateOrderRequest;
import com.example.orders.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests order creation, totals, and Kafka event publication.
 */
class OrderServiceTest {

    @Test
    void createsOrderAndPublishesMatchingEvent() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(eq("orders.created"), any(String.class), any(OrderCreatedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        OrderService service = new OrderService(kafkaTemplate, "orders.created");

        var order = service.create(new CreateOrderRequest(
                "  Alex  ", "  Keyboard  ", 2, new BigDecimal("49.95")));

        assertEquals("Alex", order.customerName());
        assertEquals("Keyboard", order.item());
        assertEquals(new BigDecimal("99.90"), order.total());
        assertEquals(order, service.findById(order.id()));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void doesNotAcceptOrderWhenKafkaPublicationFails() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(eq("orders.created"), any(String.class), any(OrderCreatedEvent.class)))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker unavailable")));
        OrderService service = new OrderService(kafkaTemplate, "orders.created");

        assertThrows(OrderEventPublishException.class, () -> service.create(new CreateOrderRequest(
                "Alex", "Keyboard", 1, new BigDecimal("49.95"))));
        assertEquals(0, service.findAll().size());
    }
}
