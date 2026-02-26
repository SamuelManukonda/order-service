# Order Service - Comprehensive Testing Guide

## Table of Contents

1. [Overview](#overview)
2. [Test Structure](#test-structure)
3. [Unit Tests](#unit-tests)
4. [Integration Tests](#integration-tests)
5. [End-to-End Tests](#end-to-end-tests)
6. [Circuit Breaker Tests](#circuit-breaker-tests)
7. [Running Tests](#running-tests)
8. [Code Coverage](#code-coverage)

---

## Overview

This document provides a comprehensive guide to the testing strategy implemented for the Order Service microservice. The test suite covers:

- **Unit Tests**: Testing individual components in isolation
- **Integration Tests**: Testing component interactions
- **End-to-End Tests**: Complete workflow testing through the API
- **Circuit Breaker State Tests**: Resilience pattern verification

### Test Framework Stack

- **Testing Framework**: JUnit 5 (Jupiter)
- **Mocking**: Mockito 5.x
- **Web Testing**: Spring Test (MockMvc)
- **HTTP Mocking**: OkHttp3 MockWebServer
- **Coverage**: JaCoCo 0.8.12
- **Resilience Testing**: Resilience4j test utilities

---

## Test Structure

```
src/test/java/com/hometask/orderservice/
├── OrderServiceIntegrationTest.java          # E2E and integration tests
├── OrderServiceApplicationTests.java         # Context loading tests
├── controller/
│   ├── InventoryControllerTest.java          # Controller unit tests
│   ├── OrderControllerTest.java              # Order controller unit tests
│   └── CircuitBreakerStateTransitionTest.java # Resilience pattern tests
├── service/
│   ├── OrderServiceTest.java                 # Order service logic tests
│   ├── KafkaProducerServiceTest.java         # Kafka producer tests
│   ├── InventoryServiceClientTest.java       # HTTP client tests
│   └── KafkaConsumerServiceTest.java         # Kafka consumer tests
└── config/
    ├── OpenApiConfigTest.java                # OpenAPI configuration tests
    ├── KafkaConfigTest.java                  # Kafka configuration tests
    └── WebClientConfigTest.java              # WebClient configuration tests
```

---

## Unit Tests

### InventoryControllerTest

Tests the Inventory Controller in isolation using mocks.

**Covered Scenarios:**
- ✅ Successful product retrieval
- ✅ Service unavailability fallback
- ✅ Empty product list handling
- ✅ Exception handling

**Key Test Methods:**
```java
testGetAllProducts_Success()           // Happy path
testGetAllProducts_Fallback()          // Fallback mechanism
testGetAllProducts_EmptyList()         // Edge case
testGetAllProducts_Exception()         // Error handling
```

### OrderControllerTest

Tests the Order Controller logic without external dependencies.

**Covered Scenarios:**
- ✅ Successful order placement with sufficient stock
- ✅ Order rejection due to insufficient stock
- ✅ Product not found error
- ✅ Minimum quantity order (1)
- ✅ Maximum available stock order
- ✅ Multiple sequential orders
- ✅ Empty product list handling

**Key Test Methods:**
```java
testPlaceOrder_Success()               // Happy path
testPlaceOrder_InsufficientStock()     // Validation
testPlaceOrder_ProductNotFound()       // Error handling
testPlaceOrder_MinimumQuantity()       // Boundary
testPlaceOrder_MaximumStock()          // Boundary
testPlaceMultipleOrders()              // Sequence
```

### OrderServiceTest

Tests the core business logic of the Order Service.

**Covered Scenarios:**
- ✅ Successful order placement
- ✅ Different product IDs
- ✅ Minimum quantity handling
- ✅ Kafka message verification

**Key Test Methods:**
```java
testPlaceOrder_Success()               // Happy path
testPlaceOrder_WithDifferentProductId() // Variation
testPlaceOrder_WithMinimumQuantity()   // Boundary
testPlaceOrder_VerifyKafkaInteraction() // Integration
```

### KafkaProducerServiceTest

Tests Kafka message publishing functionality.

**Covered Scenarios:**
- ✅ Simple message sending
- ✅ Complex object serialization
- ✅ Inventory update messages
- ✅ Topic verification

**Key Test Methods:**
```java
testSendMessage_Success()              // Basic
testSendMessage_WithComplexObject()    // Serialization
testUpdateInventoryMessage_Success()   // Domain logic
testUpdateInventoryMessage_VerifyCorrectTopic() // Verification
```

### KafkaConsumerServiceTest

Tests Kafka consumer service initialization.

**Covered Scenarios:**
- ✅ Bean instantiation
- ✅ Service availability

### InventoryServiceClientTest

Tests HTTP client communication with Inventory Service.

**Covered Scenarios:**
- ✅ Successful API calls
- ✅ Error responses
- ✅ Retry mechanisms
- ✅ Network timeouts

### Config Tests

Tests configuration beans (OpenApiConfig, KafkaConfig, WebClientConfig).

**Covered Scenarios:**
- ✅ Bean creation and wiring
- ✅ Configuration property binding
- ✅ Dependency injection

---

## Integration Tests

### OrderServiceIntegrationTest

End-to-end integration tests that test complete workflows through the API layer.

**Test Categories:**

#### 1. Inventory Endpoint Tests

**Scenario: Get all products successfully**
```
GET /api/inventory/products
→ Controller calls InventoryServiceClient
→ Client makes HTTP GET to external service
→ Response is deserialized and returned
✓ Status: 200 OK
✓ Body: Array of ProductDTO objects
```

**Scenario: Handle inventory service failure with fallback**
```
GET /api/inventory/products
→ External service returns 503
→ Circuit breaker triggers fallback
✓ Status: 503 Service Unavailable
```

**Scenario: Get products with empty response**
```
GET /api/inventory/products
→ Service returns empty array
✓ Status: 200 OK
✓ Body: Empty array []
```

#### 2. Order Placement Tests

**Scenario: Place order successfully with sufficient stock**
```
POST /api/orders/place/{productId}/{quantity}
→ Retrieve all products from inventory
→ Validate product exists and stock available
→ Place order through OrderService
→ Send Kafka messages
✓ Status: 200 OK
✓ Body: Success message
```

**Scenario: Place order fails with insufficient stock**
```
POST /api/orders/place/{productId}/{quantity}
→ Retrieve all products
→ Validate stock < requested quantity
✓ Status: 400 Bad Request
✓ Body: Insufficient stock message
```

**Scenario: Place order fails when product not found**
```
POST /api/orders/place/nonexistent/1
→ Retrieve all products
→ Product not in list
✓ Status: 500 Internal Server Error
```

**Scenario: Multiple sequential order placements**
```
POST /api/orders/place/{productId}/{quantity} (3 times)
→ Each request validates stock independently
→ Multiple Kafka messages sent
✓ Status: 200 OK (all)
```

---

## End-to-End Tests

### OrderServiceIntegrationTest

Comprehensive E2E tests covering the entire request-response cycle.

**Key Features:**

1. **MockWebServer Integration**
   - Simulates external Inventory Service
   - Configurable responses for various scenarios
   - No actual network calls

2. **Complete Workflow Testing**
   - Request creation
   - Controller processing
   - Service orchestration
   - External service calls
   - Response validation

3. **Boundary Testing**
   - Minimum quantity (1)
   - Maximum quantity (500+)
   - Stock variations

4. **Error Scenarios**
   - Service unavailability (503)
   - Timeout (504)
   - Invalid responses (400)

---

## Circuit Breaker Tests

### CircuitBreakerStateTransitionTest

Tests the Resilience4j circuit breaker pattern state transitions.

**Circuit Breaker States:**

```
CLOSED (Normal Operation)
    ↓
    [Failures exceed threshold]
    ↓
OPEN (Rejecting calls)
    ↓
    [Wait duration expires]
    ↓
HALF_OPEN (Testing recovery)
    ↓
    [Success or failure]
    ↓
CLOSED or OPEN
```

**Covered Test Scenarios:**

1. **CLOSED State - Successful Calls**
   - Circuit breaker allows requests
   - Successful response returned
   - Circuit remains CLOSED

2. **CLOSED → OPEN Transition**
   - Multiple failures occur
   - Failure rate threshold exceeded
   - Circuit transitions to OPEN
   - Subsequent calls rejected

3. **OPEN State - Rejection**
   - Circuit breaker rejects all calls
   - Fallback mechanism triggered
   - No external service calls made

4. **OPEN → HALF_OPEN Transition**
   - Wait duration expires
   - Next call allowed as test
   - Response determines next state

5. **HALF_OPEN State - Test Call**
   - Limited calls allowed
   - Success: HALF_OPEN → CLOSED
   - Failure: HALF_OPEN → OPEN

6. **Recovery After Failures**
   - Initial failure occurrence
   - Subsequent successful call
   - Circuit transitions to CLOSED
   - Normal operation resumes

7. **Fallback Mechanism**
   - When circuit is OPEN
   - `getStaticProductsFallback()` is invoked
   - Returns 503 status code

8. **Retry Mechanism**
   - Transient failures trigger retries
   - Fixed delay between retries
   - Maximum 3 retry attempts
   - Success on retry closes circuit

**Test Configuration:**
```properties
minimum-number-of-calls=3              # For state transition
failure-rate-threshold=50%             # Failure percentage
sliding-window-size=5                  # Call window
wait-duration-in-open-state=5s         # Recovery wait time
```

---

## Running Tests

### Run All Tests

```bash
mvn clean test
```

### Run Specific Test Class

```bash
mvn test -Dtest=OrderServiceIntegrationTest
mvn test -Dtest=InventoryControllerTest
mvn test -Dtest=CircuitBreakerStateTransitionTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=OrderServiceIntegrationTest#testPlaceOrder_Success
```

### Run with Coverage Report

```bash
mvn clean test jacoco:report
# Open: target/site/jacoco/index.html
```

### Run Tests in Parallel

```bash
mvn test -Dparallel=methods -DthreadCount=4
```

---

## Code Coverage

### Coverage Goals

| Component | Target | Current |
|-----------|--------|---------|
| Controllers | > 85% | TBD |
| Services | > 90% | TBD |
| Config | > 80% | TBD |
| Overall | > 85% | TBD |

### Generating Coverage Report

```bash
mvn clean test jacoco:report
```

**Report Location:** `target/site/jacoco/index.html`

### Coverage Exclusions

The following are excluded from coverage:
- Lombok-generated methods (@Data, @Getter, @Setter)
- Configuration classes (beans only)
- Logging statements
- Constants

### Key Coverage Metrics

1. **Line Coverage**: Percentage of executable lines executed
2. **Branch Coverage**: Percentage of conditional branches tested
3. **Method Coverage**: Percentage of methods with test coverage

---

## Test Execution Matrix

| Test Type | Class | Method Count | Coverage Focus |
|-----------|-------|--------------|-----------------|
| Unit | InventoryController | 4 | Controller logic |
| Unit | OrderController | 10 | Order validation |
| Unit | OrderService | 4 | Business logic |
| Unit | KafkaProducer | 5 | Message publishing |
| Unit | Config | 3 | Bean creation |
| Integration | E2E Tests | 7 | Complete workflows |
| Resilience | Circuit Breaker | 8 | State transitions |
| **TOTAL** | - | **41** | - |

---

## Best Practices Implemented

### 1. Test Organization

✅ Tests organized by layer (controller, service, config)
✅ Clear test method naming: `test{Method}_{Scenario}()`
✅ Separate files for different concerns

### 2. Mocking Strategy

✅ Unit tests use Mockito for isolation
✅ Integration tests use MockWebServer for HTTP
✅ Spring context used only when necessary

### 3. Test Data

✅ Fixtures in @BeforeEach methods
✅ Clear test data setup with descriptive values
✅ Realistic product/order data

### 4. Assertions

✅ Multiple assertions per test (logical grouping)
✅ Clear assertion messages
✅ Use of custom matchers for complex assertions

### 5. Circuit Breaker Testing

✅ State transition verification
✅ Fallback mechanism validation
✅ Retry behavior confirmation

### 6. Documentation

✅ Comprehensive JavaDoc comments
✅ @DisplayName annotations for clarity
✅ Detailed test descriptions

---

## Common Test Patterns

### Pattern 1: Arrange-Act-Assert (AAA)

```java
@Test
void testExample() {
    // Arrange - Setup test data and mocks
    when(service.getData()).thenReturn(expectedData);
    
    // Act - Execute the code under test
    Result result = controller.getAction();
    
    // Assert - Verify the results
    assertEquals(expectedData, result);
}
```

### Pattern 2: BDD-Style Given-When-Then

```java
@Test
void testExample() {
    // Given
    given(service.getData()).willReturn(expectedData);
    
    // When
    Result result = controller.getAction();
    
    // Then
    then(result).should().equal(expectedData);
}
```

### Pattern 3: MockWebServer for HTTP Testing

```java
@BeforeEach
void setup() {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
}

@Test
void testHttpCall() {
    // Arrange
    mockWebServer.enqueue(new MockResponse().setBody("{}"));
    
    // Act & Assert
    // Make API call
}
```

---

## Troubleshooting

### Issue: Tests fail with ClassFormatException (JaCoCo)

**Solution**: Update JaCoCo to 0.8.12+ for Java 21 compatibility

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
</plugin>
```

### Issue: Circuit Breaker tests don't reflect state changes

**Solution**: Ensure test configuration has lower thresholds:
```properties
resilience4j.circuitbreaker.instances.inventoryService.minimum-number-of-calls=3
resilience4j.circuitbreaker.instances.inventoryService.failure-rate-threshold=50
```

### Issue: MockWebServer port conflicts

**Solution**: Each test uses a unique port via `MockWebServer`
```java
mockWebServer.start(); // Auto-assigns random port
String baseUrl = mockWebServer.url("/").toString();
```

---

## Next Steps

1. **Increase Coverage**: Add tests for edge cases
2. **Performance Testing**: Add load testing for concurrent orders
3. **Security Testing**: Add authentication/authorization tests
4. **Contract Testing**: Add Pact tests for service dependencies
5. **Mutation Testing**: Add PIT for mutation testing

---

## Appendix

### A. Required Dependencies

```xml
<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mocking -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- HTTP Mocking -->
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <version>4.12.0</version>
    <scope>test</scope>
</dependency>

<!-- Resilience4j Testing -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>2.3.0</version>
    <scope>test</scope>
</dependency>

<!-- Coverage -->
<dependency>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
</dependency>
```

### B. Maven Commands Reference

```bash
# Clean and test
mvn clean test

# Test with coverage
mvn clean test jacoco:report

# Skip tests during build
mvn clean install -DskipTests

# Run single test
mvn test -Dtest=TestClassName

# Run with specific profile
mvn test -Ptest

# View coverage report
open target/site/jacoco/index.html
```

### C. Environment Variables

```bash
# JaCoCo output
jacoco.exec -> target/jacoco.exec

# Test resources
application-test.properties -> src/test/resources/

# Coverage minimum
jacoco.minimum-coverage=0.85
```

---

**Last Updated**: February 26, 2026
**Document Version**: 1.0
**Status**: Complete

