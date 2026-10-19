# Copyright (c) 2026 React-Gateway-SpringBoot-Kafka-Demo contributors.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -pl services/notification-service -am package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/services/notification-service/target/notification-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
