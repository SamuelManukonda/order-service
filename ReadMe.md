# Order Service - Microservice Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Features](#features)
4. [Technology Stack](#technology-stack)
5. [Project Structure](#project-structure)
6. [Getting Started](#getting-started)
7. [API Documentation](#api-documentation)
8. [Configuration](#configuration)
9. [Testing](#testing)
10. [Resilience Patterns](#resilience-patterns)
11. [Kafka Integration](#kafka-integration)
12. [Troubleshooting](#troubleshooting)
13. [Contributing](#contributing)

---

## Overview

The **Order Service** is a Spring Boot-based microservice that handles order placement and management in a distributed e-commerce system. It integrates with an Inventory Service to manage product availability and uses Apache Kafka for asynchronous event-driven communication.

**Current Version:** 0.0.1-SNAPSHOT  
**Java Version:** 21  
**Spring Boot Version:** 3.5.11  

---

## Architecture

### System Design

```
┌─────────────────────────────────────────────────────────┐
│                  Client (REST API)                       │
└──────────────────────────┬──────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────┐
        │      Order Service (Port 8081)   │
        │  ┌─────────────┐  ┌────────────┐ │
        │  │  Controller │  │  Service   │ │
        │  └─────────────┘  └────────────┘ │
        └──────────────────┬───────────────┘
                           │
        ┌──────────────────┼───────────────┐
        │                  │               │
        ▼                  ▼               ▼
    ┌────────────┐   ┌──────────┐   ┌─────────────┐
    │ Inventory  │   │  Kafka   │   │   Redis     │
    │  Service   │   │  Broker  │   │   Cache     │
    │(Port 8080) │   │(Port9092)│   │(Port 6379)  │
    └────────────┘   └──────────┘   └─────────────┘
```

### Service Interactions

1. **InventoryService Integration:**
   - WebClient calls inventory service for product details
   - Circuit breaker pattern for resilience
   - Fallback mechanism for service unavailability

2. **Kafka Event Publishing:**
   - Publishes order placement events to Kafka
   - Topic: `inventory-updates`
   - Enables asynchronous processing

3. **Redis Caching:**
   - Reactive Redis support for caching
   - Improves performance for frequently accessed data

---

## Features

### Core Functionality
- ✅ Order placement with inventory validation
- ✅ Product search and browsing
- ✅ Stock availability checking
- ✅ Order confirmation and messaging

### Resilience & Fault Tolerance
- ✅ **Circuit Breaker Pattern** - Prevents cascading failures
- ✅ **Retry Logic** - Automatic retry with exponential backoff
- ✅ **Fallback Mechanisms** - Graceful degradation on service failure
- ✅ **Timeout Management** - Prevents hanging requests

### Observability
- ✅ Comprehensive Logging
- ✅ Health Checks (Spring Actuator)
- ✅ Metrics Exposure
- ✅ OpenAPI/Swagger Documentation

---

## Technology Stack

### Core Framework
- **Spring Boot 3.5.11** - Main application framework
- **Spring Web** - REST API support
- **Spring WebFlux** - Reactive programming support
- **Spring Data Redis Reactive** - Async Redis client

### Resilience & Fault Tolerance
- **Resilience4j 2.3.0** - Circuit breaker, retry, timeout patterns
- **Resilience4j CircuitBreaker** - State management
- **Resilience4j Core** - Base resilience library

### Messaging & Events
- **Apache Kafka** - Event streaming platform
- **Spring Kafka** - Kafka integration

### API & Documentation
- **SpringDoc OpenAPI 2.7.0** - Swagger/OpenAPI integration
- **Spring Actuator** - Health checks and metrics

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking library
- **Reactor Test** - Reactive testing utilities
- **OkHttp MockWebServer** - HTTP mocking
- **Spring Kafka Test** - Kafka testing utilities
- **JaCoCo 0.8.11** - Code coverage

### Development Tools
- **Maven 3.x** - Build tool
- **Java 21** - Language

---

## Project Structure

```
order-service/
├── src/main/java/com/hometask/orderservice/
│   ├── OrderServiceApplication.java          # Application entry point
│   │
│   ├── config/                               # Configuration classes
│   │   ├── KafkaConfig.java                 # Kafka producer/consumer setup
│   │   ├── WebClientConfig.java             # WebClient configuration
│   │   └── OpenApiConfig.java               # Swagger/OpenAPI configuration
│   │
│   ├── controller/                           # REST Controllers
│   │   ├── OrderController.java             # Order endpoints
│   │   └── InventoryController.java         # Inventory endpoints
│   │
│   ├── service/                              # Business Logic Services
│   │   ├── OrderService.java                # Order processing
│   │   ├── InventoryServiceClient.java      # Inventory API client
│   │   ├── KafkaProducerService.java        # Kafka message publishing
│   │   └── KafkaConsumerService.java        # Kafka message consumption
│   │
│   └── dto/                                  # Data Transfer Objects
│       ├── OrderPlaceRequest.java           # Order request DTO
│       └── ProductDTO.java                  # Product information DTO
│
├── src/main/resources/
│   └── application.properties                # Application configuration
│
├── src/test/java/com/hometask/orderservice/
│   ├── controller/
│   │   ├── InventoryControllerTest.java
│   │   ├── InventoryCircuitBreakerTest.java
│   │   └── OrderControllerTest.java
│   │
│   └── service/
│       ├── InventoryServiceClientTest.java
│       ├── OrderServiceTest.java
│       └── KafkaProducerServiceTest.java
│
├── pom.xml                                   # Maven dependencies
├── README.md                                 # This file
├── ARCHITECTURE.md                           # Architecture details
├── API_DOCUMENTATION.md                      # API specifications
├── CONFIGURATION.md                          # Configuration guide
└── TESTING.md                                # Testing guide
```

---

## Getting Started

### Prerequisites

- Java 21 JDK
- Maven 3.8+
- Docker (for running Kafka and Redis)
- Git

### Installation & Setup

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd order-service
```

#### 2. Start Dependencies (Docker)

```bash
# Start Kafka
docker run -d --name kafka -p 9092:9092 confluentinc/cp-kafka:7.5.0

# Start Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Start Inventory Service (on port 8080)
# Follow the Inventory Service README
```

#### 3. Build the Application

```bash
mvn clean install
```

#### 4. Run the Application

```bash
mvn spring-boot:run
```

Or using Java directly:

```bash
java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

The service will start on **http://localhost:8081**

#### 5. Verify Installation

```bash
# Check health endpoint
curl http://localhost:8081/actuator/health

# View API documentation
# Open http://localhost:8081/swagger-ui.html in browser
```

---

## API Documentation

### Base URL
```
http://localhost:8081/api
```

### Order Endpoints

#### Place Order
```http
POST /api/orders/place/{productId}/{quantity}
```

**Path Parameters:**
- `productId` (string, required) - The product ID
- `quantity` (integer, required) - Quantity to order

**Response (200 OK):**
```json
{
  "message": "Order placed successfully! 123.45"
}
```

**Response (400 Bad Request):**
```json
{
  "error": "Insufficient stock for product: productId"
}
```

**Response (503 Service Unavailable):**
```json
{
  "error": "Inventory service unavailable"
}
```

### Inventory Endpoints

#### Get All Products
```http
GET /api/inventory/products
```

**Response (200 OK):**
```json
[
  {
    "id": "1",
    "name": "Product Name",
    "description": "Product Description",
    "price": 99.99,
    "currency": "USD",
    "category": "Electronics",
    "stock": 10,
    "imageUrl": "http://example.com/image.jpg",
    "rating": 4.5
  }
]
```

**Response (503 Service Unavailable):**
```
null
```

---

## Configuration

### Application Properties

**File:** `src/main/resources/application.properties`

#### Server Configuration
```properties
spring.application.name=order-service
server.port=8081
```

#### Inventory Service
```properties
inventory.service.base-url=http://localhost:8080
inventory.service.timeout=5000
```

#### Circuit Breaker Configuration
```properties
resilience4j.circuitbreaker.instances.inventoryService.failure-rate-threshold=10
resilience4j.circuitbreaker.instances.inventoryService.slow-call-rate-threshold=10
resilience4j.circuitbreaker.instances.inventoryService.slow-call-duration-threshold=2s
resilience4j.circuitbreaker.instances.inventoryService.minimum-number-of-calls=10
resilience4j.circuitbreaker.instances.inventoryService.sliding-window-size=20
resilience4j.circuitbreaker.instances.inventoryService.permitted-number-of-calls-in-half-open-state=5
resilience4j.circuitbreaker.instances.inventoryService.wait-duration-in-open-state=30s
```

#### Kafka Configuration
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=orders-group
spring.kafka.consumer.auto-offset-reset=earliest
kafka.inventory.update.topic=inventory-updates
```

#### Actuator
```properties
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

### Environment Variables

You can override properties using environment variables:

```bash
export INVENTORY_SERVICE_BASE_URL=http://inventory-service:8080
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
export SERVER_PORT=8081
```

---

## Testing

### Running Tests

#### Run All Tests
```bash
mvn test
```

#### Run Specific Test Class
```bash
mvn test -Dtest=OrderServiceTest
```

#### Run with Code Coverage
```bash
mvn clean test jacoco:report
```

View coverage report at: `target/site/jacoco/index.html`

### Test Files

#### Unit Tests
- **OrderServiceTest** - Order service logic
- **KafkaProducerServiceTest** - Kafka message publishing
- **InventoryServiceClientTest** - Inventory service client

#### Integration Tests
- **InventoryControllerTest** - Inventory API endpoints
- **InventoryCircuitBreakerTest** - Circuit breaker behavior

### Test Coverage Goals

Current Coverage: **55%**

Target Coverage: **80%+**

### Writing Tests

Example test structure:

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    
    @Mock
    private DependencyService dependency;
    
    @InjectMocks
    private MyService service;
    
    @Test
    void testFeatureSuccess() {
        // Arrange
        when(dependency.method()).thenReturn(value);
        
        // Act
        Result result = service.feature();
        
        // Assert
        assertEquals(expected, result);
        verify(dependency).method();
    }
}
```

---

## Resilience Patterns

### Circuit Breaker Pattern

The Order Service implements **Resilience4j CircuitBreaker** for the Inventory Service calls.

**States:**
- **CLOSED** - Normal operation, requests pass through
- **OPEN** - Service unavailable, requests fail fast with fallback
- **HALF_OPEN** - Testing if service has recovered

**Configuration:**
```properties
failure-rate-threshold=10%          # Open circuit when 10% fail
minimum-number-of-calls=10          # Need 10 calls to evaluate
sliding-window-size=20              # Evaluate last 20 calls
wait-duration-in-open-state=30s     # Wait 30s before trying HALF_OPEN
```

**Fallback Mechanism:**
```java
@CircuitBreaker(name = "inventoryService", fallbackMethod = "fallback")
public ResponseEntity<List<ProductDTO>> getAllProducts() {
    // Main logic
}

public ResponseEntity<List<ProductDTO>> fallback(Throwable throwable) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
}
```

### Retry Logic

Automatic retry with exponential backoff:
- **Max Attempts:** 3
- **Delay:** 3 seconds
- **Strategy:** Exponential backoff

### Timeout Management

- **Connection Timeout:** 5000ms
- **Read Timeout:** 5000ms
- **Write Timeout:** 5000ms

---

## Kafka Integration

### Message Flow

```
Order Service → Kafka Topic (inventory-updates) → Inventory Service
```

### Publishing Orders

**Topic:** `inventory-updates`

**Message Format:**
```java
public record OrderPlaceRequest(String productId, int quantity) {}
```

**Producer Configuration:**
- Serializer: JSON
- Key Serializer: String
- Type Info Headers: Enabled

### Consuming Messages

Consumer group: `orders-group`

**Consumer Configuration:**
- Deserializer: JSON
- Value Default Type: `java.util.Map`
- Auto Offset Reset: `earliest`

### Message Publishing Example

```java
OrderPlaceRequest order = new OrderPlaceRequest("product123", 5);
kafkaProducerService.sendOrderPlacedMessage(order);
```

---

## Health Checks

### Liveness Probe
```bash
curl http://localhost:8081/actuator/health/liveness
```

### Readiness Probe
```bash
curl http://localhost:8081/actuator/health/readiness
```

### Detailed Health
```bash
curl http://localhost:8081/actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "kafka": { "status": "UP" },
    "redis": { "status": "UP" },
    "db": { "status": "UP" }
  }
}
```

---

## Troubleshooting

### Common Issues

#### 1. Inventory Service Connection Failed

**Error:**
```
java.lang.RuntimeException: Service unavailable
```

**Solution:**
- Verify Inventory Service is running on port 8080
- Check network connectivity
- Review `inventory.service.base-url` configuration

#### 2. Kafka Connection Failed

**Error:**
```
org.apache.kafka.common.errors.TimeoutException
```

**Solution:**
- Ensure Kafka broker is running on localhost:9092
- Check `spring.kafka.bootstrap-servers` configuration
- Verify Docker container is running: `docker ps`

#### 3. CircuitBreaker Not Opening

**Issue:** Circuit breaker remains CLOSED even with failures

**Solution:**
- Check `minimum-number-of-calls` configuration
- Verify failure rate exceeds `failure-rate-threshold`
- Review CircuitBreaker state: `/actuator/circuitbreakers`

#### 4. High Latency

**Symptoms:** Slow response times

**Solution:**
- Check Inventory Service performance
- Verify network latency
- Review Redis cache hit rate
- Adjust timeout values if needed

### Debug Endpoints

```bash
# Circuit breaker status
curl http://localhost:8081/actuator/circuitbreakers

# Metrics
curl http://localhost:8081/actuator/metrics

# Environment
curl http://localhost:8081/actuator/env

# Logs (if enabled)
curl http://localhost:8081/actuator/loggers
```

---

## Monitoring & Metrics

### Available Metrics

- `resilience4j_circuitbreaker_calls` - Circuit breaker call count
- `resilience4j_circuitbreaker_state` - Circuit breaker state (0=CLOSED, 1=OPEN, 2=HALF_OPEN)
- `http_requests_total` - Total HTTP requests
- `http_request_duration_seconds` - Request duration histogram

### Prometheus Integration

Metrics are exposed at:
```
http://localhost:8081/actuator/prometheus
```

---

## Contributing

### Code Style

- Use meaningful variable names
- Add JavaDoc for public methods
- Follow Spring conventions
- Write unit tests for new features

### Pull Request Process

1. Create a feature branch
2. Write tests for new functionality
3. Ensure all tests pass: `mvn clean test`
4. Verify code coverage: `mvn jacoco:report`
5. Submit PR with description

---

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [OpenAPI/Swagger Specification](https://swagger.io/specification/)

---

**Last Updated:** February 2026  
**Maintainers:** Development Team  
**License:** [Your License Here]

