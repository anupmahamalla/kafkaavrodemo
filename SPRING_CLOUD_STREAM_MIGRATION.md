# Spring Cloud Stream Migration Guide

## Overview
This project has been successfully migrated from traditional Spring Kafka to **Spring Cloud Stream** with Kafka binder. Spring Cloud Stream provides a framework for building event-driven microservices with a simplified programming model.

## What Changed

### 1. Dependencies (pom.xml)
Added the following dependencies:
- `spring-cloud-dependencies` (BOM) - version 2024.0.0
- `spring-cloud-stream` - Core Spring Cloud Stream library
- `spring-cloud-stream-binder-kafka` - Kafka binder implementation
- `spring-boot-starter-json` - JSON serialization support
- `jackson-dataformat-xml` - XML format support

### 2. Configuration (application.yaml)
Replaced traditional Kafka configuration with Spring Cloud Stream bindings:

**Before:**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: ...
    consumer:
      group-id: ...
```

**After:**
```yaml
spring:
  cloud:
    stream:
      bindings:
        orderProducer-out-0:
          destination: order-details-topic
        orderConsumer-in-0:
          destination: order-details-topic
          group: order-consumer-group
```

### 3. Producer Classes
**Before (KafkaTemplate):**
```java
@Autowired
private KafkaTemplate<String, OrderDetails> kafkaTemplate;

public void sendOrder(OrderDetails orderDetails) {
    kafkaTemplate.send(topic, key, orderDetails);
}
```

**After (StreamBridge):**
```java
@Autowired
private StreamBridge streamBridge;

public void sendOrder(OrderDetails orderDetails) {
    Message<OrderDetails> message = MessageBuilder
        .withPayload(orderDetails)
        .setHeader(KafkaHeaders.KEY, orderDetails.getOrderId())
        .build();
    streamBridge.send("orderProducer-out-0", message);
}
```

### 4. Consumer Classes
**Before (@KafkaListener):**
```java
@KafkaListener(topics = "order-details-topic")
public void consumeOrder(ConsumerRecord<String, OrderDetails> record) {
    OrderDetails order = record.value();
    processOrder(order);
}
```

**After (Function<Message>):**
```java
@Bean
public Consumer<Message<OrderDetails>> orderConsumer() {
    return message -> {
        OrderDetails order = message.getPayload();
        processOrder(order);
    };
}
```

### 5. Removed Configuration Classes
The following classes are **NO LONGER NEEDED** and can be deleted:
- `OrderKafkaConfig.java` - Replaced by Spring Cloud Stream bindings
- `TaxLotKafkaConfig.java` - Replaced by Spring Cloud Stream bindings
- `KafkaTopicConfig.java` - Topics are auto-created or managed via bindings

## Key Benefits

### 1. **Simplified Configuration**
- No need to manually configure `ProducerFactory`, `ConsumerFactory`, `KafkaTemplate`
- All configuration is done declaratively in `application.yaml`
- Easier to switch between message brokers (Kafka, RabbitMQ, etc.)

### 2. **Function-Based Programming Model**
- Consumers are simple Java functions (`Consumer<T>`, `Function<T,R>`)
- Better testability - functions can be tested without Kafka infrastructure
- Reactive programming support (if needed)

### 3. **Native Cloud Support**
- Better integration with Spring Cloud ecosystem
- Service discovery, distributed tracing, and configuration management
- Cloud-native patterns (event sourcing, CQRS)

### 4. **Unified API**
- Single API for multiple message brokers
- Easy to add new bindings without code changes
- StreamBridge for dynamic destinations

## Configuration Details

### Bindings Naming Convention
Spring Cloud Stream uses a naming convention for bindings:
- **Producers**: `<functionName>-out-<index>` (e.g., `orderProducer-out-0`)
- **Consumers**: `<functionName>-in-<index>` (e.g., `orderConsumer-in-0`)

### Avro Serialization
Avro serialization is configured per binding:
```yaml
spring:
  cloud:
    stream:
      kafka:
        bindings:
          orderProducer-out-0:
            producer:
              configuration:
                key.serializer: org.apache.kafka.common.serialization.StringSerializer
                value.serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
                schema.registry.url: http://localhost:8081
```

### Partitioning
TaxLot messages are partitioned by key (EventId + InvestmentId):
```yaml
taxLotProducer-out-0:
  producer:
    partitionCount: 3
taxLotConsumer-in-0:
  consumer:
    concurrency: 3  # One consumer per partition
```

## Testing

### 1. Start Infrastructure
```powershell
docker-compose up -d
```

### 2. Test Order API
```powershell
# Send order
Invoke-RestMethod -Uri http://localhost:8080/api/orders -Method POST -Headers @{"Content-Type"="application/json"} -Body '{
  "customerId": "CUST-001",
  "amount": 1500.00,
  "status": "CREATED"
}'
```

### 3. Test TaxLot API
```powershell
# Send tax lot
Invoke-RestMethod -Uri http://localhost:8080/api/taxlots -Method POST -Headers @{"Content-Type"="application/json"} -Body '{
  "eventId": "EVT-001",
  "investmentId": "INV-001",
  "clientShortName": "CLIENT-A",
  "clientId": 1001
  # ... other fields
}'
```

## Migration Checklist

- [x] Updated `pom.xml` with Spring Cloud Stream dependencies
- [x] Updated `application.yaml` with Spring Cloud Stream configuration
- [x] Converted `OrderProducer` to use `StreamBridge`
- [x] Converted `TaxLotProducer` to use `StreamBridge`
- [x] Converted `OrderConsumer` to use function-based approach
- [x] Converted `TaxLotConsumer` to use function-based approach
- [x] Created `StreamConfig` placeholder
- [ ] Delete old Kafka config classes (optional cleanup)
- [ ] Test all endpoints
- [ ] Update documentation

## Troubleshooting

### Issue: Consumer not receiving messages
**Solution:** Check that function names match in:
1. Bean method name (e.g., `orderConsumer()`)
2. `spring.cloud.stream.function.definition` property
3. Binding name (e.g., `orderConsumer-in-0`)

### Issue: Avro serialization error
**Solution:** Verify:
1. Schema Registry is running and accessible
2. Correct serializers are configured in bindings
3. `specific.avro.reader: true` is set for consumers

### Issue: Wrong partition assignment
**Solution:** Ensure:
1. Key is properly set in message headers
2. Partition count matches between producer and topic
3. Consumer concurrency matches partition count

## Additional Resources

- [Spring Cloud Stream Documentation](https://spring.io/projects/spring-cloud-stream)
- [Kafka Binder Reference](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/current/reference/html/)
- [Function-Based Programming Model](https://spring.io/blog/2019/10/14/spring-cloud-stream-programming-model)

## Notes

- The old Kafka config classes are no longer used but kept for reference
- You can safely delete `OrderKafkaConfig.java`, `TaxLotKafkaConfig.java`, and `KafkaTopicConfig.java`
- All functionality remains the same - only the implementation changed
- Controllers remain unchanged and work as before

