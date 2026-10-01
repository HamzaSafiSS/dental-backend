# Stage 1: Build application
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and POM first for dependency caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (offline cache layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code
COPY src src

# Package JAR skipping tests
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Production runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root group and user for container security
RUN addgroup -S dentalgroup && adduser -S dentaluser -G dentalgroup

# Create logs directory with proper ownership
RUN mkdir -p /app/logs && chown -R dentaluser:dentalgroup /app

# Copy built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar
RUN chown dentaluser:dentalgroup app.jar

USER dentaluser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
