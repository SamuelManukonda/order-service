# Quick Start Guide - Inventory Service Integration

## Prerequisites
- Java 21
- Maven
- Inventory Service running on http://localhost:8080

## How to Use

### 1. Start the Order Service
```bash
./mvnw spring-boot:run
```

The service will initialize the WebClient and connect to the Inventory Service.

### 2. Call the Inventory Service

#### Option A: Using REST Endpoints

**Synchronous Call:**
```bash
curl http://localhost:8081/api/inventory/products
```

**Asynchronous Call:**
```bash
curl http://localhost:8081/api/inventory/products/async
```

#### Option B: Direct Service Integration

Inject the `InventoryServiceClient` in your service/controller:

```java
@Service
public class OrderService {
    
    private final InventoryServiceClient inventoryServiceClient;
    
    @Autowired
    public OrderService(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
    }
    
    public void processOrder() {
        // Synchronous call
        List<ProductDTO> products = inventoryServiceClient.getAllProducts();
        
        // Use products in your business logic
        for (ProductDTO product : products) {
            System.out.println("Product: " + product.getName() + 
                             ", Price: " + product.getPrice());
        }
    }
    
    public void processOrderAsync() {
        // Asynchronous call
        inventoryServiceClient.getAllProductsAsync()
            .subscribe(products -> {
                // Handle products asynchronously
                products.forEach(product -> 
                    System.out.println("Product: " + product.getName()));
            });
    }
}
```

### 3. Monitor Logs

Watch the console for detailed logs:

```
INFO  c.h.o.config.WebClientConfig - Initializing WebClient for Inventory Service
INFO  c.h.o.service.InventoryServiceClient - Fetching all products from inventory service
INFO  c.h.o.service.InventoryServiceClient - Successfully fetched 10 products
```

### 4. Configuration

Modify `application.properties` to change settings:

```properties
# Change inventory service URL
inventory.service.base-url=http://your-inventory-service:8080

# Change timeout (in milliseconds)
inventory.service.timeout=10000
```

## Expected Response Format

The inventory service should return JSON in this format:

```json
[
  {
    "id": 1,
    "name": "Product Name",
    "description": "Product Description",
    "price": 99.99,
    "quantity": 10,
    "category": "Category"
  }
]
```

## Error Scenarios

### Inventory Service Down
- The client will retry 3 times with 1-second delay
- Synchronous method: throws RuntimeException
- Asynchronous method: returns empty list
- All attempts are logged

### Timeout
- Default timeout: 5 seconds
- Configurable via `inventory.service.timeout` property
- Logged as error with retry

### Invalid Response
- Invalid JSON: Logged and exception thrown
- Empty response: Returns empty list

## Testing

Run the unit tests:
```bash
./mvnw test
```

Tests include:
- Successful response handling
- Empty list handling  
- Error scenarios with retry
- Both sync and async methods

## Troubleshooting

### Issue: Connection Refused
**Solution:** Ensure inventory service is running on http://localhost:8080

### Issue: Timeout
**Solution:** Increase timeout in application.properties
```properties
inventory.service.timeout=10000
```

### Issue: No logs appearing
**Solution:** Check logging level in application.properties
```properties
logging.level.com.hometask.orderservice=DEBUG
```

## What's Logged

| Event | Log Level | Message |
|-------|-----------|---------|
| WebClient Init | INFO | "Initializing WebClient for Inventory Service..." |
| Request Start | INFO | "Fetching all products from inventory service" |
| Request Details | INFO | "Outgoing Request: GET http://..." |
| Success | INFO | "Successfully fetched X products..." |
| Retry | WARN | "Retrying API call. Attempt: X" |
| Error | ERROR | "Error response from inventory service: ..." |
| Headers | DEBUG | "Request Header: ...", "Response Header: ..." |

## Best Practices

1. **Use Async for Non-Critical Paths**: Use `getAllProductsAsync()` when you don't need immediate results
2. **Handle Errors**: Always wrap sync calls in try-catch blocks
3. **Monitor Logs**: Watch for retry patterns indicating service issues
4. **Configure Timeouts**: Adjust based on your inventory service response times
5. **Test Error Scenarios**: Use the provided unit tests as examples

## Support

For detailed documentation, see `INVENTORY_INTEGRATION.md`

