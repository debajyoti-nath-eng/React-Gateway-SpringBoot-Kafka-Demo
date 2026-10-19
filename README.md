<!-- Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors. -->
# React + API Gateway + Spring Boot + REST + Kafka

A small end-to-end order demo showing a React dashboard calling a Spring Cloud API Gateway, which routes REST requests to a Spring Boot order service. The service publishes `orders.created` messages to Kafka; a separate notification service consumes them.

## Architecture

```text
React / Vite :5173
      │  /api/orders
      ▼
Spring Cloud Gateway :8080
      │
      ▼
Spring Boot Order REST API :8081 ──► Kafka topic: orders.created
                                           │
                                           ▼
                                  Notification consumer
```

The order API keeps orders in memory for demonstration purposes; restarting it clears the list. It waits up to five seconds for Kafka to acknowledge each event before accepting an order. A successful create returns HTTP `201 Created`; Kafka failures return HTTP `503 Service Unavailable` and are logged. The notification consumer logs each event it receives.

## Requirements

- Java 17 or newer
- Maven 3.9+
- Node.js 20+ and npm
- Docker Desktop with Compose (for the local Kafka/backend stack)

## Run the sample

1. Start Kafka and the Spring services from the repository root:

   ```sh
   docker compose up --build
   ```

   The gateway is available at `http://localhost:8080`; Kafka is exposed at `localhost:9092`. The first build downloads Maven and container dependencies.

2. In a second terminal, start the React app:

   ```sh
   cd frontend
   npm install
   npm run dev
   ```

   Open `http://localhost:5173`. Vite proxies `/api` to the gateway. To use another gateway URL, set `VITE_GATEWAY_URL` before starting Vite.

3. Create an order in the UI. The dashboard refreshes from the REST API, and the notification service prints the consumed event in its container logs:

   ```sh
   docker compose logs -f notification-service
   ```

Stop the backend with `docker compose down`.

## REST API

All requests go through the gateway at port `8080`.

| Method | Path | Result |
| --- | --- | --- |
| `GET` | `/api/orders` | List orders, newest first |
| `GET` | `/api/orders/{id}` | Read one order (`404` if absent) |
| `POST` | `/api/orders` | Create an order (`201` with `Location`) |

Example:

```sh
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Alex Morgan","item":"Mechanical keyboard","quantity":2,"unitPrice":49.95}'
```

The request requires a non-blank `customerName` and `item`, a positive integer `quantity`, and a positive `unitPrice`. The created event contains the order ID, customer, item, quantity, total, and creation time.

## Build and test

```sh
mvn test
cd frontend
npm install
npm run build
```

Java services use JavaDocs on their public application, API, and event types and members. Source files include a copyright notice and refer to the repository's GPL-3.0 license.
