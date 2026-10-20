/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * Licensed under the MIT License; see LICENSE.
 */
package com.example.orders.service;

import com.example.orders.api.CreateOrderRequest;
import com.example.orders.api.OrderResponse;
import com.example.orders.event.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates and retrieves demo orders, then publishes order-created events.
 * Orders are held in memory and are cleared when the service restarts.
 */
@Service
public class OrderService {

    private final ConcurrentHashMap<String, OrderResponse> orders = new ConcurrentHashMap<>();
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String topic;

    /**
     * Creates an order service using the configured Kafka topic.
     *
     * @param kafkaTemplate Kafka producer template
     * @param topic         topic used for order-created events
     */
    public OrderService(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Publishes the order event before accepting the order into the in-memory store.
     *
     * @param request validated order details
     * @return the created order
     * @throws OrderEventPublishException when Kafka does not acknowledge the event
     */
    public OrderResponse create(CreateOrderRequest request) {
        String id = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();
        BigDecimal total = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
        OrderResponse order = new OrderResponse(
                id, request.customerName().trim(), request.item().trim(), request.quantity(),
                request.unitPrice(), total, createdAt, "CONFIRMED");

        OrderCreatedEvent event = new OrderCreatedEvent(
                id, order.customerName(), order.item(), order.quantity(), total, createdAt);
        try {
            kafkaTemplate.send(topic, id, event).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OrderEventPublishException("Interrupted while publishing the order event", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new OrderEventPublishException("Kafka did not acknowledge the order event", exception);
        } catch (KafkaException exception) {
            throw new OrderEventPublishException("Kafka could not send the order event", exception);
        }
        orders.put(id, order);
        return order;
    }

    /**
     * Returns all known orders, newest first.
     *
     * @return an immutable list of orders
     */
    public List<OrderResponse> findAll() {
        return orders.values().stream()
                .sorted(Comparator.comparing(OrderResponse::createdAt).reversed())
                .toList();
    }

    /**
     * Finds an order by its identifier.
     *
     * @param id order identifier
     * @return the matching order, or {@code null} when absent
     */
    public OrderResponse findById(String id) {
        return orders.get(id);
    }
}
