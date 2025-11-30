# Fixes Applied to Kafka Avro Demo

## Problem 1: ListenerExecutionFailedException - Consumer Method Signature Mismatch

### **Problem Description**
The Kafka consumer was throwing a `ListenerExecutionFailedException` because Spring Kafka was attempting to invoke the consumer method with additional metadata parameters (topic name, partition, and offset) that weren't defined in the method signature.

### **Root Cause**
The original consumer method only accepted the `OrderDetails` payload:
```java
public void consumeOrder(@Payload OrderDetails orderDetails)
```

But Kafka was trying to pass:
```java
consumeOrder(OrderDetails, String topic, int partition, long offset)
```

### **Solution**
Updated the `OrderConsumer.java` to accept Kafka metadata parameters using `@Header` annotations:

```java
public void consumeOrder(@Payload OrderDetails orderDetails,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                         @Header(KafkaHeaders.OFFSET) long offset)
```

### **Benefits**
- ✅ Fixes the listener exception
- ✅ Provides better logging with topic, partition, and offset information
- ✅ Helps with debugging and monitoring message consumption

---

## Problem 2: No Logs in Docker Container

### **Problem Description**
You weren't seeing any logs from the producer and consumer because the Spring Boot application wasn't running in a Docker container at all.

### **Root Cause**
The `docker-compose.yml` only defined Kafka infrastructure services (Zookeeper, Kafka, Schema Registry, Kafka UI) but didn't include the Spring Boot application itself.

### **Solution**
Added a new service to `docker-compose.yml` to run the Spring Boot application in Docker:

```yaml
kafkaavrodemo-app:
  build:
    context: .
    dockerfile: Dockerfile
  container_name: kafkaavrodemo-app
  depends_on:
    - kafka
    - schema-registry
  ports:
    - "8080:8080"
  environment:
    SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    SPRING_KAFKA_SCHEMA_REGISTRY_URL: http://schema-registry:8081
  networks:
    - kafka-network
  restart: unless-stopped
```

### **Benefits**
- ✅ Application now runs in Docker container
- ✅ Logs are visible using `docker logs kafkaavrodemo-app`
- ✅ Proper networking with Kafka infrastructure
- ✅ Environment variables correctly configured for containerized Kafka

---

## How to Use

### 1. Build and Start All Services
```powershell
docker-compose down -v
docker-compose up --build
```

### 2. View Application Logs
```powershell
# View real-time logs
docker logs -f kafkaavrodemo-app

# View last 100 lines
docker logs --tail 100 kafkaavrodemo-app
```

### 3. Test the Producer and Consumer

Send a test order:
```powershell
curl -X POST http://localhost:8080/api/orders `
  -H "Content-Type: application/json" `
  -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-123",
    "amount": 99.99,
    "status": "PENDING",
    "createdAt": "2025-11-17T10:00:00Z"
  }'
```

### 4. Check All Container Logs
```powershell
# View all services
docker-compose logs

# View specific service
docker logs kafka
docker logs schema-registry
docker logs kafkaavrodemo-app
```

### 5. Access Kafka UI
Open your browser and go to: http://localhost:8090

You can view:
- Topics and messages
- Consumer groups and their offsets
- Schema Registry schemas

---

## What Was Fixed

### File: `OrderConsumer.java`
**Changed:**
- Added imports: `KafkaHeaders`, `@Header`
- Updated method signature to accept topic, partition, and offset
- Enhanced logging to include Kafka metadata

### File: `docker-compose.yml`
**Added:**
- New service `kafkaavrodemo-app` to run the Spring Boot application
- Proper dependencies on Kafka and Schema Registry
- Environment variables for Kafka configuration
- Port mapping for accessing the REST API

---

## Expected Behavior

### When you send an order via REST API:

**Producer Logs:**
```
INFO  Sending order to Kafka topic: order-details-topic with orderId: ORD-001
INFO  Successfully sent order [ORD-001] with offset [0]
```

**Consumer Logs:**
```
INFO  Received order from Kafka - Topic: order-details-topic, Partition: 0, Offset: 0
INFO  Order Details - ID: ORD-001, Customer: CUST-123, Amount: 99.99, Status: PENDING, Created At: 2025-11-17T10:00:00Z
INFO  Processing order with ID: ORD-001
```

---

## Troubleshooting

### If you still don't see logs:
1. Make sure all containers are running: `docker-compose ps`
2. Check if the application started successfully: `docker logs kafkaavrodemo-app`
3. Wait for Kafka and Schema Registry to be fully ready (30-60 seconds)

### If you get connection errors:
- Kafka and Schema Registry need time to start
- The application will retry connections automatically
- Check network connectivity: `docker network inspect kafkaavrodemo_kafka-network`

### If you get schema registry errors:
- Ensure Schema Registry is accessible from the app container
- Check environment variable: `SPRING_KAFKA_SCHEMA_REGISTRY_URL=http://schema-registry:8081`
- Verify in logs that schema registration was successful

---

## Summary

The fixes ensure that:
1. ✅ The consumer method signature matches what Spring Kafka expects
2. ✅ The application runs in a Docker container
3. ✅ Logs are properly visible via `docker logs`
4. ✅ All services are properly networked and configured
5. ✅ You can monitor messages with enhanced metadata logging

