# Architecture Documentation

## System Architecture Overview

This document provides detailed information about the Order Service architecture, component interactions, and design patterns.

## Table of Contents

1. [Component Architecture](#component-architecture)
2. [Layer Architecture](#layer-architecture)
3. [Data Flow](#data-flow)
4. [Design Patterns](#design-patterns)
5. [External Integrations](#external-integrations)
6. [Deployment Architecture](#deployment-architecture)

---

## Component Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      Order Service                           │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                  REST Endpoints                      │   │
│  │  ┌──────────────┐      ┌──────────────┐            │   │
│  │  │ OrderController   │InventoryController│           │   │
│  │  └──────────────┘      └──────────────┘            │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     │                                        │
│  ┌──────────────────▼──────────────────────────────────┐   │
│  │                  Services                           │   │
│  │  ┌──────────────┐  ┌──────────────────┐            │   │
│  │  │OrderService  │  │InventoryServiceClient │         │   │
│  │  └──────────────┘  └──────────────────┘            │   │
│  │  ┌──────────────────────────────────────┐          │   │
│  │  │  KafkaProducerService                │          │   │
│  │  └──────────────────────────────────────┘          │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Cross-Cutting Concerns                 │   │
│  │  ┌──────────────────────────────────────────────┐  │   │
│  │  │ Circuit Breaker (Resilience4j)              │  │   │
│  │  │ Retry Logic                                  │  │   │
│  │  │ Timeout Management                           │  │   │
│  │  └──────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Infrastructure                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │   │
│  │  │WebClient │  │KafkaConfig│  │RedisConfig      │  │   │
│  │  └──────────┘  └──────────┘  └──────────────────┘  │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
   ┌─────────────┐     ┌─────────────┐     ┌────────────────┐
   │ Inventory   │     │   Kafka     │     │    Redis       │
   │  Service    │     │   Broker    │     │    Cache       │
   │ (REST API)  │     │  (Message)  │     │   (Reactive)   │
   └─────────────┘     └─────────────┘     └────────────────┘
```

### Key Components

#### 1. Controllers
- **OrderController** - Handles order-related REST endpoints
  - `/api/orders/place/{productId}/{quantity}` - Place new orders
  
- **InventoryController** - Manages inventory-related endpoints
  - `/api/inventory/products` - Retrieve all products
  - Includes Circuit Breaker for fault tolerance

#### 2. Services
- **OrderService** - Business logic for order processing
  - Validates stock availability
  - Publishes order events to Kafka
  - Generates order confirmation
  
- **InventoryServiceClient** - External service integration
  - Communicates with Inventory Service via REST
  - Implements WebClient with timeouts
  - Retry logic with exponential backoff
  
- **KafkaProducerService** - Event publishing
  - Sends order placement messages
  - Publishes to `inventory-updates` topic
  - JSON serialization support

- **KafkaConsumerService** - Event consumption
  - Listens for messages (currently disabled)
  - Placeholder for async processing

#### 3. Configuration Classes
- **WebClientConfig** - HTTP client setup
  - Configures timeouts (5000ms default)
  - Adds request/response logging
  - Sets up connection pooling
  
- **KafkaConfig** - Message broker setup
  - Producer factory configuration
  - Consumer factory configuration
  - Serialization/deserialization setup
  
- **OpenApiConfig** - API documentation
  - Swagger/OpenAPI integration
  - API metadata configuration

#### 4. Data Transfer Objects (DTOs)
- **ProductDTO** - Product information
  - Represents product details from Inventory Service
  - Includes price, stock, rating, etc.
  
- **OrderPlaceRequest** - Order placement request
  - Simple record with productId and quantity
  - Used for Kafka messaging

---

## Layer Architecture

### Multi-Layer Architecture

```
┌─────────────────────────────────────────────────┐
│          Presentation Layer                      │
│  (REST Endpoints / HTTP Controllers)            │
├─────────────────────────────────────────────────┤
│          Application Layer                       │
│  (Business Logic & Orchestration)               │
│  - OrderService                                 │
│  - KafkaProducerService                         │
├─────────────────────────────────────────────────┤
│          Integration Layer                       │
│  (External Service Communication)               │
│  - InventoryServiceClient                       │
│  - KafkaProducerService                         │
├─────────────────────────────────────────────────┤
│          Infrastructure Layer                    │
│  (Configuration & Framework)                    │
│  - WebClientConfig                              │
│  - KafkaConfig                                  │
│  - Spring Boot Framework                        │
├─────────────────────────────────────────────────┤
│          Cross-Cutting Concerns                  │
│  - Resilience4j Circuit Breaker                │
│  - Logging                                      │
│  - Exception Handling                           │
└─────────────────────────────────────────────────┘
```

### Responsibility by Layer

#### Presentation Layer
- Expose REST APIs
- Validate incoming requests
- Return HTTP responses
- Handle request mapping

#### Application Layer
- Process business logic
- Coordinate between services
- Implement business rules
- Generate responses

#### Integration Layer
- Call external services
- Implement resilience patterns
- Handle service errors
- Manage retries

#### Infrastructure Layer
- Configure frameworks
- Setup connection pools
- Configure serialization
- Setup logging

---

## Data Flow

### Order Placement Flow

```
Client Request
    │
    ▼
┌─────────────────────────────┐
│  OrderController.placeOrder │
│  POST /api/orders/place/... │
└──────────────┬──────────────┘
               │
               ▼
        ┌─────────────────┐
        │  Validate Input │
        │ (productId, qty)│
        └────────┬────────┘
                 │
                 ▼
     ┌──────────────────────────┐
     │ InventoryController      │
     │  .getAllProducts()        │
     │ [Circuit Breaker]         │
     └──────────────┬────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
   YES  ▼                       ▼  NO/FAIL
 ┌────────────────┐      ┌─────────────────┐
 │ Stock Check    │      │ Return 503 or   │
 │ Sufficient?    │      │ 400 Error       │
 └────────┬───────┘      └─────────────────┘
          │
   YES    ▼
 ┌──────────────────────┐
 │  OrderService        │
 │  .placeOrder()       │
 └──────────┬───────────┘
            │
            ▼
  ┌──────────────────────────┐
  │ KafkaProducerService     │
  │ .sendOrderPlacedMessage()│
  └──────────┬───────────────┘
             │
             ▼
    ┌─────────────────────┐
    │  Kafka Broker       │
    │  Topic: inventory-  │
    │  updates            │
    └─────────────────────┘
             │
             ▼
       Return Response
       (200 OK + Message)
```

### Inventory Fetch Flow

```
Client Request
    │
    ▼
┌─────────────────────────────────┐
│InventoryController.getAllProducts│
│ GET /api/inventory/products      │
└──────────────┬──────────────────┘
               │
       ┌───────▼────────┐
       │ Circuit Breaker│ (Check State)
       └───────┬────────┘
               │
       ┌───────▼──────────┐
       │ CLOSED/HALF_OPEN?│
       └───────┬──────────┘
       YES     │
               ▼
   ┌──────────────────────────────┐
   │ InventoryServiceClient       │
   │ .getAllProducts()            │
   │  - Create WebClient Request  │
   │  - Add Timeouts              │
   │  - Retry Logic (backoff)     │
   └──────────┬───────────────────┘
              │
              ▼
   ┌──────────────────────────┐
   │ Inventory Service        │
   │ /api/products/all        │
   └──────────┬───────────────┘
              │
    ┌─────────┴──────────┐
    │                    │
    ▼ 200 OK            ▼ Error
 Success              Failure
    │                    │
    ▼                    ▼
 Return            Retry (0-3)
 Products          │
                   ├─ Success → Return
                   │
                   └─ All Fail → Circuit OPEN
                               Return Fallback
```

---

## Design Patterns

### 1. Circuit Breaker Pattern

**Purpose:** Prevent cascading failures when calling external services

**Implementation:**
```
States:
- CLOSED: Requests pass through normally
- OPEN: Requests fail fast, fallback method called
- HALF_OPEN: Limited requests allowed to test recovery

Triggers:
- Failure rate threshold: 10%
- Minimum calls to evaluate: 10
- Sliding window: last 20 calls
```

**Code Example:**
```java
@CircuitBreaker(name = "inventoryService", fallbackMethod = "getStaticProductsFallback")
public ResponseEntity<List<ProductDTO>> getAllProducts() {
    // Main logic
}

public ResponseEntity<List<ProductDTO>> getStaticProductsFallback(Throwable throwable) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
}
```

### 2. Retry Pattern

**Purpose:** Handle transient failures by retrying

**Implementation:**
- Max attempts: 3
- Initial delay: 3 seconds
- Strategy: Exponential backoff

**Code Example:**
```java
.retryWhen(Retry.backoff(RETRY_ATTEMPTS, RETRY_DELAY)
    .doBeforeRetry(signal -> logger.warn("Retrying..."))
    .onRetryExhaustedThrow((spec, signal) -> 
        new RuntimeException("Max retries exceeded")))
```

### 3. Timeout Pattern

**Purpose:** Prevent hanging requests

**Configuration:**
```properties
# WebClient Timeouts
connection-timeout: 5000ms
read-timeout: 5000ms
write-timeout: 5000ms
```

### 4. Fallback Pattern

**Purpose:** Provide graceful degradation

**Implementation:**
```java
// When Inventory Service fails
// Return HTTP 503 (Service Unavailable)
// Instead of throwing exception
```

### 5. Repository Pattern (DTO)

**Purpose:** Separate data access from business logic

**Implementation:**
```java
// ProductDTO - Represents external data
// OrderPlaceRequest - Represents internal data
```

### 6. Dependency Injection

**Purpose:** Loose coupling between components

**Implementation:**
```java
@Service
public class OrderService {
    private final KafkaProducerService kafkaProducerService;
    
    public OrderService(KafkaProducerService producer) {
        this.kafkaProducerService = producer;
    }
}
```

### 7. Reactive Programming

**Purpose:** Non-blocking I/O operations

**Implementation:**
```java
// WebClient with reactive WebFlux
// Redis with reactive driver
// Mono/Flux for async operations
```

---

## External Integrations

### 1. Inventory Service Integration

**Type:** Synchronous REST API

**Base URL:** `http://localhost:8080`

**Endpoint:** `GET /api/products/all`

**Features:**
- WebClient for HTTP communication
- Timeout handling (5 seconds)
- Retry with exponential backoff
- Circuit breaker protection
- Request/Response logging

**Response Format:**
```json
[
  {
    "id": "1",
    "name": "Product",
    "price": 99.99,
    "stock": 10,
    ...
  }
]
```

### 2. Kafka Integration

**Type:** Asynchronous Message Broker

**Bootstrap Servers:** `localhost:9092`

**Topics:**
- `inventory-updates` - Order placement events

**Message Format:**
```java
public record OrderPlaceRequest(
    String productId,
    int quantity
)
```

**Configuration:**
- Producer: JSON serialization
- Consumer: JSON deserialization
- Consumer Group: `orders-group`
- Auto Offset Reset: `earliest`

### 3. Redis Integration

**Type:** Reactive Cache

**Address:** `localhost:6379`

**Features:**
- Reactive Redis client
- Async operations
- Cache storage for frequently accessed data

---

## Deployment Architecture

### Containerized Deployment

```
┌────────────────────────────────────────────────────┐
│              Docker/Kubernetes                     │
│                                                    │
│  ┌──────────────┐  ┌────────────────────────────┐│
│  │Order Service │  │ Environment Variables      ││
│  │Container     │  │ - Service URLs             ││
│  │(Port 8081)   │  │ - Kafka Brokers            ││
│  └──────────────┘  │ - Redis Address            ││
│                    └────────────────────────────┘│
│                                                    │
│  ┌──────────────────────────────────────────────┐│
│  │  Service Dependencies (separate containers)  ││
│  │  - Inventory Service (Port 8080)             ││
│  │  - Kafka Broker (Port 9092)                  ││
│  │  - Redis (Port 6379)                         ││
│  └──────────────────────────────────────────────┘│
│                                                    │
└────────────────────────────────────────────────────┘
```

### Environment Configuration

**Development:**
```properties
INVENTORY_SERVICE_BASE_URL=http://localhost:8080
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_REDIS_HOST=localhost
```

**Production:**
```properties
INVENTORY_SERVICE_BASE_URL=http://inventory-service:8080
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
SPRING_REDIS_HOST=redis
```

---

## Performance Considerations

### Caching Strategy
- Redis for product caching
- TTL: Configurable per use case
- Cache invalidation on updates

### Timeout Configuration
- Connection: 5000ms
- Read: 5000ms
- Write: 5000ms
- Adjust based on network conditions

### Circuit Breaker Tuning
- Failure threshold: 10%
- Window size: 20 calls
- Open state duration: 30 seconds
- Monitor metrics for optimization

### Kafka Batch Processing
- Batch size: Default Spring Kafka settings
- Consumer concurrency: 3 threads
- Monitor lag and throughput

---

## Security Considerations

### Current Implementation
- No authentication/authorization (development mode)
- CORS not explicitly configured
- Input validation at controller level

### Recommended Enhancements
1. Add Spring Security
2. Implement API authentication (JWT/OAuth2)
3. Add request validation
4. Enable CORS with restricted origins
5. Add rate limiting
6. Implement audit logging

---

## Monitoring & Observability

### Available Metrics
- Circuit breaker state changes
- Request latency
- Kafka message count
- Redis cache hits
- HTTP error rates

### Health Checks
- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`
- Detailed: `/actuator/health`

### Logging
- Framework: SLF4J with Logback
- Levels: INFO, WARN, ERROR
- Context: Request ID, Service name, Timestamps

---

**Document Version:** 1.0  
**Last Updated:** February 2026  
**Next Review:** Q2 2026

