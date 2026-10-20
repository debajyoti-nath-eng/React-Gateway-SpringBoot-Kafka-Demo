/*
 * Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
 * All rights reserved; see LICENSE.
 */
package com.example.orders.api;

import com.example.orders.service.OrderEventPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts application failures to explicit REST error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Reports Kafka publication failures as HTTP 503 responses.
     *
     * @param exception event publication failure
     * @return an RFC 9457 problem response
     */
    @ExceptionHandler(OrderEventPublishException.class)
    public ResponseEntity<ProblemDetail> handlePublishFailure(OrderEventPublishException exception) {
        LOGGER.error("Order creation failed because Kafka did not acknowledge the event", exception);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "The order event could not be published. No order was created.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }
}
