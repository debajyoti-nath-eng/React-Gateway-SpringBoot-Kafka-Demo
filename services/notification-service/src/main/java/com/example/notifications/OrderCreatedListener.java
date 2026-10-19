/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * Licensed under the GNU General Public License v3.0; see LICENSE.
 */
package com.example.notifications;

import com.example.orders.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes order-created events and logs a sample notification.
 */
@Component
public class OrderCreatedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCreatedListener.class);

    /**
     * Handles an order-created event.
     *
     * @param event event emitted by the order service
     */
    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(OrderCreatedEvent event) {
        LOGGER.info("Notification: order {} for {} ({}) is confirmed",
                event.orderId(), event.customerName(), event.item());
    }
}
