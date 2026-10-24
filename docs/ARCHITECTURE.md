# Architecture Overview

## Purpose

This repository demonstrates a lightweight event-driven microservice flow using:

- a React frontend
- a Spring Cloud API Gateway
- a Spring Boot order service
- Kafka for asynchronous event propagation
- a notification consumer service

The design emphasizes clarity over production complexity. It allows a developer to see how HTTP requests, service-to-service routing, and Kafka messaging work together in a small but realistic architecture.

## Component map

```text
React UI
  │
  ├── sends HTTP requests to the gateway
  │
  ▼
API Gateway
  │  - exposes /api/orders
  │  - enforces CORS for local frontend origin
  │  - forwards traffic to the order service
  ▼
Order Service
  │  - validates incoming order payloads
  │  - computes totals and status
  │  - persists in memory
  │  - publishes Kafka event: orders.created
  ├──────────────► Kafka Broker
  │
  ▼
Notification Service
    - subscribes to orders.created
    - logs a confirmation when an event arrives
```

## Request flow

### 1. Client creates an order
The browser sends an HTTP `POST` to the gateway at `/api/orders`. The gateway matches the route and forwards the request to the `order-service` application.

### 2. Order service validates and creates a record
The `OrderController` receives the request and passes it to `OrderService.create(...)`. Validation runs through Jakarta Bean Validation annotations on `CreateOrderRequest`.

### 3. Kafka event is published
Before the order is accepted into the in-memory store, the service serializes and sends an `OrderCreatedEvent` to Kafka using the configured topic `orders.created`.

### 4. Notification service consumes the event
The consumer (`OrderCreatedListener`) listens for the topic and logs a message with the order ID, customer name, and purchased item. This illustrates a decoupled downstream consumer that reacts to a business event without being directly invoked by the HTTP API.

### 5. Response is returned to the client
If Kafka acknowledges the send within five seconds, the order is stored and returned to the UI with HTTP `201 Created` and a `Location` header pointing to the new resource.

## Deployment topology

The repository includes a Docker Compose setup that starts the required local dependencies:

- Kafka broker
- order-service
- notification-service
- api-gateway

This lets the stack run as a single local environment without requiring a full Kubernetes or cloud deployment setup.

## Design notes

### Why the order store is in-memory
The sample is intentionally lightweight. In-memory storage makes it easy to reason about the flow, test the API quickly, and highlight the messaging pattern without introducing persistence complexity.

### Why Kafka publication is synchronous before acceptance
The `OrderService` waits for Kafka acknowledgement before writing the order to the in-memory map. This is a deliberate demonstration of a transactional-ish API behavior: the system does not accept a local order if the required event could not be emitted.

### Why the notification consumer is separate
This separation makes the event-driven pattern explicit. The order API is responsible for domain logic and event publication, while the notification service is only concerned with reacting to the event.

## Technology choices

- Spring Boot 3.3.x for service implementation
- Spring Cloud Gateway for request routing
- Kafka for asynchronous event distribution
- React + Vite for frontend activity
- Docker Compose for local orchestration

## Risks and limitations

- There is no persistent database or durable storage.
- There is no retry policy, DLQ, or idempotency guarantee for Kafka message handling.
- In-memory state is cleared whenever the order service restarts.
- The sample is designed for local demonstration and is not a full production microservice architecture.

## Related files

- `docker-compose.yml` — container orchestration for the services and Kafka broker
- `services/api-gateway/src/main/resources/application.yml` — gateway routing and CORS config
- `services/order-service/src/main/resources/application.yml` — order service Kafka producer config
- `services/notification-service/src/main/resources/application.yml` — Kafka consumer config
- `README.md` — end-user overview and run instructions
