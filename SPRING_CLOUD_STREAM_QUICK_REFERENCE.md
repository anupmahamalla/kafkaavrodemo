# Spring Cloud Stream - Quick Reference

## Binding Names
```
orderProducer-out-0     → Order topic producer
orderConsumer-in-0      → Order topic consumer
taxLotProducer-out-0    → TaxLot topic producer
taxLotConsumer-in-0     → TaxLot topic consumer
```

## Producing Messages

### Using StreamBridge (Recommended)
```java
@Autowired
private StreamBridge streamBridge;

// Simple message
streamBridge.send("orderProducer-out-0", orderDetails);

// With key
Message<OrderDetails> message = MessageBuilder
    .withPayload(orderDetails)
    .setHeader(KafkaHeaders.KEY, "order-123")
    .build();
streamBridge.send("orderProducer-out-0", message);
```

### Using Supplier (Reactive/Polling)
```java
@Bean
public Supplier<OrderDetails> orderSupplier() {
    return () -> generateOrder();
}
```

## Consuming Messages

### Basic Consumer
```java
@Bean
public Consumer<OrderDetails> orderConsumer() {
    return order -> {
        // Process order
        log.info("Received: {}", order);
    };
}
```

### With Message Headers
```java
@Bean
public Consumer<Message<OrderDetails>> orderConsumer() {
    return message -> {
        OrderDetails order = message.getPayload();
        String key = (String) message.getHeaders().get(KafkaHeaders.RECEIVED_KEY);
        Integer partition = (Integer) message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION);
        Long offset = (Long) message.getHeaders().get(KafkaHeaders.OFFSET);
        
        log.info("Key: {}, Partition: {}, Offset: {}", key, partition, offset);
        processOrder(order);
    };
}
```

### Processing and Producing
```java
@Bean
public Function<OrderDetails, TaxLotDetail> orderToTaxLot() {
    return order -> {
        // Transform order to tax lot
        return createTaxLot(order);
    };
}
```

## Configuration Patterns

### Basic Binding
```yaml
spring:
  cloud:
    stream:
      bindings:
        myFunction-in-0:
          destination: input-topic
          group: my-consumer-group
        myFunction-out-0:
          destination: output-topic
```

### With Kafka-Specific Settings
```yaml
spring:
  cloud:
    stream:
      kafka:
        bindings:
          myFunction-in-0:
            consumer:
              configuration:
                max.poll.records: 100
                session.timeout.ms: 30000
```

### Avro Configuration
```yaml
spring:
  cloud:
    stream:
      kafka:
        binder:
          configuration:
            schema.registry.url: http://localhost:8081
        bindings:
          orderProducer-out-0:
            producer:
              configuration:
                key.serializer: org.apache.kafka.common.serialization.StringSerializer
                value.serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
          orderConsumer-in-0:
            consumer:
              configuration:
                key.deserializer: org.apache.kafka.common.serialization.StringDeserializer
                value.deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
                specific.avro.reader: true
```

### Partitioning
```yaml
spring:
  cloud:
    stream:
      bindings:
        producer-out-0:
          producer:
            partitionCount: 3
            partitionKeyExpression: headers['key']
        consumer-in-0:
          consumer:
            concurrency: 3  # Match partition count
```

## Common Headers

```java
// Producer headers
KafkaHeaders.KEY              // Message key
KafkaHeaders.PARTITION_ID     // Target partition
KafkaHeaders.TIMESTAMP        // Message timestamp

// Consumer headers
KafkaHeaders.RECEIVED_KEY           // Message key
KafkaHeaders.RECEIVED_PARTITION     // Partition number
KafkaHeaders.OFFSET                 // Offset
KafkaHeaders.RECEIVED_TIMESTAMP     // Timestamp
KafkaHeaders.RECEIVED_TOPIC         // Topic name
```

## Error Handling

### Retry with DLQ
```yaml
spring:
  cloud:
    stream:
      bindings:
        consumer-in-0:
          consumer:
            maxAttempts: 3
      kafka:
        bindings:
          consumer-in-0:
            consumer:
              enableDlq: true
              dlqName: error-topic
```

### Custom Error Handler
```java
@Bean
public Consumer<Message<?>> errorHandler() {
    return message -> {
        log.error("Error processing: {}", message);
        // Handle error
    };
}
```

## Testing

### Unit Test (No Kafka)
```java
@Test
void testConsumer() {
    OrderConsumer consumer = new OrderConsumer();
    Consumer<Message<OrderDetails>> function = consumer.orderConsumer();
    
    Message<OrderDetails> message = MessageBuilder
        .withPayload(createTestOrder())
        .build();
    
    function.accept(message);
    // Verify processing
}
```

### Integration Test
```java
@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class OrderConsumerTest {
    
    @Autowired
    private InputDestination input;
    
    @Test
    void testOrderConsumption() {
        OrderDetails order = createTestOrder();
        input.send(new GenericMessage<>(order), "order-details-topic");
        // Verify consumption
    }
}
```

## Monitoring

### Actuator Endpoints
```yaml
management:
  endpoints:
    web:
      exposure:
        include: bindings,health,metrics
```

Access: `http://localhost:8080/actuator/bindings`

### Metrics
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

## Useful Commands

### View Bindings
```bash
curl http://localhost:8080/actuator/bindings
```

### Stop Consumer
```bash
curl -X POST http://localhost:8080/actuator/bindings/orderConsumer-in-0 -H "Content-Type: application/json" -d '{"state":"STOPPED"}'
```

### Start Consumer
```bash
curl -X POST http://localhost:8080/actuator/bindings/orderConsumer-in-0 -H "Content-Type: application/json" -d '{"state":"STARTED"}'
```

## Best Practices

1. **Use Message<T> in consumers** to access headers
2. **Set proper group IDs** for consumer groups
3. **Handle errors gracefully** with retry and DLQ
4. **Use specific Avro readers** for better performance
5. **Configure concurrency** to match partitions
6. **Enable actuator** for runtime management
7. **Use StreamBridge** for dynamic destinations
8. **Test functions** without Kafka infrastructure

## Common Issues

### Issue: Consumer not receiving messages
**Check:**
1. Function name in `spring.cloud.stream.function.definition`
2. Bean method name matches function name
3. Binding name format: `<functionName>-in-0`

### Issue: Serialization error
**Check:**
1. Schema Registry URL is correct
2. Correct serializers configured
3. `specific.avro.reader: true` for consumers

### Issue: Wrong partition
**Check:**
1. Key is set in message headers
2. Partition count matches topic
3. Partitioning expression is correct

## Resources

- [Spring Cloud Stream Docs](https://spring.io/projects/spring-cloud-stream)
- [Kafka Binder Reference](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/current/reference/html/)
- [GitHub Samples](https://github.com/spring-cloud/spring-cloud-stream-samples)

