# API Documentation

## Order Service REST API Reference

This document provides comprehensive API documentation for the Order Service.

## Table of Contents

1. [Base URL & Authentication](#base-url--authentication)
2. [Order Endpoints](#order-endpoints)
3. [Inventory Endpoints](#inventory-endpoints)
4. [Health & Monitoring Endpoints](#health--monitoring-endpoints)
5. [Error Responses](#error-responses)
6. [Data Models](#data-models)
7. [Example Workflows](#example-workflows)

---

## Base URL & Authentication

### Base URL
```
http://localhost:8081/api
```

### Authentication
Currently, the API does not require authentication (development mode).

**Note:** For production, implement OAuth2/JWT authentication.

### Headers

All requests should include:
```
Content-Type: application/json
Accept: application/json
```

### API Versioning
Current API Version: `v1` (implicit)

Future versions: `/api/v2/...`

---

## Order Endpoints

### Place Order

**Endpoint:** `POST /api/orders/place/{productId}/{quantity}`

**Description:** Create a new order for a product

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| productId | string | Yes | Unique product identifier |
| quantity | integer | Yes | Number of units to order (1-1000) |

**Query Parameters:** None

**Request Body:** None

**Request Example:**
```bash
curl -X POST "http://localhost:8081/api/orders/place/PROD001/5" \
  -H "Content-Type: application/json"
```

**Response (200 OK) - Success:**
```json
{
  "message": "Order placed successfully! 847.45"
}
```

**Response (400 Bad Request) - Insufficient Stock:**
```json
{
  "error": "Insufficient stock for product: PROD001"
}
```

**Response (503 Service Unavailable) - Service Error:**
```json
{
  "error": "Inventory service unavailable"
}
```

**Status Codes:**
| Code | Meaning | Scenario |
|------|---------|----------|
| 200 | OK | Order placed successfully |
| 400 | Bad Request | Product not found or insufficient stock |
| 503 | Service Unavailable | Inventory service down |

**Business Logic:**
1. Validates product ID exists
2. Checks stock availability for requested quantity
3. Creates order event
4. Publishes to Kafka `inventory-updates` topic
5. Returns confirmation

**Error Handling:**
- Product not found → 400 Bad Request
- Stock insufficient → 400 Bad Request
- Service unavailable → 503 Service Unavailable (fallback)
- Kafka failure → Exception logged, order rejected

---

## Inventory Endpoints

### Get All Products

**Endpoint:** `GET /api/inventory/products`

**Description:** Retrieve all available products from inventory service

**Path Parameters:** None

**Query Parameters:** None

**Request Body:** None

**Request Example:**
```bash
curl -X GET "http://localhost:8081/api/inventory/products" \
  -H "Content-Type: application/json"
```

**Response (200 OK) - Success:**
```json
[
  {
    "id": "PROD001",
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "currency": "USD",
    "category": "Electronics",
    "stock": 15,
    "imageUrl": "https://example.com/laptop.jpg",
    "rating": 4.8
  },
  {
    "id": "PROD002",
    "name": "Headphones",
    "description": "Wireless noise-canceling headphones",
    "price": 199.99,
    "currency": "USD",
    "category": "Electronics",
    "stock": 50,
    "imageUrl": "https://example.com/headphones.jpg",
    "rating": 4.5
  }
]
```

**Response (200 OK) - Empty List:**
```json
[]
```

**Response (503 Service Unavailable) - Fallback:**
```
null
```

**Status Codes:**
| Code | Meaning | Scenario |
|------|---------|----------|
| 200 | OK | Products retrieved successfully |
| 503 | Service Unavailable | Inventory service down (fallback) |

**Circuit Breaker Behavior:**

**State: CLOSED** (Normal)
- Requests pass through to Inventory Service
- Response returned normally
- Timeout: 5000ms

**State: OPEN** (Service Failed)
- Requests fail fast
- Fallback method returns 503
- Wait 30 seconds before retrying

**State: HALF_OPEN** (Recovery Testing)
- Limited requests allowed through
- If succeed → CLOSED
- If fail → OPEN again

**Retry Logic:**
- Max attempts: 3
- Initial delay: 3 seconds
- Strategy: Exponential backoff
- Only on transient failures (not 4xx)

---

## Health & Monitoring Endpoints

### Health Check (Detailed)

**Endpoint:** `GET /actuator/health`

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "kafka": {
      "status": "UP",
      "details": {
        "brokerId": 1,
        "clusterId": "abc123"
      }
    },
    "redisReactive": {
      "status": "UP"
    },
    "livenessState": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    }
  }
}
```

### Liveness Probe

**Endpoint:** `GET /actuator/health/liveness`

**Purpose:** Kubernetes liveness check

**Response (200 OK):**
```json
{
  "status": "UP"
}
```

### Readiness Probe

**Endpoint:** `GET /actuator/health/readiness`

**Purpose:** Kubernetes readiness check

**Response (200 OK):**
```json
{
  "status": "UP"
}
```

### Circuit Breaker Status

**Endpoint:** `GET /actuator/circuitbreakers`

**Response (200 OK):**
```json
{
  "circuitBreakers": [
    {
      "name": "inventoryService",
      "status": "CLOSED",
      "details": {
        "slowCallRate": 0.0,
        "failureRate": 0.0,
        "numberOfBufferedCalls": 5,
        "numberOfFailedCalls": 0,
        "numberOfNotPermittedCalls": 0,
        "numberOfSuccessfulCalls": 5,
        "numberOfSlowCalls": 0,
        "numberOfSlowSuccessfulCalls": 0,
        "lastRecordedException": null
      }
    }
  ]
}
```

### Metrics

**Endpoint:** `GET /actuator/metrics`

**Available Metrics:**
```json
{
  "names": [
    "jvm.memory.used",
    "http.server.requests",
    "resilience4j.circuitbreaker.calls",
    "resilience4j.circuitbreaker.state",
    "process.cpu.usage",
    "system.cpu.usage",
    ...
  ]
}
```

**Get Specific Metric:**
```bash
# Circuit breaker call count
curl http://localhost:8081/actuator/metrics/resilience4j.circuitbreaker.calls
```

---

## Error Responses

### Standard Error Format

All error responses follow this format:

```json
{
  "timestamp": "2026-02-26T10:30:45.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message",
  "path": "/api/orders/place/INVALID/abc"
}
```

### HTTP Status Codes

| Code | Name | Meaning | Common Causes |
|------|------|---------|---------------|
| 200 | OK | Request succeeded | - |
| 400 | Bad Request | Invalid parameters | Product not found, invalid quantity |
| 500 | Internal Server Error | Server error | Unexpected exception, bugs |
| 503 | Service Unavailable | Service down | External service unavailable |

### Common Error Scenarios

#### 1. Product Not Found
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Product not found: INVALID_PRODUCT"
}
```

#### 2. Invalid Quantity
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Quantity must be between 1 and 1000"
}
```

#### 3. Insufficient Stock
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for product: PROD001. Available: 5, Requested: 10"
}
```

#### 4. Inventory Service Down
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Inventory service is currently unavailable"
}
```

#### 5. Kafka Connection Error
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to publish order event to message broker"
}
```

---

## Data Models

### ProductDTO

Represents a product from the Inventory Service.

```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "price": "number",
  "currency": "string (ISO 4217)",
  "category": "string",
  "stock": "integer",
  "imageUrl": "string (URL)",
  "rating": "number (0-5)"
}
```

**Field Descriptions:**
| Field | Type | Example | Notes |
|-------|------|---------|-------|
| id | string | "PROD001" | Unique identifier |
| name | string | "Laptop" | Product name |
| description | string | "High-performance laptop" | Product description |
| price | number | 999.99 | Unit price |
| currency | string | "USD" | ISO 4217 code |
| category | string | "Electronics" | Product category |
| stock | integer | 15 | Available quantity |
| imageUrl | string | "https://..." | Product image URL |
| rating | number | 4.8 | Average rating (0-5) |

### OrderPlaceRequest

Represents an order placement request (used internally for Kafka messaging).

```java
public record OrderPlaceRequest(
    String productId,
    int quantity
)
```

**JSON Representation:**
```json
{
  "productId": "PROD001",
  "quantity": 5
}
```

**Field Descriptions:**
| Field | Type | Example | Notes |
|-------|------|---------|-------|
| productId | string | "PROD001" | Product to order |
| quantity | integer | 5 | Quantity to order |

---

## Example Workflows

### Workflow 1: Successful Order Placement

```
1. Client initiates order
   GET /api/inventory/products
   
   Response: [Product list with stock info]
   
2. Client places order
   POST /api/orders/place/PROD001/5
   
   Response (200 OK):
   {
     "message": "Order placed successfully! 847.45"
   }
   
3. Order is published to Kafka
   Topic: inventory-updates
   Message: {
     "productId": "PROD001",
     "quantity": 5
   }
```

### Workflow 2: Insufficient Stock Handling

```
1. Client initiates order
   GET /api/inventory/products
   
   Response: Product has 3 units in stock
   
2. Client attempts to order 5 units
   POST /api/orders/place/PROD001/5
   
   Response (400 Bad Request):
   {
     "error": "Insufficient stock for product: PROD001"
   }
   
3. Client retries with available quantity
   POST /api/orders/place/PROD001/3
   
   Response (200 OK):
   {
     "message": "Order placed successfully! 508.47"
   }
```

### Workflow 3: Service Failure & Fallback

```
1. Client requests products
   GET /api/inventory/products
   
2. Inventory Service is down
   
3. Circuit Breaker detects failure
   State: CLOSED → OPEN
   
4. Subsequent requests fail fast
   GET /api/inventory/products
   
   Response (503 Service Unavailable):
   null
   
5. After 30 seconds, circuit enters HALF_OPEN
   Testing if service has recovered
   
6. If service is back:
   State: HALF_OPEN → CLOSED
   Normal operation resumes
```

### Workflow 4: Retry on Transient Failure

```
1. Client initiates order
   POST /api/orders/place/PROD001/5
   
2. Inventory Service temporarily unavailable
   
3. Retry mechanism triggers
   Attempt 1: FAIL (connection timeout)
   Wait 3 seconds
   Attempt 2: FAIL (network error)
   Wait 6 seconds (exponential backoff)
   Attempt 3: SUCCESS
   
4. Order placed successfully
   Response (200 OK):
   {
     "message": "Order placed successfully! 847.45"
   }
```

---

## API Best Practices

### Request Validation

Always validate inputs:
```bash
# Valid request
curl -X POST "http://localhost:8081/api/orders/place/PROD001/5"

# Invalid - missing quantity
curl -X POST "http://localhost:8081/api/orders/place/PROD001"
# → 404 Not Found

# Invalid - non-numeric quantity
curl -X POST "http://localhost:8081/api/orders/place/PROD001/abc"
# → 400 Bad Request
```

### Error Handling Recommendations

1. **Always check HTTP status code**
   - 2xx = Success
   - 4xx = Client error (fix request)
   - 5xx = Server error (retry later)

2. **Implement exponential backoff** for retries
   ```
   Retry after: 1s, 2s, 4s, 8s, ...
   Max retries: 3-5
   ```

3. **Use circuit breaker pattern** for external calls
   - Fail fast after repeated failures
   - Recover gracefully

4. **Log all errors** for debugging
   - Timestamp
   - Error code
   - Request ID
   - Stack trace

### Rate Limiting (Future)

**Coming soon:** API rate limiting
```
- 100 requests per minute (per IP)
- Headers: X-RateLimit-Limit, X-RateLimit-Remaining
```

---

## OpenAPI/Swagger Documentation

### Access Swagger UI
```
http://localhost:8081/swagger-ui.html
```

### Access OpenAPI JSON
```
http://localhost:8081/v3/api-docs
```

### OpenAPI YAML
```
http://localhost:8081/v3/api-docs.yaml
```

---

**API Version:** 1.0  
**Document Version:** 1.0  
**Last Updated:** February 2026  
**Next Review:** Q2 2026

