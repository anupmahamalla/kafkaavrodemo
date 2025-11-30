# Fix for ListenerExecutionFailedException

## The Error You Encountered

```
org.springframework.kafka.listener.ListenerExecutionFailedException: 
Listener method could not be invoked with the incoming message
Endpoint handler details:
Method [public void com.ssnc.kafkaavrodemo.consumer.OrderConsumer.consumeOrder(...)]
```

## Root Cause

This error occurs when the Kafka consumer cannot properly deserialize the Avro message or when there's a mismatch between the expected method parameters and what Kafka is providing.

## What Was Fixed

### 1. **Simplified Consumer Method Signature**

**Before:**
```java
public void consumeOrder(
        @Payload OrderDetails orderDetails,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset) {
    // ...
}
```

**After:**
```java
public void consumeOrder(@Payload OrderDetails orderDetails) {
    // ...
}
```

**Why:** Sometimes Spring Kafka has issues injecting header values, especially with Avro deserialization. Simplifying to just the payload ensures the deserialization focuses on the main object.

### 2. **Verified KafkaAvroDeserializer Configuration**

The `KafkaConfig.java` properly configures:
```java
configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
configProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
configProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
```

The `SPECIFIC_AVRO_READER_CONFIG: true` is **critical** - it tells the deserializer to create `OrderDetails` objects instead of generic Avro records.

### 3. **Added Error Handling**

```java
public void consumeOrder(@Payload OrderDetails orderDetails) {
    try {
        // Process order
    } catch (Exception e) {
        log.error("Error processing order: ", e);
        throw e;
    }
}
```

## How to Test the Fix

### Step 1: Ensure Kafka is Running
```powershell
docker-compose ps
# Should show kafka, zookeeper, schema-registry, kafka-ui as "Up"
```

### Step 2: Start the Application
```powershell
mvn spring-boot:run
```

Watch for successful startup:
```
Kafka Avro Demo Application Started Successfully
```

### Step 3: Send a Test Order
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-001","amount":99.99,"status":"PENDING"}'
```

### Step 4: Verify Logs
You should see:
```
INFO  c.s.k.producer.OrderProducer : Sending order to Kafka topic: order-details-topic...
INFO  c.s.k.producer.OrderProducer : Successfully sent order [<uuid>] with offset [0]
INFO  c.s.k.consumer.OrderConsumer : Received order from Kafka
INFO  c.s.k.consumer.OrderConsumer : Order Details - ID: <uuid>, Customer: CUST-001, Amount: 99.99...
INFO  c.s.k.consumer.OrderConsumer : Processing order with ID: <uuid>
```

## Common Causes of This Error

### 1. **Schema Registry Not Running**
```powershell
# Check schema registry
curl http://localhost:8081/subjects

# If it fails, restart Docker
docker-compose down
docker-compose up -d
```

### 2. **Wrong Deserializer Configuration**
Ensure `application.yaml` has:
```yaml
consumer:
  value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
  properties:
    schema.registry.url: http://localhost:8081
    specific.avro.reader: true  # ← This is critical!
```

### 3. **Schema Mismatch**
If you changed the Avro schema:
```powershell
# Regenerate classes
mvn clean generate-sources

# Clear Kafka topics and restart
docker-compose down -v
docker-compose up -d
```

### 4. **ClassCastException (GenericRecord vs OrderDetails)**
If `specific.avro.reader: true` is missing, Kafka will try to pass a `GenericRecord` instead of `OrderDetails`, causing the error.

**Fix:** Ensure `KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG` is set to `true` in `KafkaConfig.java`

### 5. **Incompatible Message in Topic**
If there are old messages with different schemas:
```powershell
# Delete and recreate topic
docker exec kafka kafka-topics --delete --topic order-details-topic --bootstrap-server localhost:9092
docker exec kafka kafka-topics --create --topic order-details-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

## Verification Checklist

Before testing, ensure:

- ✅ Kafka is running: `docker-compose ps`
- ✅ Schema Registry is accessible: `curl http://localhost:8081`
- ✅ Avro classes generated: Check `target/generated-sources/avro/com/ssnc/avroModels/OrderDetails.java`
- ✅ Application starts without errors: `mvn spring-boot:run`
- ✅ Consumer factory has `SPECIFIC_AVRO_READER_CONFIG: true`

## Alternative: If Headers Are Needed

If you need topic, partition, offset info, use `ConsumerRecord`:

```java
@KafkaListener(topics = "${spring.kafka.topic.orders}", 
               groupId = "${spring.kafka.consumer.group-id}")
public void consumeOrder(ConsumerRecord<String, OrderDetails> record) {
    OrderDetails orderDetails = record.value();
    String topic = record.topic();
    int partition = record.partition();
    long offset = record.offset();
    
    log.info("Received from topic: {}, partition: {}, offset: {}", 
             topic, partition, offset);
    log.info("Order: {}", orderDetails);
}
```

## Summary

The fix involved:
1. ✅ Simplifying the consumer method to use only `@Payload OrderDetails`
2. ✅ Verifying Avro deserializer configuration with `specific.avro.reader: true`
3. ✅ Adding error handling for better debugging
4. ✅ Removing unused header parameters that could cause injection issues

The application should now successfully consume Avro messages from Kafka!

