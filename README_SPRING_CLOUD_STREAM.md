# Kafka Avro Demo with Spring Cloud Stream

## 🎉 Successfully Migrated to Spring Cloud Stream!

This project demonstrates event-driven microservices using **Spring Cloud Stream** with **Kafka** and **Avro** serialization. It has been fully migrated from traditional Spring Kafka to Spring Cloud Stream for better abstraction and cloud-native support.

## 📋 Table of Contents
- [What Changed](#what-changed)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Testing](#testing)
- [Documentation](#documentation)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)

## 🔄 What Changed

This project was recently converted from traditional Spring Kafka to **Spring Cloud Stream**:

| Aspect | Before | After |
|--------|--------|-------|
| **Framework** | Spring Kafka | Spring Cloud Stream |
| **Producer** | KafkaTemplate | StreamBridge |
| **Consumer** | @KafkaListener | Function-based (Consumer\<T\>) |
| **Configuration** | Java Config Classes | YAML only |
| **Testability** | Requires Kafka | Pure functions, no Kafka needed |

See [BEFORE_AFTER_COMPARISON.md](BEFORE_AFTER_COMPARISON.md) for detailed comparison.

## 🛠️ Technology Stack

- **Spring Boot** 3.5.7
- **Spring Cloud** 2024.0.0
- **Spring Cloud Stream** (with Kafka Binder)
- **Apache Kafka** (via Docker)
- **Confluent Schema Registry** (via Docker)
- **Apache Avro** 1.12.0
- **Java** 17

## 🏗️ Architecture

### Components
1. **Order Service**: Manages order creation and processing
   - Topic: `order-details-topic`
   - Key: String (Order ID)
   - Value: Avro (OrderDetails)

2. **TaxLot Service**: Manages tax lot transactions
   - Topic: `taxlot-details-topic`
   - Key: Avro (TaxLotDetailKey with EventId + InvestmentId)
   - Value: Avro (TaxLotDetail)
   - Partitions: 3 (for parallel processing)

### Data Flow
```
REST API → Producer → StreamBridge → Spring Cloud Stream Binding → Kafka → 
→ Spring Cloud Stream Binding → Consumer Function → Business Logic
```

### Avro Schemas
Located in `src/main/resources/avro/`:
- `orderDetails.avsc` - Order schema
- `TaxLot.avsc` - Tax lot details schema
- `TaxLotKey.avsc` - Tax lot composite key schema

## 📦 Prerequisites

1. **Docker Desktop** (for Kafka and Schema Registry)
2. **Java 17** or higher
3. **Maven** (or use included Maven wrapper)

## 🚀 Quick Start

### 1. Start Infrastructure
```powershell
# Start Kafka, Zookeeper, and Schema Registry
docker-compose up -d

# Verify services are running
docker-compose ps
```

Expected services:
- Zookeeper: `localhost:2181`
- Kafka: `localhost:9092`
- Schema Registry: `localhost:8081`

### 2. Build the Application
```powershell
# Using Maven wrapper
./mvnw.cmd clean package -DskipTests

# Or with installed Maven
mvn clean package -DskipTests
```

### 3. Run the Application
```powershell
# Using Maven
./mvnw.cmd spring-boot:run

# Or run the JAR
java -jar target/kafkaavrodemo-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### 4. Run Tests
```powershell
# Automated test script
.\test-spring-cloud-stream.ps1
```

## 🧪 Testing

### Manual Testing

#### Test Order Service
```powershell
# Health check
Invoke-RestMethod -Uri http://localhost:8080/api/orders/test

# Send an order
Invoke-RestMethod -Uri http://localhost:8080/api/orders -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{
    "customerId": "CUST-001",
    "amount": 1500.00,
    "status": "CREATED"
  }'
```

#### Test TaxLot Service
```powershell
# Health check
Invoke-RestMethod -Uri http://localhost:8080/api/taxlots/test

# Send a tax lot
Invoke-RestMethod -Uri http://localhost:8080/api/taxlots -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{
    "eventId": "EVT-001",
    "investmentId": "INV-001",
    "clientShortName": "CLIENT-A",
    "clientId": 1001,
    "fundShortName": "FUND-A",
    "fundId": 5001,
    "genevaServer": "GENEVA-PROD",
    "taxLotId": 100001,
    "swapCurrency": "USD",
    "underlyingInvestmentId": "UND-001",
    "underlyingCurrency": "USD",
    "userTranId": "TXN-001"
  }'
```

### Check Logs
Look for these log messages:
- **Producer**: `Sending order via Spring Cloud Stream`
- **Producer**: `Successfully sent order [xxx] via Spring Cloud Stream`
- **Consumer**: `Received order from Spring Cloud Stream`
- **Consumer**: `Processing order with ID: xxx`

### Verify in Kafka
```powershell
# List topics
docker exec -it <kafka-container> kafka-topics --list --bootstrap-server localhost:9092

# Consume order messages
docker exec -it <kafka-container> kafka-console-consumer --topic order-details-topic --from-beginning --bootstrap-server localhost:9092

