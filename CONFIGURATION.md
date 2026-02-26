# Configuration Documentation

## Order Service Configuration Guide

This document provides detailed information about configuring the Order Service for different environments.

## Table of Contents

1. [Configuration Files](#configuration-files)
2. [Application Properties](#application-properties)
3. [Environment Variables](#environment-variables)
4. [Profile-Specific Configuration](#profile-specific-configuration)
5. [Circuit Breaker Configuration](#circuit-breaker-configuration)
6. [Kafka Configuration](#kafka-configuration)
7. [WebClient Configuration](#webclient-configuration)
8. [Production Configuration](#production-configuration)
9. [Troubleshooting Configuration](#troubleshooting-configuration)

---

## Configuration Files

### Primary Configuration File

**Location:** `src/main/resources/application.properties`

```properties
# Application name and port
spring.application.name=order-service
server.port=8081

# Inventory Service Configuration
inventory.service.base-url=http://localhost:8080
inventory.service.timeout=5000

# Management (Actuator)
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always

# Resilience4j Circuit Breaker Configuration
resilience4j.circuitbreaker.instances.inventoryService.failure-rate-threshold=10
resilience4j.circuitbreaker.instances.inventoryService.slow-call-rate-threshold=10
resilience4j.circuitbreaker.instances.inventoryService.slow-call-duration-threshold=2s
resilience4j.circuitbreaker.instances.inventoryService.minimum-number-of-calls=10
resilience4j.circuitbreaker.instances.inventoryService.sliding-window-size=20
resilience4j.circuitbreaker.instances.inventoryService.permitted-number-of-calls-in-half-open-state=5
resilience4j.circuitbreaker.instances.inventoryService.wait-duration-in-open-state=30s

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=orders-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*

kafka.inventory.update.topic=inventory-updates
```

### Profile-Specific Files

Create environment-specific files in `src/main/resources/`:

- `application-dev.properties` - Development environment
- `application-test.properties` - Test environment
- `application-prod.properties` - Production environment

---

## Application Properties

### Server Configuration

```properties
# Server port
server.port=8081

# Application name (for logging and monitoring)
spring.application.name=order-service

# Server servlet context path
server.servlet.context-path=/

# Connection timeout (milliseconds)
server.tomcat.connection-timeout=60000

# Thread pool configuration
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
```

### Inventory Service Configuration

```properties
# Base URL of Inventory Service
inventory.service.base-url=http://localhost:8080

# Connection/Read/Write timeout in milliseconds
inventory.service.timeout=5000
```

**Description:**
- `base-url`: Complete URL to Inventory Service
- `timeout`: Maximum time to wait for response (default: 5000ms)

**Environment Variables:**
```bash
export INVENTORY_SERVICE_BASE_URL=http://inventory-service:8080
export INVENTORY_SERVICE_TIMEOUT=10000
```

### Management (Actuator) Configuration

```properties
# Expose all endpoints
management.endpoints.web.exposure.include=*

# Disable specific endpoints if needed
# management.endpoints.web.exposure.exclude=env,beans

# Show detailed health information
management.endpoint.health.show-details=always

# Health endpoint configuration
management.endpoint.health.probes.enabled=true

# Endpoint base path
management.endpoints.web.base-path=/actuator

# Enable detailed metrics
management.metrics.enable.jvm=true
management.metrics.enable.process=true
management.metrics.enable.logback=true
```

**Available Endpoints:**
- `/actuator/health` - Overall health
- `/actuator/health/liveness` - Liveness probe
- `/actuator/health/readiness` - Readiness probe
- `/actuator/metrics` - Metrics
- `/actuator/circuitbreakers` - Circuit breaker status
- `/actuator/env` - Environment properties
- `/actuator/beans` - Registered beans

---

## Environment Variables

### Override Properties with Environment Variables

Spring Boot automatically converts environment variables to properties:

```bash
# Convert to property: server.port
export SERVER_PORT=8081

# Convert to property: spring.kafka.bootstrap-servers
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Convert to property: inventory.service.base-url
export INVENTORY_SERVICE_BASE_URL=http://inventory-service:8080
```

### Precedence (Highest to Lowest)

1. Environment variables
2. System properties (`-D` flag)
3. `application-{profile}.properties`
4. `application.properties`
5. Built-in defaults

### Setting Multiple Environment Variables

```bash
#!/bin/bash
export SERVER_PORT=8081
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
export INVENTORY_SERVICE_BASE_URL=http://inventory-service:8080
export INVENTORY_SERVICE_TIMEOUT=5000
export SPRING_REDIS_HOST=redis
export SPRING_REDIS_PORT=6379

java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

---

## Profile-Specific Configuration

### Development Profile

**File:** `application-dev.properties`

```properties
spring.application.name=order-service
server.port=8081

# Development inventory service (local)
inventory.service.base-url=http://localhost:8080
inventory.service.timeout=5000

# Local Kafka
spring.kafka.bootstrap-servers=localhost:9092
kafka.inventory.update.topic=inventory-updates

# Local Redis
spring.redis.host=localhost
spring.redis.port=6379

# Detailed logging
logging.level.root=INFO
logging.level.com.hometask.orderservice=DEBUG
logging.level.org.springframework=INFO

# Development actuator
management.endpoints.web.exposure.include=*
```

**Run with development profile:**
```bash
java -jar target/order-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
# Or
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Test Profile

**File:** `application-test.properties`

```properties
spring.application.name=order-service-test
server.port=0  # Random port

# Test inventory service
inventory.service.base-url=http://localhost:8080
inventory.service.timeout=5000

# Test Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Minimal logging
logging.level.root=WARN

# No actuator exposure in test
management.endpoints.web.exposure.include=health
```

### Production Profile

**File:** `application-prod.properties`

```properties
spring.application.name=order-service
server.port=8081

# Production inventory service
inventory.service.base-url=http://inventory-service.default.svc.cluster.local:8080
inventory.service.timeout=10000

# Production Kafka cluster
spring.kafka.bootstrap-servers=kafka-broker-1:9092,kafka-broker-2:9092,kafka-broker-3:9092
kafka.inventory.update.topic=inventory-updates

# Production Redis cluster
spring.redis.host=redis.default.svc.cluster.local
spring.redis.port=6379

# Production logging (warnings and errors only)
logging.level.root=WARN
logging.level.com.hometask.orderservice=INFO

# Limited actuator exposure for security
management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=when-authorized
```

**Run with production profile:**
```bash
java -jar target/order-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## Circuit Breaker Configuration

### Overview

Resilience4j Circuit Breaker prevents cascading failures when calling the Inventory Service.

### Properties

```properties
# Circuit breaker name
resilience4j.circuitbreaker.instances.inventoryService

# Failure rate threshold (%)
# Opens circuit when failure rate exceeds this
failure-rate-threshold=10

# Slow call rate threshold (%)
# Opens circuit when slow call rate exceeds this
slow-call-rate-threshold=10

# Duration considered as slow call (milliseconds)
slow-call-duration-threshold=2s

# Minimum number of calls before evaluating
# Prevents opening on single failure
minimum-number-of-calls=10

# Number of calls to evaluate
# Sliding window of recent calls
sliding-window-size=20

# Number of permitted calls in HALF_OPEN state
# Limits test calls during recovery
permitted-number-of-calls-in-half-open-state=5

# Duration in OPEN state before trying HALF_OPEN
# Wait time before testing recovery
wait-duration-in-open-state=30s
```

### Configuration Explanation

**Failure Rate Threshold (10%)**
```
If in last 20 calls, 2 or more fail (10%)
→ Circuit transitions to OPEN
→ Subsequent calls fail fast
```

**Slow Call Rate (10%)**
```
If in last 20 calls, 2 or more are slow (>2 seconds)
→ Circuit transitions to OPEN
→ Triggers fallback
```

**Minimum Number of Calls (10)**
```
Circuit only evaluates after 10 calls
First 9 calls don't affect circuit state
Prevents premature opening
```

**Sliding Window (20)**
```
Evaluates only last 20 calls
Older calls are ignored
Allows recovery from past failures
```

**Wait Duration (30s)**
```
After circuit opens:
- Requests fail immediately for 30 seconds
- After 30s, enters HALF_OPEN state
- Allows testing if service recovered
```

### Tuning for Different Scenarios

**Aggressive (Fail Fast):**
```properties
failure-rate-threshold=5
minimum-number-of-calls=3
sliding-window-size=10
wait-duration-in-open-state=10s
```

**Conservative (Tolerate Failures):**
```properties
failure-rate-threshold=20
minimum-number-of-calls=20
sliding-window-size=50
wait-duration-in-open-state=60s
```

### Monitor Circuit Breaker Status

```bash
curl http://localhost:8081/actuator/circuitbreakers
```

Response:
```json
{
  "circuitBreakers": [
    {
      "name": "inventoryService",
      "status": "CLOSED",
      "details": {
        "failureRate": 0.0,
        "slowCallRate": 0.0,
        "numberOfBufferedCalls": 10,
        "numberOfFailedCalls": 0,
        "numberOfSuccessfulCalls": 10
      }
    }
  ]
}
```

---

## Kafka Configuration

### Producer Configuration

```properties
# Kafka broker addresses (comma-separated)
spring.kafka.bootstrap-servers=localhost:9092

# Producer serializers
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

# Add type info headers for deserialization
spring.kafka.producer.properties.spring.json.add-type-headers=true

# Acknowledgment level
# all = wait for all replicas
spring.kafka.producer.acks=all

# Retries on failure
spring.kafka.producer.retries=3

# Batch size (bytes)
spring.kafka.producer.batch-size=16384

# Linger time (milliseconds) - wait before sending batch
spring.kafka.producer.linger-ms=10

# Custom topic
kafka.inventory.update.topic=inventory-updates
```

### Consumer Configuration

```properties
# Kafka broker addresses
spring.kafka.bootstrap-servers=localhost:9092

# Consumer group ID
spring.kafka.consumer.group-id=orders-group

# Consumer deserializers
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

# Auto offset reset (what to do if offset not found)
# earliest = start from beginning
# latest = start from latest
spring.kafka.consumer.auto-offset-reset=earliest

# Trusted packages for deserialization
spring.kafka.consumer.properties.spring.json.trusted.packages=*

# Auto commit offset
spring.kafka.consumer.enable-auto-commit=true

# Auto commit interval (milliseconds)
spring.kafka.consumer.auto-commit-interval=3000

# Maximum poll records
spring.kafka.consumer.max-poll-records=500
```

### Example: Development vs Production

**Development:**
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=orders-group
kafka.inventory.update.topic=inventory-updates-dev
```

**Production:**
```properties
spring.kafka.bootstrap-servers=kafka-1:9092,kafka-2:9092,kafka-3:9092
spring.kafka.consumer.group-id=orders-group-prod
kafka.inventory.update.topic=inventory-updates
```

---

## WebClient Configuration

### Connection Settings

```properties
# Base URL for Inventory Service
inventory.service.base-url=http://localhost:8080

# Timeout in milliseconds
inventory.service.timeout=5000
```

### Advanced Configuration (WebClientConfig.java)

The WebClient is configured with:

1. **Connection Timeout:** 5000ms
   - Maximum time to establish connection
   
2. **Read Timeout:** 5000ms
   - Maximum time to wait for response
   
3. **Write Timeout:** 5000ms
   - Maximum time to write request
   
4. **Logging Filters:**
   - Logs all outgoing requests
   - Logs all incoming responses
   
5. **Error Handling:**
   - Retries on transient failures
   - Circuit breaker protection

### Request/Response Logging

```java
// Enabled by default in WebClientConfig
// Logs format:
// "Outgoing Request: GET http://localhost:8080/api/products/all"
// "Incoming Response: Status Code 200 OK"
```

### Retry Configuration

```java
// In InventoryServiceClient.java
RETRY_ATTEMPTS = 3
RETRY_DELAY = Duration.ofSeconds(3)

// Retry strategy:
// Attempt 1: Immediately
// Attempt 2: After 3 seconds
// Attempt 3: After 6 seconds (exponential backoff)
```

---

## Production Configuration

### Security Settings

```properties
# Disable endpoints for security
management.endpoints.web.exposure.include=health,metrics

# Show health details only when authorized
management.endpoint.health.show-details=when-authorized

# Disable unused features
spring.h2.console.enabled=false
```

### Performance Tuning

```properties
# Connection pool size
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10

# Request timeout
server.tomcat.connection-timeout=60000

# Kafka batch settings
spring.kafka.producer.batch-size=32768
spring.kafka.producer.linger-ms=100

# Consumer settings
spring.kafka.consumer.max-poll-records=1000
spring.kafka.consumer.session-timeout-ms=30000
```

### Monitoring

```properties
# Enable metrics
management.metrics.export.prometheus.enabled=true

# Enable detailed health checks
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

### Logging

```properties
# Production logging levels
logging.level.root=WARN
logging.level.com.hometask.orderservice=INFO
logging.level.org.springframework=WARN

# Log file configuration
logging.file.name=/var/log/order-service/application.log
logging.file.max-size=10MB
logging.file.max-history=10

# Log pattern
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### Environment Example

```bash
#!/bin/bash
export SERVER_PORT=8081
export SPRING_PROFILES_ACTIVE=prod
export INVENTORY_SERVICE_BASE_URL=http://inventory-service:8080
export INVENTORY_SERVICE_TIMEOUT=10000
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092,kafka-2:9092,kafka-3:9092
export SPRING_REDIS_HOST=redis-cluster
export SPRING_REDIS_PORT=6379

java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

---

## Troubleshooting Configuration

### Issue: Service Won't Start

**Check property syntax:**
```bash
# Validate properties file
mvn clean compile
```

**Check for typos:**
```properties
# WRONG
inventory.service.timeout=5000ms

# CORRECT
inventory.service.timeout=5000
```

### Issue: Service Can't Connect to Inventory Service

**Verify configuration:**
```bash
# Check if base URL is correct
curl http://inventory-service:8080/api/products/all

# Check environment variable
echo $INVENTORY_SERVICE_BASE_URL

# Check active configuration
curl http://localhost:8081/actuator/env | grep inventory
```

**Solution:**
```properties
# Make sure base URL matches actual service address
inventory.service.base-url=http://correct-host:correct-port
```

### Issue: Circuit Breaker Always Open

**Check configuration:**
```bash
curl http://localhost:8081/actuator/circuitbreakers
```

**Possible causes:**
1. Inventory Service is actually down
2. Timeout too short
3. Failure rate threshold too low

**Solution:**
```properties
# Increase timeout
inventory.service.timeout=10000

# Increase failure threshold
resilience4j.circuitbreaker.instances.inventoryService.failure-rate-threshold=20

# Increase minimum calls before opening
resilience4j.circuitbreaker.instances.inventoryService.minimum-number-of-calls=20
```

### Issue: Kafka Messages Not Publishing

**Check configuration:**
```bash
# Verify Kafka is running
docker ps | grep kafka

# Check bootstrap servers
echo $SPRING_KAFKA_BOOTSTRAP_SERVERS

# Check topic exists
kafka-topics --list --bootstrap-server kafka:9092 | grep inventory-updates
```

**Solution:**
```properties
# Correct bootstrap servers
spring.kafka.bootstrap-servers=kafka:9092

# Verify topic name
kafka.inventory.update.topic=inventory-updates
```

---

## Configuration Checklist

### Development Setup
- [ ] `inventory.service.base-url` set to local/dev inventory service
- [ ] Kafka bootstrap servers pointing to dev cluster
- [ ] Redis host/port correct
- [ ] Logging level set to DEBUG
- [ ] All actuator endpoints exposed

### Production Deployment
- [ ] Using `application-prod.properties`
- [ ] Environment variables set correctly
- [ ] Inventory service URL uses internal DNS
- [ ] Kafka cluster addresses correct (multiple brokers)
- [ ] Redis cluster configured
- [ ] Limited actuator endpoints exposed
- [ ] Logging to file configured
- [ ] Circuit breaker thresholds tuned
- [ ] Health checks configured

---

**Document Version:** 1.0  
**Last Updated:** February 2026  
**Next Review:** Q2 2026

