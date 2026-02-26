# Developer Guide

Comprehensive guide for developers contributing to the Order Service.

## Table of Contents

1. [Setting Up Development Environment](#setting-up-development-environment)
2. [Project Structure](#project-structure)
3. [Development Workflow](#development-workflow)
4. [Code Standards](#code-standards)
5. [Adding New Features](#adding-new-features)
6. [Debugging](#debugging)
7. [Performance Tips](#performance-tips)
8. [Git Workflow](#git-workflow)

---

## Setting Up Development Environment

### IDE Setup

#### IntelliJ IDEA (Recommended)

1. **Import Project**
   - File → Open → Select pom.xml
   - Choose "Open as Project"

2. **Configure JDK**
   - Settings → Project Structure → Project
   - Set Language level to 21
   - Set Project SDK to Java 21

3. **Install Plugins**
   - Lombok
   - Spring Boot
   - Kafka

4. **Enable Annotations**
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

#### VS Code

1. **Install Extensions**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Maven for Java

2. **Open Workspace**
   - File → Open Folder → Select project directory

3. **Configure Launch**
   - Create `.vscode/launch.json`
   - Configure for Maven/Java 21

#### Eclipse

1. **Import Project**
   - File → Import → Maven → Existing Maven Projects
   - Select project root

2. **Configure JDK**
   - Project Properties → Java Compiler
   - Set Compiler compliance level to 21

### Install Dependencies

```bash
# Maven
mvn clean install

# Download dependencies
mvn dependency:resolve

# Update dependencies
mvn dependency:update-snapshots
```

### Setup Git Hooks (Optional)

```bash
# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
mvn clean verify
if [ $? -ne 0 ]; then
  echo "Tests failed. Commit aborted."
  exit 1
fi
EOF

chmod +x .git/hooks/pre-commit
```

---

## Project Structure

### Directory Overview

```
order-service/
├── src/main/java/com/hometask/orderservice/
│   ├── OrderServiceApplication.java         # Entry point
│   ├── config/                              # Configuration
│   ├── controller/                          # REST Controllers
│   ├── service/                             # Business Logic
│   ├── dto/                                 # Data Transfer Objects
│   └── exception/                           # Custom Exceptions (future)
│
├── src/main/resources/
│   ├── application.properties               # Configuration
│   ├── application-dev.properties
│   ├── application-prod.properties
│   └── logback-spring.xml                   # Logging config
│
├── src/test/
│   ├── java/com/hometask/orderservice/
│   │   ├── controller/                      # Controller tests
│   │   ├── service/                         # Service tests
│   │   └── config/                          # Test config
│   └── resources/
│       ├── application-test.properties
│       └── test-data.json
│
├── pom.xml                                  # Maven config
├── README.md                                # Overview
├── ARCHITECTURE.md                          # Design
├── API_DOCUMENTATION.md                     # API reference
├── CONFIGURATION.md                         # Configuration guide
├── TESTING.md                               # Testing guide
└── QUICK_START.md                           # Quick setup
```

### Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Class | PascalCase | OrderService |
| Method | camelCase | placeOrder() |
| Constant | UPPER_SNAKE_CASE | MAX_RETRIES |
| Package | lowercase.dot.notation | com.hometask.orderservice |
| Test Class | {Name}Test | OrderServiceTest |
| Config Class | {Name}Config | WebClientConfig |
| DTO | {Name}DTO | ProductDTO |

---

## Development Workflow

### 1. Create Feature Branch

```bash
# Update master
git checkout master
git pull origin master

# Create feature branch
git checkout -b feature/add-order-filtering

# Or bugfix branch
git checkout -b bugfix/fix-timeout-issue
```

**Branch Naming Conventions:**
- Feature: `feature/description`
- Bugfix: `bugfix/description`
- Hotfix: `hotfix/description`

### 2. Develop Feature

```bash
# Make changes
# Edit files
# Test locally

# Run tests
mvn clean test

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### 3. Commit Changes

```bash
# Stage changes
git add .

# Commit with meaningful message
git commit -m "feat: add order filtering by status

- Add filter parameter to /api/orders endpoint
- Implement OrderFilter interface
- Add unit tests for filter logic
- Update API documentation

Fixes #123"
```

**Commit Message Format:**
```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation
- `test:` Tests
- `refactor:` Code refactoring
- `style:` Code style
- `chore:` Build, dependencies

### 4. Push and Create PR

```bash
# Push branch
git push origin feature/add-order-filtering

# Create Pull Request on GitHub/GitLab
# Link to issue
# Add description
# Request review
```

### 5. Code Review Process

1. **CI Checks Pass**
   - All tests pass
   - Code coverage meets requirements
   - No build errors

2. **Code Review**
   - At least 2 approvals
   - No requested changes

3. **Merge**
   - Squash commits option
   - Delete branch after merge

---

## Code Standards

### Java Code Style

#### Imports
```java
// Organized by:
// 1. Java standard library
// 2. Third-party libraries
// 3. Project code

import java.util.List;
import org.springframework.stereotype.Service;
import com.hometask.orderservice.dto.ProductDTO;
```

#### Formatting
```java
// Max line length: 120 characters
// Indent: 4 spaces
// Brace style: Java (opening brace on same line)

public class OrderService {
    public void placeOrder(String id, int qty) {
        // 4 space indent
    }
}
```

#### Naming
```java
// Classes: PascalCase
class OrderService { }

// Methods/Variables: camelCase
void placeOrder() { }
String productId;

// Constants: UPPER_SNAKE_CASE
static final int MAX_RETRIES = 3;
```

#### JavaDoc

```java
/**
 * Fetches all products from the inventory service.
 *
 * @return List of ProductDTO objects
 * @throws RuntimeException if the API call fails after retries
 */
public List<ProductDTO> getAllProducts() {
    // Implementation
}
```

### Spring Best Practices

#### Dependency Injection
```java
// ✅ GOOD - Constructor injection
@Service
public class OrderService {
    private final KafkaProducerService producer;
    
    public OrderService(KafkaProducerService producer) {
        this.producer = producer;
    }
}

// ❌ AVOID - Field injection
@Service
public class OrderService {
    @Autowired
    private KafkaProducerService producer;
}
```

#### Configuration
```java
// ✅ GOOD - Use @Bean with configuration class
@Configuration
public class AppConfig {
    @Bean
    public SomeService someService() {
        return new SomeService();
    }
}

// ❌ AVOID - @Component on everything
@Component
public class RandomClass {
}
```

#### Error Handling
```java
// ✅ GOOD - Specific exceptions
try {
    service.call();
} catch (TimeoutException e) {
    log.error("Request timeout", e);
    return fallback();
}

// ❌ AVOID - Catching Exception
try {
    service.call();
} catch (Exception e) {
    // Swallowing exceptions
}
```

### Code Review Checklist

- [ ] Code follows style guide
- [ ] No hardcoded values (except constants)
- [ ] Meaningful variable names
- [ ] No unnecessary comments
- [ ] Tests pass locally
- [ ] Code coverage meets target
- [ ] No debug statements left
- [ ] Documentation updated
- [ ] No performance regression

---

## Adding New Features

### Step 1: Design the Feature

```bash
# 1. Create issue
# 2. Discuss design in comments
# 3. Plan implementation
# 4. List files to modify
```

### Step 2: Create Test First (TDD)

```java
// src/test/java/.../service/OrderServiceTest.java
@Test
void testFilterOrdersByStatus_Success() {
    // Test implementation
}
```

### Step 3: Implement Feature

```java
// src/main/java/.../service/OrderService.java
public List<Order> filterByStatus(Status status) {
    // Implementation
}
```

### Step 4: Add Controller Endpoint

```java
// src/main/java/.../controller/OrderController.java
@GetMapping("/by-status/{status}")
public ResponseEntity<List<Order>> getOrdersByStatus(
    @PathVariable Status status) {
    return ResponseEntity.ok(orderService.filterByStatus(status));
}
```

### Step 5: Update Documentation

```markdown
# Update API_DOCUMENTATION.md
- Add new endpoint
- Add request/response examples
- Document error scenarios
```

### Step 6: Add Configuration (if needed)

```properties
# application.properties
feature.order-filtering.enabled=true
```

### Step 7: Integration Test

```java
// src/test/java/.../controller/OrderControllerTest.java
@SpringBootTest
class OrderControllerIntegrationTest {
    // Integration test
}
```

---

## Debugging

### Enable Debug Mode

```bash
# Run with debug logging
export LOGGING_LEVEL_COM_HOMETASK=DEBUG
mvn spring-boot:run

# Or programmatically
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

### IntelliJ Debugging

1. **Set Breakpoint**
   - Click left margin (line number area)
   - Red dot appears

2. **Start Debugging**
   - Right-click → Debug
   - Or Shift+F9

3. **Debug Controls**
   - Step Over: F8
   - Step Into: F7
   - Continue: F9
   - Evaluate Expression: Alt+F9

### Viewing Variables

```java
// In debugger:
// 1. Hover over variable
// 2. Right-click → Inspect
// 3. Or Variables tab at bottom
```

### Remote Debugging

```bash
# Start with debug port
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
  -jar target/order-service-0.0.1-SNAPSHOT.jar

# In IDE: Run → Edit Configurations → + → Remote
# Set host: localhost, port: 5005
```

### View Logs

```bash
# Tail application logs
tail -f logs/application.log

# Filter by log level
grep ERROR logs/application.log

# Search with pattern
grep -i "circuit" logs/application.log
```

---

## Performance Tips

### 1. Use Caching

```java
// Cache expensive operations
@Cacheable("products")
public List<Product> getAll() {
    return repository.findAll();
}
```

### 2. Async Processing

```java
// Non-blocking operations
@Async
public void publishEvent(OrderPlaced event) {
    kafkaProducerService.send(event);
}
```

### 3. Connection Pooling

```java
// Configure in WebClientConfig
httpClient.connectionProvider(
    ConnectionProvider.builder("fixed")
        .maxConnections(100)
        .build()
);
```

### 4. Batch Operations

```java
// Send multiple messages efficiently
messages.forEach(msg -> kafkaProducer.send(msg));

// Or better:
kafkaProducer.sendBatch(messages);
```

### 5. Optimize Queries

```java
// If you have JPA:
// ❌ AVOID - N+1 queries
List<Orders> orders = repo.findAll();
orders.forEach(o -> o.getCustomer().getName()); // Extra queries

// ✅ GOOD - Fetch all at once
@Query("SELECT o FROM Order o JOIN FETCH o.customer")
List<Order> findAll();
```

### 6. Monitor Performance

```bash
# Check metrics
curl http://localhost:8081/actuator/metrics

# Monitor specific metric
curl http://localhost:8081/actuator/metrics/http.server.requests
```

---

## Git Workflow

### Common Commands

```bash
# Check status
git status

# View changes
git diff

# View staged changes
git diff --staged

# View commit history
git log --oneline -10

# Create and push branch
git checkout -b feature/name
git push -u origin feature/name

# Rebase on master
git fetch origin
git rebase origin/master

# Interactive rebase (clean history)
git rebase -i HEAD~3

# Squash commits
git rebase -i HEAD~2
# Mark commits as 'squash'

# Force push (be careful!)
git push -f origin feature/name
```

### Handling Conflicts

```bash
# See conflicting files
git status

# Edit conflicting files
# Remove <<<<< and >>>>> markers
# Keep desired changes

# Mark as resolved
git add .

# Complete merge/rebase
git rebase --continue
# or
git merge --continue
```

### Pull Request Workflow

```bash
# 1. Push feature branch
git push origin feature/name

# 2. Create PR on GitHub
# - Title: Descriptive title
# - Description: What, why, how
# - Link to issue: Fixes #123

# 3. Wait for reviews
# - Make requested changes
# - git add . && git commit -m "Address review comments"
# - git push

# 4. Merge when approved
# - Squash commits
# - Delete branch

# 5. Update local master
git checkout master
git pull origin master
```

---

## Testing During Development

### Run Tests on Every Change

```bash
# Watch for changes and run tests
mvn verify -f pom.xml | watch

# Run tests in background
mvn test &

# Run specific test class
mvn test -Dtest=OrderServiceTest

# Run specific test method
mvn test -Dtest=OrderServiceTest#testPlaceOrder_Success
```

### Test Coverage

```bash
# Generate report
mvn clean test jacoco:report

# View coverage
open target/site/jacoco/index.html

# Fail if coverage below threshold
mvn test -Djacoco.haltOnFailure=true -Djacoco.coveredBranchRatio=0.80
```

### Pre-commit Testing

```bash
# Create git hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
mvn clean test
exit $?
EOF

chmod +x .git/hooks/pre-commit
```

---

## Useful Commands

### Clean Build

```bash
# Remove all build artifacts
mvn clean

# Rebuild everything
mvn clean install

# Skip tests during build
mvn clean install -DskipTests
```

### Update Dependencies

```bash
# Check for updates
mvn versions:display-dependency-updates

# Update snapshots
mvn install -U

# Update major versions
mvn versions:use-latest-versions
```

### Generate Code

```bash
# Compile only
mvn compile

# Run code generators
mvn generate-sources

# Generate JavaDoc
mvn javadoc:javadoc
```

---

## IDE Shortcuts (IntelliJ IDEA)

| Action | Shortcut |
|--------|----------|
| Run | Shift+F10 |
| Debug | Shift+F9 |
| Stop | Ctrl+F2 |
| Run Tests | Ctrl+Shift+F10 |
| Debug Tests | Ctrl+Shift+F9 |
| Format Code | Ctrl+Alt+L |
| Optimize Imports | Ctrl+Alt+O |
| Find Usage | Ctrl+F7 |
| Go to Definition | Ctrl+B |
| Quick Fix | Alt+Enter |
| Run Anything | Ctrl+Shift+R |
| Find in Files | Ctrl+Shift+F |
| Replace in Files | Ctrl+Shift+H |

---

**Document Version:** 1.0  
**Last Updated:** February 2026  
**Next Review:** Q2 2026

