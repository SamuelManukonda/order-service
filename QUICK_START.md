# Quick Start Guide

Get the Order Service running in 5 minutes!

## Prerequisites

- Java 21 JDK installed
- Maven 3.8+
- Docker (optional, for Kafka/Redis)
- Git

## Option 1: Local Development (Fastest)

### 1. Clone & Build (1 minute)

```bash
git clone <repository-url>
cd order-service
mvn clean install
```

### 2. Start External Services (2 minutes)

```bash
# Terminal 1: Kafka
docker run -d --name kafka \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -p 9092:9092 \
  confluentinc/cp-kafka:7.5.0

# Terminal 2: Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Terminal 3: Inventory Service (assumed running on port 8080)
# See Inventory Service documentation
```

### 3. Run Order Service (1 minute)

```bash
mvn spring-boot:run
```

Service starts on: **http://localhost:8081**

## Option 2: Docker Compose (Recommended)

### 1. Create docker-compose.yml

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  order-service:
    build: .
    ports:
      - "8081:8081"
    environment:
      INVENTORY_SERVICE_BASE_URL: http://inventory-service:8080
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      SPRING_REDIS_HOST: redis
    depends_on:
      - kafka
      - redis
```

### 2. Run with Docker Compose

```bash
docker-compose up -d
```

## Verify Installation

### 1. Check Health

```bash
curl http://localhost:8081/actuator/health
```

**Expected Response:**
```json
{"status": "UP"}
```

### 2. Get Products

```bash
curl http://localhost:8081/api/inventory/products
```

### 3. Place an Order

```bash
curl -X POST http://localhost:8081/api/orders/place/PROD001/5
```

### 4. View Swagger UI

Open in browser: **http://localhost:8081/swagger-ui.html**

## Common Commands

### Run Tests

```bash
# All tests
mvn test

# With coverage report
mvn clean test jacoco:report
# View at: target/site/jacoco/index.html
```

### View Logs

```bash
# Stream logs
mvn spring-boot:run | tail -f

# Find specific errors
grep "ERROR" output.log
```

### Stop Services

```bash
# Docker
docker-compose down

# Maven
Ctrl + C
```

### Check Ports

```bash
# macOS/Linux
lsof -i :8081  # Order Service
lsof -i :9092  # Kafka
lsof -i :6379  # Redis

# Windows
netstat -ano | findstr :8081
```

## Troubleshooting

### Port Already in Use

```bash
# Find process using port 8081
lsof -i :8081

# Kill process
kill -9 <PID>

# Or use different port
export SERVER_PORT=8082
mvn spring-boot:run
```

### Can't Connect to Kafka

```bash
# Check Kafka is running
docker ps | grep kafka

# Check bootstrap servers
echo $SPRING_KAFKA_BOOTSTRAP_SERVERS

# Start Kafka if not running
docker-compose up kafka
```

### Inventory Service Not Found

```bash
# Make sure Inventory Service is running on port 8080
curl http://localhost:8080/actuator/health

# Update configuration if needed
export INVENTORY_SERVICE_BASE_URL=http://correct-address:8080
```

### Tests Failing

```bash
# Clean and rebuild
mvn clean install

# Run tests with debug
mvn test -X

# Run specific test
mvn test -Dtest=OrderServiceTest
```

## Next Steps

1. **Read the documentation**
   - [README.md](README.md) - Full documentation
   - [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - API reference
   - [ARCHITECTURE.md](ARCHITECTURE.md) - System design

2. **Explore the API**
   - Visit http://localhost:8081/swagger-ui.html
   - Try sample endpoints

3. **Write tests**
   - See [TESTING.md](TESTING.md)
   - Add tests for new features

4. **Deploy to production**
   - See [CONFIGURATION.md](CONFIGURATION.md) for production setup
   - Use [ARCHITECTURE.md](ARCHITECTURE.md) for deployment patterns

## Useful Links

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **Health Check:** http://localhost:8081/actuator/health
- **Metrics:** http://localhost:8081/actuator/metrics
- **Circuit Breaker Status:** http://localhost:8081/actuator/circuitbreakers

## Getting Help

### View Detailed Logs

```bash
# Enable debug logging
export LOGGING_LEVEL_COM_HOMETASK=DEBUG
mvn spring-boot:run
```

### Check Configuration

```bash
curl http://localhost:8081/actuator/env | jq '.propertySources[] | select(.name | contains("application"))'
```

### Monitor Services

```bash
# Watch Docker containers
docker ps -a

# Check Kafka topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Check Redis
docker exec redis redis-cli ping
```

---

**Need more help?** See [README.md](README.md) and [TROUBLESHOOTING.md](ARCHITECTURE.md#troubleshooting)

