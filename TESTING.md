# Testing Documentation

## Overview

This document provides comprehensive information about testing strategies, test structure, and guidelines for the Order Service project.

## Table of Contents

1. [Testing Strategy](#testing-strategy)
2. [Test Types](#test-types)
3. [Test Structure](#test-structure)
4. [Running Tests](#running-tests)
5. [Code Coverage](#code-coverage)
6. [Test Examples](#test-examples)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Testing Strategy

### Testing Pyramid

```
                    ▲
                   /│\
                  / │ \       End-to-End Tests (5%)
                 /  │  \      - Full system integration
                /   │   \     - Minimal number
               /    │    \
              /     │     \
             /      │      \
            /───────┼───────\     Integration Tests (15%)
           /        │        \   - Service integration
          /         │         \  - Database interaction
         /          │          \ - Kafka messaging
        /───────────┼───────────\
       /            │            \  Unit Tests (80%)
      /   ┌─────────┴──────────┐  \ - Individual components
     /    │   Mocking Layer    │   \- Isolated testing
    /     │  (Mockito/Faker)   │    \- Fast execution
   /      └────────────────────┘     \
  /                                   \
```

### Test Distribution Goals

| Test Type | Target % | Current % | Status |
|-----------|----------|-----------|--------|
| Unit Tests | 70-80% | 75% | ✅ Good |
| Integration Tests | 15-20% | 20% | ✅ Good |
| End-to-End Tests | 5-10% | 5% | ⚠️ Minimal |
| **Total Coverage** | **80%+** | **55%** | ❌ Need Improvement |

---

## Test Types

### 1. Unit Tests

**Purpose:** Test individual components in isolation

**Scope:** Single class/method

**Characteristics:**
- No external dependencies
- Uses mocks and stubs
- Runs in milliseconds
- Deterministic results

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private KafkaProducerService kafkaProducerService;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void testPlaceOrder_Success() {
        // Arrange
        String productId = "product123";
        int quantity = 5;
        
        // Act
        String result = orderService.placeOrder(productId, quantity);
        
        // Assert
        assertNotNull(result);
        verify(kafkaProducerService, times(1)).sendOrderPlacedMessage(any());
    }
}
```

### 2. Integration Tests

**Purpose:** Test interaction between components

**Scope:** Multiple classes/services

**Characteristics:**
- Tests real beans/components
- May use test containers
- Slower than unit tests
- More realistic scenarios

**Example:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestConfig.class)
class InventoryCircuitBreakerTest {
    
    @Autowired
    private InventoryController controller;
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @MockBean
    private InventoryServiceClient client;
    
    @Test
    void testCircuitBreakerStateTransition() {
        // Test actual circuit breaker behavior
    }
}
```

### 3. End-to-End Tests

**Purpose:** Test complete user workflows

**Scope:** Full application flow

**Characteristics:**
- Uses real external services
- Long execution time
- High maintenance effort
- Most realistic testing

**Example:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderPlacementE2ETest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testCompleteOrderPlacementFlow() {
        // 1. Get products
        // 2. Place order
        // 3. Verify Kafka message
        // 4. Check inventory update
    }
}
```

---

## Test Structure

### Directory Structure

```
src/test/
├── java/com/hometask/orderservice/
│   ├── controller/
│   │   ├── InventoryControllerTest.java
│   │   ├── InventoryCircuitBreakerTest.java
│   │   └── OrderControllerTest.java
│   │
│   ├── service/
│   │   ├── OrderServiceTest.java
│   │   ├── KafkaProducerServiceTest.java
│   │   ├── InventoryServiceClientTest.java
│   │   └── KafkaConsumerServiceTest.java
│   │
│   └── config/
│       └── TestConfig.java
│
└── resources/
    ├── application-test.properties
    └── mock-data.json
```

### Test Class Naming Convention

- **Unit Tests:** `{ClassName}Test.java`
- **Integration Tests:** `{ClassName}IntegrationTest.java`
- **E2E Tests:** `{FeatureName}E2ETest.java`
- **Fixtures:** `{Feature}Fixtures.java`

### Test Method Naming Convention

```java
void test{MethodUnderTest}_{Scenario}_{ExpectedResult}() {
    // Example: testPlaceOrder_WithInvalidProduct_ThrowsException()
}
```

---

## Running Tests

### Basic Commands

#### Run All Tests
```bash
mvn test
```

#### Run Specific Test Class
```bash
mvn test -Dtest=OrderServiceTest
```

#### Run Specific Test Method
```bash
mvn test -Dtest=OrderServiceTest#testPlaceOrder_Success
```

#### Run Tests in Parallel
```bash
mvn test -T 4
```

#### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

### Advanced Options

#### Skip Tests During Build
```bash
mvn clean install -DskipTests
```

#### Run Only Fast Tests (Unit Tests)
```bash
mvn test -Dgroups=fast
```

#### Run with Debug Output
```bash
mvn test -X
```

#### Generate Test Report
```bash
mvn test-compile surefire:test site:site
```

### IDE Integration

#### IntelliJ IDEA
1. Right-click test class → "Run Tests"
2. Or press `Ctrl+Shift+F10`
3. Use Code Coverage tool: `Ctrl+Shift+F10` with coverage enabled

#### VS Code with Maven
```bash
# Run tests from VS Code terminal
mvn test
```

---

## Code Coverage

### Coverage Metrics

**Current Status:**
```
┌────────────────────────────────────────────────────┐
│               Code Coverage Report                 │
├────────────────┬──────────┬────────┬───────────────┤
│ Module         │ Coverage │ Status │ Target        │
├────────────────┼──────────┼────────┼───────────────┤
│ Controllers    │ 60%      │ ⚠️     │ 80%           │
│ Services       │ 65%      │ ⚠️     │ 85%           │
│ DTOs           │ 40%      │ ❌     │ 70%           │
│ Config         │ 50%      │ ❌     │ 75%           │
├────────────────┼──────────┼────────┼───────────────┤
│ TOTAL          │ 55%      │ ❌     │ 80%+          │
└────────────────┴──────────┴────────┴───────────────┘
```

### Viewing Coverage Report

#### Generate Report
```bash
mvn clean test jacoco:report
```

#### View Report
```bash
# Open in browser
open target/site/jacoco/index.html  # macOS
start target/site/jacoco/index.html # Windows
xdg-open target/site/jacoco/index.html # Linux
```

#### Coverage Breakdown
```
target/site/jacoco/
├── index.html              # Overall summary
├── com/hometask/orderservice/
│   ├── index.html          # Package overview
│   ├── controller/         # Controller coverage
│   ├── service/            # Service coverage
│   ├── config/             # Config coverage
│   └── dto/                # DTO coverage
```

### Improving Coverage

#### 1. Identify Untested Code
- Red lines in coverage report = untested
- Yellow lines = partially tested

#### 2. Write Tests for Gaps
```java
// Example: Test exception handling
@Test
void testPlaceOrder_WithNullProductId_ThrowsException() {
    assertThrows(NullPointerException.class, 
        () -> orderService.placeOrder(null, 5));
}
```

#### 3. Test Edge Cases
```java
// Example: Boundary testing
@Test
void testPlaceOrder_WithZeroQuantity_ThrowsException() {
    assertThrows(IllegalArgumentException.class,
        () -> orderService.placeOrder("prod1", 0));
}

@Test
void testPlaceOrder_WithNegativeQuantity_ThrowsException() {
    assertThrows(IllegalArgumentException.class,
        () -> orderService.placeOrder("prod1", -5));
}
```

#### 4. Test Exception Paths
```java
@Test
void testGetAllProducts_OnNetworkError_ReturnsFallback() {
    when(client.getAllProducts()).thenThrow(
        new RuntimeException("Network error"));
    
    ResponseEntity<?> response = controller.getAllProducts();
    assertEquals(503, response.getStatusCode().value());
}
```

---

## Test Examples

### 1. Service Unit Test

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private KafkaProducerService kafkaProducerService;
    
    @InjectMocks
    private OrderService orderService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, 
            "inventoryTopic", "update-inventory");
    }
    
    @Test
    void testPlaceOrder_Success() {
        // Arrange
        String productId = "PROD001";
        int quantity = 5;
        
        // Act
        String result = orderService.placeOrder(productId, quantity);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Order placed successfully"));
        
        ArgumentCaptor<OrderPlaceRequest> captor = 
            ArgumentCaptor.forClass(OrderPlaceRequest.class);
        verify(kafkaProducerService).sendOrderPlacedMessage(captor.capture());
        
        OrderPlaceRequest captured = captor.getValue();
        assertEquals(productId, captured.productId());
        assertEquals(quantity, captured.quantity());
    }
}
```

### 2. Controller Test with Mocks

```java
@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {
    
    @Mock
    private InventoryServiceClient inventoryServiceClient;
    
    @InjectMocks
    private InventoryController controller;
    
    @Test
    void testGetAllProducts_Success() {
        // Arrange
        List<ProductDTO> mockProducts = Arrays.asList(
            new ProductDTO("1", "Product A", "Desc", 
                BigDecimal.valueOf(99.99), "USD", 
                "Electronics", 10, "url", BigDecimal.valueOf(4.5))
        );
        when(inventoryServiceClient.getAllProducts())
            .thenReturn(mockProducts);
        
        // Act
        ResponseEntity<List<ProductDTO>> response = 
            controller.getAllProducts();
        
        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(inventoryServiceClient).getAllProducts();
    }
    
    @Test
    void testGetAllProducts_ServiceUnavailable() {
        // Arrange
        RuntimeException failure = new RuntimeException("Service down");
        
        // Act
        ResponseEntity<List<ProductDTO>> response = 
            controller.getStaticProductsFallback(failure);
        
        // Assert
        assertEquals(503, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
```

### 3. Integration Test

```java
@SpringBootTest(
    classes = OrderServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Import(TestConfig.class)
class InventoryServiceClientIntegrationTest {
    
    @Autowired
    private InventoryServiceClient client;
    
    private MockWebServer mockWebServer;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        // Reconfigure client to use mock server
        WebClient testWebClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();
        ReflectionTestUtils.setField(client, 
            "inventoryWebClient", testWebClient);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    void testGetAllProducts_WithMockServer() {
        // Arrange
        String jsonResponse = """
            [{"id":"1","name":"Product","price":99.99}]
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json")
            .setResponseCode(200));
        
        // Act
        List<ProductDTO> products = client.getAllProducts();
        
        // Assert
        assertEquals(1, products.size());
        assertEquals("Product", products.get(0).getName());
    }
}
```

---

## Best Practices

### 1. Arrange-Act-Assert (AAA) Pattern

```java
@Test
void testFeature() {
    // ARRANGE - Setup test data
    String input = "test";
    
    // ACT - Execute the method
    String result = service.process(input);
    
    // ASSERT - Verify results
    assertEquals("expected", result);
}
```

### 2. One Assertion Per Concept

```java
// ✅ GOOD
@Test
void testOrderCreation() {
    Order order = service.createOrder(productId, quantity);
    assertNotNull(order);
    assertEquals(productId, order.getProductId());
    assertEquals(quantity, order.getQuantity());
}

// ❌ AVOID
@Test
void testOrderCreation() {
    assert(service.createOrder(productId, quantity) != null);
    // Can't isolate which part failed
}
```

### 3. Use Meaningful Test Names

```java
// ✅ GOOD
void testPlaceOrder_WithValidProduct_ReturnsSuccess() {}
void testPlaceOrder_WithInsufficientStock_ReturnsBadRequest() {}
void testGetAllProducts_OnNetworkError_ReturnsFallback() {}

// ❌ AVOID
void test1() {}
void testOrder() {}
void testFail() {}
```

### 4. Test Behavior, Not Implementation

```java
// ✅ GOOD - Test what the method does
@Test
void testPlaceOrder_SendsKafkaMessage() {
    service.placeOrder("product1", 5);
    verify(kafkaService, times(1)).send(any());
}

// ❌ AVOID - Testing internal implementation
@Test
void testPlaceOrder_CallsPrivateMethod() {
    // Don't test private methods
}
```

### 5. Use Test Fixtures for Common Data

```java
class TestFixtures {
    public static ProductDTO createValidProduct() {
        return new ProductDTO("1", "Product", "Desc", 
            BigDecimal.valueOf(99.99), "USD", 
            "Category", 10, "url", BigDecimal.valueOf(4.5));
    }
    
    public static OrderPlaceRequest createValidOrder() {
        return new OrderPlaceRequest("PROD001", 5);
    }
}

// Usage in tests
@Test
void testPlaceOrder() {
    ProductDTO product = TestFixtures.createValidProduct();
    // ... test
}
```

### 6. Mock External Dependencies

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    
    @Mock
    private ExternalService externalService;
    
    @InjectMocks
    private MyService service;
    
    // Never let tests depend on real external services
}
```

### 7. Test Error Scenarios

```java
@Test
void testGetAllProducts_OnServerError_ReturnsErrorResponse() {
    // Arrange
    when(client.getAllProducts())
        .thenThrow(new RuntimeException("Server Error"));
    
    // Act
    ResponseEntity<?> response = controller.getAllProducts();
    
    // Assert
    assertEquals(503, response.getStatusCode().value());
}
```

---

## Troubleshooting

### Common Test Issues

#### 1. Test Fails: "No qualifying bean of type..."

**Problem:**
```
NoSuchBeanDefinitionException: No qualifying bean of type 'SomeService'
```

**Solution:**
```java
// Use @Import or @TestConfiguration
@SpringBootTest
@Import(TestConfig.class)  // ← Add this
class MyTest {
    // ...
}
```

#### 2. Circuit Breaker Not Resetting Between Tests

**Problem:**
```
CircuitBreaker state persists between tests
```

**Solution:**
```java
@BeforeEach
void setUp() {
    CircuitBreaker cb = registry.circuitBreaker("name");
    cb.reset();  // ← Reset state
}
```

#### 3. Mock Not Working with Annotation

**Problem:**
```
Mockito not initializing mocks
```

**Solution:**
```java
// Add MockitoExtension
@ExtendWith(MockitoExtension.class)  // ← Add this
class MyTest {
    @Mock
    private Service service;
}
```

#### 4. Test Timeout Issues

**Problem:**
```
Test takes too long or hangs
```

**Solution:**
```java
@Test
@Timeout(5)  // ← Timeout after 5 seconds
void testSlowOperation() {
    // Test code
}
```

#### 5. MockWebServer Port Already in Use

**Problem:**
```
Address already in use: 0.0.0.0
```

**Solution:**
```java
mockWebServer = new MockWebServer();
mockWebServer.start();  // Uses random available port
// URL includes the port: mockWebServer.url("/")
```

---

## Continuous Integration

### Test Execution in CI/CD

```yaml
# Example: GitHub Actions
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
      - run: mvn clean test
      - run: mvn jacoco:report
      - uses: codecov/codecov-action@v2
```

### Code Coverage Requirements

```
Minimum Coverage Thresholds:
- Line Coverage: 80%
- Branch Coverage: 75%
- Method Coverage: 85%

Enforcement:
- Pre-commit hooks
- CI pipeline checks
- PR review requirements
```

---

## Performance Tips

### Speed Up Test Execution

1. **Use Unit Tests:** Faster than integration tests
2. **Parallelize:** `mvn test -T 4`
3. **Mock External Services:** Don't use real calls
4. **Skip Slow Tests:** Use `@Timeout` to detect slow tests
5. **Cache Dependencies:** Pre-download Maven artifacts

### Optimize Test Database

```java
// Reuse test database
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
```

---

**Document Version:** 1.0  
**Last Updated:** February 2026  
**Next Review:** Q2 2026