# Consume tax lot messages
docker exec -it <kafka-container> kafka-console-consumer --topic taxlot-details-topic --from-beginning --bootstrap-server localhost:9092
```

### Check Schema Registry
```powershell
# List all subjects
Invoke-RestMethod -Uri http://localhost:8081/subjects

# Get schema for a subject
Invoke-RestMethod -Uri http://localhost:8081/subjects/order-details-topic-value/versions/latest
```

## 📚 Documentation

Comprehensive documentation is available:

1. **[SPRING_CLOUD_STREAM_MIGRATION.md](SPRING_CLOUD_STREAM_MIGRATION.md)**
   - Complete migration guide
   - Configuration details
   - Troubleshooting

2. **[SPRING_CLOUD_STREAM_CONVERSION_SUMMARY.md](SPRING_CLOUD_STREAM_CONVERSION_SUMMARY.md)**
   - What changed
   - File-by-file changes
   - Testing instructions

3. **[BEFORE_AFTER_COMPARISON.md](BEFORE_AFTER_COMPARISON.md)**
   - Side-by-side code comparison
   - Architecture diagrams
   - ROI analysis

4. **[SPRING_CLOUD_STREAM_QUICK_REFERENCE.md](SPRING_CLOUD_STREAM_QUICK_REFERENCE.md)**
   - Quick reference card
   - Common patterns
   - Code snippets

## ⚙️ Configuration

### Spring Cloud Stream Bindings

Located in `src/main/resources/application.yaml`:

```yaml
spring:
  cloud:
    stream:
      bindings:
        # Order producer
        orderProducer-out-0:
          destination: order-details-topic
          
        # Order consumer
        orderConsumer-in-0:
          destination: order-details-topic
          group: order-consumer-group
          
        # TaxLot producer
        taxLotProducer-out-0:
          destination: taxlot-details-topic
          
        # TaxLot consumer
        taxLotConsumer-in-0:
          destination: taxlot-details-topic
          group: taxlot-consumer-group
```

### Environment Variables

Override defaults using environment variables:
```powershell
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:SPRING_KAFKA_SCHEMA_REGISTRY_URL="http://localhost:8081"
```

## 🔌 API Endpoints

### Order Endpoints
- **GET** `/api/orders/test` - Health check
- **POST** `/api/orders` - Create and send order

### TaxLot Endpoints
- **GET** `/api/taxlots/test` - Health check
- **POST** `/api/taxlots` - Create and send tax lot
- **POST** `/api/taxlots/test-partitions` - Test partition distribution

### Actuator Endpoints (if enabled)
- **GET** `/actuator/health` - Application health
- **GET** `/actuator/bindings` - View Spring Cloud Stream bindings
- **POST** `/actuator/bindings/{name}` - Control binding state

## 🔧 Troubleshooting

### Application won't start
1. Check if port 8080 is available
2. Verify Kafka and Schema Registry are running
3. Check logs for connection errors

### Messages not being consumed
1. Verify function names match binding names
2. Check `spring.cloud.stream.function.definition` property
3. Ensure consumer group IDs are unique

### Serialization errors
1. Verify Schema Registry is accessible
2. Check serializers are correctly configured
3. Ensure Avro schemas are valid

See [SPRING_CLOUD_STREAM_MIGRATION.md](SPRING_CLOUD_STREAM_MIGRATION.md) for detailed troubleshooting.

## 📊 Monitoring

### Application Logs
```powershell
# Enable debug logging
$env:LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_STREAM="DEBUG"
```

### Docker Logs
```powershell
# Kafka logs
docker logs <kafka-container-name>

# Schema Registry logs
docker logs <schema-registry-container-name>
```

## 🧹 Cleanup

### Stop Application
Press `Ctrl+C` in the terminal running the application

### Stop Infrastructure
```powershell
# Stop containers
docker-compose down

# Remove volumes (clean slate)
docker-compose down -v
```

## 🎯 Next Steps

1. ✅ **Delete old configuration classes** (optional cleanup):
   - `OrderKafkaConfig.java`
   - `TaxLotKafkaConfig.java`
   - `KafkaTopicConfig.java`

2. ✅ **Explore advanced features**:
   - Error handling with DLQ
   - Reactive streams with Flux/Mono
   - Kafka Streams integration

3. ✅ **Add monitoring**:
   - Prometheus metrics
   - Grafana dashboards
   - Distributed tracing

## 📖 Resources

- [Spring Cloud Stream Documentation](https://spring.io/projects/spring-cloud-stream)
- [Kafka Binder Reference](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/current/reference/html/)
- [Apache Avro Documentation](https://avro.apache.org/docs/)
- [Confluent Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html)

## 👥 Contributors

This project demonstrates best practices for event-driven microservices with Spring Cloud Stream.

## 📝 License

This is a demo project for educational purposes.

---

**Status**: ✅ Migrated to Spring Cloud Stream (December 2025)  
**Version**: 0.0.1-SNAPSHOT  
**Framework**: Spring Cloud Stream 2024.0.0

