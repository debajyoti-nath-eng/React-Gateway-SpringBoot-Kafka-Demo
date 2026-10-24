<!-- Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors. -->
# React + API Gateway + Spring Boot + REST + Kafka

A small end-to-end order workflow that demonstrates how a React UI can call a Spring Cloud API Gateway, which then relays requests to a Spring Boot order service. The order service validates input, writes the status in memory, and publishes an `orders.created` Kafka event. A separate notification service consumes the message and logs it, giving a clear picture of synchronous REST handling plus asynchronous event-driven processing.

## Documentation map

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — component responsibilities, request flow, and deployment model.
- [services/order-service](services/order-service) — REST API, validation, event publication, and in-memory order storage.
- [services/notification-service](services/notification-service) — Kafka consumer that reacts to created-order events.
- [services/api-gateway](services/api-gateway) — Edge layer that exposes the API and routes requests to the order service.

## Architecture summary

```text
Browser / React UI :5173
      │  HTTP /api/orders
      ▼
Spring Cloud Gateway :8080
      │  routes /api/orders/** to order-service:8081
      ▼
Order Service (Spring Boot) :8081
      │  validates payload
      │  stores order in memory
      ├──► Kafka topic: orders.created
      │
      ▼
Notification Service (Kafka consumer)
      │  logs order confirmation events
      ▼
Application logs / container output
```

This project is intentionally simple and demo-oriented:

- The order API keeps orders in memory only, so a restart clears the list.
- The service waits up to five seconds for Kafka acknowledgement before accepting the order.
- A successful order creation returns `201 Created` with a `Location` header.
- Kafka publication failure returns `503 Service Unavailable` and is surfaced as a ProblemDetail.
- The notification service logs each received event without modifying persistent data.

## Runtime responsibilities

### React frontend
The UI is a small dashboard that fetches and submits orders through the gateway. It is intentionally skimmable and focused on demonstrating the end-to-end flow rather than production-grade UI concerns.

### API Gateway
Spring Cloud Gateway exposes a single entry point on port `8080`. It forwards `/api/orders` traffic to the order service and adds CORS for the local React dev server.

### Order service
The order service owns business validation and the order lifecycle. It:

- validates incoming payload via Jakarta Bean Validation
- calculates totals from `quantity * unitPrice`
- emits a Kafka event before persisting in the in-memory store
- returns a REST representation to the caller

### Notification service
The consumer listens to the configured Kafka topic and prints an informational log message when an order was created. This shows how the same event can be consumed by a second system without coupling the API to downstream side effects.

## Requirements

- Java 17 or newer
- Maven 3.9+
- Node.js 20+ and npm
- Docker Compose / Podman Compose for the local Kafka and service stack

## Run the sample

1. Start Kafka and the Spring services from the repository root:

   ```sh
   docker compose up --build
   ```

   The gateway is available at `http://localhost:8080` and Kafka is exposed at `localhost:9092`.

2. Start the React app in a second terminal:

   ```sh
   cd frontend
   npm install
   npm run dev
   ```

   Open `http://localhost:5173` in a browser to use the dashboard.

3. Create an order in the UI, then inspect the logs of the notification service:

   ```sh
   docker compose logs -f notification-service
   ```

   To stop the stack, run:

   ```sh
   docker compose down
   ```

## REST API contract

All requests go through the gateway at `http://localhost:8080`.

| Method | Path | Result |
| --- | --- | --- |
| `GET` | `/api/orders` | List orders, newest first |
| `GET` | `/api/orders/{id}` | Read one order (`404` if absent) |
| `POST` | `/api/orders` | Create an order (`201` with `Location`) |

Example request:

```sh
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Alex Morgan","item":"Mechanical keyboard","quantity":2,"unitPrice":49.95}'
```

Request validation includes:

- non-blank `customerName`
- non-blank `item`
- positive integer `quantity`
- positive `unitPrice`

## Build and test

```sh
mvn test
cd frontend
npm install
npm run build
```

## Repository layout

```text
.
├── README.md
├── docker-compose.yml
├── pom.xml
├── docs/
│   └── ARCHITECTURE.md
├── services/
│   ├── api-gateway/
│   ├── notification-service/
│   └── order-service/
├── frontend/
└── LICENSE
```

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for
the full license text. Third-party libraries and tools are subject to their
respective licenses.

Java services use JavaDocs on their public application, API, and event types and members, and the configuration files include explanatory comments for environment and topology details.
