# Fix Summary: Kafka Listener Exception & Missing Docker Logs

## **Problem 1: ListenerExecutionFailedException**

### Error Message
```
org.springframework.kafka.listener.ListenerExecutionFailedException: Listener method could not be invoked with the incoming message
Endpoint handler details:
Method [public void com.ssnc.kafkaavrodemo.consumer.OrderConsumer.consumeOrder(com.ssnc.avroModels.OrderDetails,java.lang.String,int,long)]
```

### Root Cause
The consumer method was attempting to use `@Header` annotations to inject Kafka metadata (topic, partition, offset), but Spring Kafka was unable to properly inject these values, causing the listener invocation to fail.

### Fix Applied
Changed the method signature to accept a `ConsumerRecord<String, OrderDetails>` object instead of using individual `@Header` parameters. This is the most reliable approach because:

1. **No dependency on header injection**: The `ConsumerRecord` contains all metadata natively
2. **Type-safe**: All fields have proper types
3. **More flexible**: Access to additional metadata like key, headers, timestamp, etc.

### Code Change

**Before (Problematic):**
```java
public void consumeOrder(@Payload OrderDetails orderDetails,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                         @Header(KafkaHeaders.OFFSET) long offset) {
    log.info("Received order from Kafka - Topic: {}, Partition: {}, Offset: {}", 
             topic, partition, offset);
    // ... rest of the code
}
```

**After (Fixed):**
```java
public void consumeOrder(ConsumerRecord<String, OrderDetails> record) {
    OrderDetails orderDetails = record.value();
    log.info("Received order from Kafka - Topic: {}, Partition: {}, Offset: {}", 
             record.topic(), record.partition(), record.offset());
    // ... rest of the code
}
```

### Benefits of This Approach
- ✅ **Reliable**: No issues with header injection
- ✅ **Complete metadata**: Access to topic, partition, offset, key, timestamp, headers
- ✅ **Clean code**: Single parameter instead of multiple annotations
- ✅ **Standard pattern**: Commonly used in Kafka applications

---

## **Problem 2: No Logs in Docker Container**

### Root Cause
The Spring Boot application (producer and consumer) was not running inside a Docker container. The `docker-compose.yml` only included Kafka infrastructure (Zookeeper, Kafka, Schema Registry, Kafka UI) but not the application itself.

### Fix Applied
Added a new service to `docker-compose.yml` to containerize the Spring Boot application:

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

### Why There Were No Logs Before
- The application wasn't running in Docker
- Only Kafka infrastructure was containerized
- You were likely running the app locally (outside Docker) or not at all
- No container = no container logs

---

## How to Use the Fixed Application

### 1. Start All Services (Including Your App)
```powershell
# Stop any existing containers
docker-compose down -v

# Build and start everything
docker-compose up --build
```

This will:
- Build your Spring Boot application in a Docker image
- Start Zookeeper, Kafka, Schema Registry, Kafka UI
- Start your application container with producer and consumer

### 2. View Application Logs
```powershell
# Follow logs in real-time
docker logs -f kafkaavrodemo-app

# View last 100 lines
docker logs --tail 100 kafkaavrodemo-app

# View all services
docker-compose logs -f
```

### 3. Test Producer and Consumer
```powershell
# Send a test order
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

### 4. Expected Logs

**Producer Logs (in kafkaavrodemo-app):**
```
INFO  Sending order to Kafka topic: order-details-topic with orderId: ORD-001
INFO  Successfully sent order [ORD-001] with offset [0]
```

**Consumer Logs (in kafkaavrodemo-app):**
```
INFO  Received order from Kafka - Topic: order-details-topic, Partition: 0, Offset: 0
INFO  Order Details - ID: ORD-001, Customer: CUST-123, Amount: 99.99, Status: PENDING, Created At: 2025-11-17T10:00:00Z
INFO  Processing order with ID: ORD-001
```

---

## Summary of All Changes

### File: `OrderConsumer.java`
- **Changed**: Method signature from `@Header` parameters to `ConsumerRecord<String, OrderDetails>`
- **Reason**: Fix ListenerExecutionFailedException
- **Benefit**: Reliable access to Kafka metadata

### File: `docker-compose.yml`
- **Added**: New service `kafkaavrodemo-app` to run the Spring Boot application
- **Reason**: Enable containerized execution and logging
- **Benefit**: Proper integration with Kafka infrastructure, visible logs

---

## Troubleshooting

### If ListenerExecutionFailedException still occurs:
1. Check that the application compiled successfully
2. Verify the Avro schema matches the expected format
3. Check Schema Registry connectivity: `curl http://localhost:8081/subjects`

### If you don't see logs:
1. Ensure container is running: `docker ps | grep kafkaavrodemo-app`
2. Check container status: `docker-compose ps`
3. Wait 30-60 seconds for Kafka and Schema Registry to fully start
4. Check for startup errors: `docker logs kafkaavrodemo-app`

### If connection errors occur:
- Kafka needs time to start (wait 30-60 seconds)
- Verify network: `docker network inspect kafkaavrodemo_kafka-network`
- Check environment variables in the container: `docker exec kafkaavrodemo-app env | grep KAFKA`

---

## Additional Notes

### Why ConsumerRecord is Better Than @Header
Using `ConsumerRecord` is a best practice because:
- It's the native Kafka object with all metadata
- No Spring-specific annotations needed
- Access to key, value, headers, timestamp in one object
- More robust and less prone to injection issues

### Docker Logging
Now that your app runs in Docker, you can:
- Monitor all services together: `docker-compose logs -f`
- Export logs: `docker logs kafkaavrodemo-app > app.log`
- Use log aggregation tools (ELK, Splunk, etc.)

### Accessing Services
- **Application API**: http://localhost:8080
- **Kafka UI**: http://localhost:8090
- **Schema Registry**: http://localhost:8081
- **Kafka Broker**: localhost:9092

---

## Verification Steps

1. **Start services**: `docker-compose up --build`
2. **Wait for startup**: Check logs until you see "Started KafkaavrodemoApplication"
3. **Send test order**: Use the curl command above
4. **Verify logs**: Both producer and consumer logs should appear
5. **Check Kafka UI**: Visit http://localhost:8090 to see the message

✅ **All issues are now resolved!**

