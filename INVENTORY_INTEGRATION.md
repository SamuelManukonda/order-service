# Inventory Service Integration

## Overview
This feature implements integration with the Inventory Service using Spring WebClient. It provides both synchronous and asynchronous methods to fetch product information from the inventory service.

## Components

### 1. Configuration (`WebClientConfig.java`)
- **Location**: `com.hometask.orderservice.config.WebClientConfig`
- **Purpose**: Configures WebClient bean for HTTP communication with the Inventory Service
- **Features**:
  - Connection timeout configuration
  - Read/Write timeout handlers
  - Request and response logging filters
  - Reactive HTTP client setup using Netty

### 2. Data Transfer Object (`ProductDTO.java`)
- **Location**: `com.hometask.orderservice.dto.ProductDTO`
- **Purpose**: Represents product data from the Inventory Service
- **Fields**:
  - `id` - Product unique identifier
  - `name` - Product name
  - `description` - Product description
  - `price` - Product price
  - `quantity` - Available quantity
  - `category` - Product category

### 3. Service Client (`InventoryServiceClient.java`)
- **Location**: `com.hometask.orderservice.service.InventoryServiceClient`
- **Purpose**: Handles communication with the Inventory Service
- **Methods**:
  - `getAllProducts()` - Synchronous method to fetch all products
  - `getAllProductsAsync()` - Asynchronous method to fetch all products
- **Features**:
  - Automatic retry mechanism (3 attempts with 1-second delay)
  - Comprehensive error handling
  - Detailed logging at each step
  - Graceful degradation on failures

### 4. REST Controller (`InventoryController.java`)
- **Location**: `com.hometask.orderservice.controller.InventoryController`
- **Purpose**: Exposes REST endpoints to access inventory data
- **Endpoints**:
  - `GET /api/inventory/products` - Synchronous endpoint
  - `GET /api/inventory/products/async` - Asynchronous endpoint

## Configuration

### Application Properties
```properties
# Inventory Service Configuration
inventory.service.base-url=http://localhost:8080
inventory.service.timeout=5000
```

### Configurable Parameters
- `inventory.service.base-url`: Base URL of the inventory service (default: http://localhost:8080)
- `inventory.service.timeout`: Connection and read/write timeout in milliseconds (default: 5000)

## Dependencies

The following dependency was added to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## Usage Examples

### 1. Using the Service Client Directly
```java
@Autowired
private InventoryServiceClient inventoryServiceClient;

// Synchronous call
public void fetchProducts() {
    List<ProductDTO> products = inventoryServiceClient.getAllProducts();
    // Process products
}

// Asynchronous call
public void fetchProductsAsync() {
    inventoryServiceClient.getAllProductsAsync()
        .subscribe(products -> {
            // Process products asynchronously
        });
}
```

### 2. Using the REST Endpoints

**Synchronous Request:**
```bash
curl -X GET http://localhost:8081/api/inventory/products
```

**Asynchronous Request:**
```bash
curl -X GET http://localhost:8081/api/inventory/products/async
```

## Logging

The implementation includes comprehensive logging at multiple levels:

### INFO Level Logs
- WebClient initialization
- Service client initialization
- API request initiation
- Successful response with product count
- Retry attempts

### DEBUG Level Logs
- Request headers
- Response headers
- Retrieved product details

### ERROR Level Logs
- Error responses from inventory service
- WebClient exceptions
- Unexpected errors
- Retry exhaustion

### Sample Log Output
```
INFO  c.h.o.config.WebClientConfig - Initializing WebClient for Inventory Service with base URL: http://localhost:8080
INFO  c.h.o.service.InventoryServiceClient - InventoryServiceClient initialized successfully
INFO  c.h.o.service.InventoryServiceClient - Fetching all products from inventory service
INFO  c.h.o.config.WebClientConfig - Outgoing Request: GET http://localhost:8080/api/products/all
INFO  c.h.o.config.WebClientConfig - Incoming Response: Status Code 200 OK
INFO  c.h.o.service.InventoryServiceClient - Successfully fetched 10 products from inventory service
```

## Error Handling

The service implements robust error handling:

1. **HTTP Error Responses**: 4xx and 5xx errors are logged with response body
2. **Connection Timeouts**: Configurable timeout with retry mechanism
3. **Retry Logic**: Automatic retry on failures (3 attempts, 1-second delay)
4. **Graceful Degradation**: Returns empty list on async errors instead of throwing exceptions
5. **Exception Propagation**: Synchronous methods throw RuntimeException with detailed error messages

## Testing

To test the integration:

1. Ensure the Inventory Service is running on `http://localhost:8080`
2. Start the Order Service
3. Call the endpoint: `GET http://localhost:8081/api/inventory/products`
4. Verify the response contains the product list from the inventory service

## Architecture Benefits

1. **Reactive Programming**: Uses WebFlux for non-blocking I/O
2. **Resilience**: Retry mechanism and timeout configuration
3. **Observability**: Comprehensive logging for debugging
4. **Separation of Concerns**: Clear separation between configuration, service, and controller layers
5. **Flexibility**: Both sync and async methods available
6. **Maintainability**: Well-documented and follows Spring Boot best practices

## Future Enhancements

Potential improvements:
- Circuit breaker pattern using Resilience4j
- Caching mechanism for frequently accessed products
- Pagination support for large product lists
- Health check endpoint for inventory service
- Metrics and monitoring using Spring Actuator
- Request/response validation
- Authentication and authorization headers

