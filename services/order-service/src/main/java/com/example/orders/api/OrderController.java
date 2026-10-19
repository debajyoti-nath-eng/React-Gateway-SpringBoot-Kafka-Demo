/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * Licensed under the GNU General Public License v3.0; see LICENSE.
 */
package com.example.orders.api;

import com.example.orders.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST endpoints for creating and querying orders.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates an order controller.
     *
     * @param orderService order application service
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates an order and returns its resource location.
     *
     * @param request validated order details
     * @return HTTP 201 and the created order
     */
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(order.id())
                .toUri();
        return ResponseEntity.created(location).body(order);
    }

    /**
     * Lists orders newest first.
     *
     * @return all orders currently held by this service instance
     */
    @GetMapping
    public List<OrderResponse> findAll() {
        return orderService.findAll();
    }

    /**
     * Reads one order.
     *
     * @param id order identifier
     * @return HTTP 200 with the order, or HTTP 404 when it is absent
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable String id) {
        OrderResponse order = orderService.findById(id);
        return order == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(order);
    }
}
