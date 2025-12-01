# TaxLot Topic Partitioning Configuration

## Overview

The TaxLot topic has been configured with **3 partitions** to enable parallel processing of messages with different keys.

---

## Configuration

### application.yaml

```yaml
spring:
  kafka:
    topic:
      taxlots: taxlot-details-topic
      taxlots-partitions: 3
      taxlots-replication-factor: 1
```

### KafkaTopicConfig.java

Automatically creates the `taxlot-details-topic` with:
- **3 partitions** for parallel processing
- **1 replica** (sufficient for local/dev environments)
- **1 hour retention** (messages deleted after 1 hour)
- **10 minute segments** for efficient log management

---

## How Partitioning Works

### Key-Based Partitioning

Kafka uses the **message key** to determine partition assignment:

```
Partition = hash(TaxLotDetailKey) % number_of_partitions
```

**TaxLotDetailKey** contains:
- `EventId` (e.g., "EVT-001")
- `InvestmentId` (e.g., "INV-12345")

### Partition Assignment Rules

1. **Same Key → Same Partition**
   - Messages with identical `EventId + InvestmentId` → Always go to the same partition
   - **Maintains ordering** for messages with the same key

2. **Different Keys → Different Partitions**
   - Messages with different `EventId + InvestmentId` combinations → Distributed across partitions
   - **Enables parallel processing**

### Example Distribution

```
Message 1: EventId="EVT-001", InvestmentId="INV-A" → Partition 0
Message 2: EventId="EVT-002", InvestmentId="INV-B" → Partition 1
Message 3: EventId="EVT-003", InvestmentId="INV-C" → Partition 2
Message 4: EventId="EVT-004", InvestmentId="INV-D" → Partition 0
Message 5: EventId="EVT-005", InvestmentId="INV-E" → Partition 1
```

---

## Consumer Configuration

### Concurrent Consumption

**TaxLotKafkaConfig.java** enables concurrent consumption:

```java
factory.setConcurrency(3);  // 3 consumers for 3 partitions
```

This means:
- **3 consumer threads** running in parallel
- Each thread processes **1 partition**
- **Maximum parallelism** for your setup

### Consumer Logs

Logs now show which partition each message came from:

```
📨 Received tax lot from Kafka - Topic: taxlot-details-topic, PARTITION: 0, Offset: 0
📨 Received tax lot from Kafka - Topic: taxlot-details-topic, PARTITION: 1, Offset: 0
📨 Received tax lot from Kafka - Topic: taxlot-details-topic, PARTITION: 2, Offset: 0
```

---

## Testing Partition Distribution

### Test Endpoint

A new endpoint has been added to test partition distribution:

**Endpoint:** `POST /api/taxlots/test-partitions`

**What it does:**
- Sends **5 messages** with different keys
- Each message has unique `EventId + InvestmentId`
- Shows partition distribution in consumer logs

**Example Request:**

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/taxlots/test-partitions" -Method Post
```

**Response:**

```json
{
  "status": "success",
  "messagesSent": 5,
  "partitions": 3,
  "message": "Check consumer logs to see partition distribution (PARTITION: 0, 1, or 2)",
  "sentMessages": [
    {"eventId": "EVT-001", "investmentId": "INV-A", "client": "CLIENT-A"},
    {"eventId": "EVT-002", "investmentId": "INV-B", "client": "CLIENT-B"},
    {"eventId": "EVT-003", "investmentId": "INV-C", "client": "CLIENT-C"},
    {"eventId": "EVT-004", "investmentId": "INV-D", "client": "CLIENT-D"},
    {"eventId": "EVT-005", "investmentId": "INV-E", "client": "CLIENT-E"}
  ]
}
```

---

## Benefits of 3 Partitions

### ✅ Parallel Processing
- 3 consumer threads process messages simultaneously
- **3x throughput** compared to single partition

### ✅ Scalability
- Can handle higher message volumes
- Easy to add more partitions later

### ✅ Ordering Guarantees
- Messages with the same key maintain order
- Different keys can be processed in parallel

### ✅ Fault Tolerance
- If one consumer fails, others continue processing
- No single point of failure

---

## Partition Distribution Strategy

### Hash-Based Distribution (Default)

Kafka uses **murmur2 hash** of the key:

```
partition = murmur2(serialize(key)) % 3
```

This provides:
- **Even distribution** across partitions
- **Deterministic** assignment (same key → same partition)
- **High performance** hashing

---

## Monitoring Partitions

### In Kafka UI (http://localhost:8080)

1. Navigate to **Topics**
2. Select `taxlot-details-topic`
3. View:
   - Number of partitions: **3**
   - Messages per partition
   - Consumer lag per partition

### In Consumer Logs

Watch for partition numbers in logs:

```
PARTITION: 0 - Processing messages for keys ABC123:INV-001
PARTITION: 1 - Processing messages for keys DEF456:INV-002
PARTITION: 2 - Processing messages for keys GHI789:INV-003
```

---

## Performance Considerations

### Current Setup (3 Partitions)

| Metric | Value |
|--------|-------|
| Partitions | 3 |
| Consumer Threads | 3 |
| Max Parallelism | 3 messages simultaneously |
| Throughput | ~3x single partition |

### Scaling Up

To increase throughput:

1. **Increase partitions** in `application.yaml`:
   ```yaml
   taxlots-partitions: 6
   ```

2. **Increase consumer concurrency**:
   ```java
   factory.setConcurrency(6);
   ```

3. **Recreate topic** (if already exists):
   ```bash
   # Delete old topic
   docker exec kafka kafka-topics --delete --topic taxlot-details-topic --bootstrap-server localhost:9092
   
   # Restart application to create new topic with more partitions
   ```

---

## Partition Assignment Example

### Scenario: 10 Messages, 3 Partitions

```
Message  | EventId  | InvestmentId | Key Hash | Partition
---------|----------|--------------|----------|----------
1        | EVT-001  | INV-A        | 12345    | 0
2        | EVT-002  | INV-B        | 67890    | 1
3        | EVT-003  | INV-C        | 24680    | 2
4        | EVT-004  | INV-D        | 13579    | 0
5        | EVT-005  | INV-E        | 98765    | 1
6        | EVT-001  | INV-A        | 12345    | 0  ← Same key, same partition
7        | EVT-006  | INV-F        | 11111    | 2
8        | EVT-002  | INV-B        | 67890    | 1  ← Same key, same partition
9        | EVT-007  | INV-G        | 22222    | 0
10       | EVT-008  | INV-H        | 33333    | 1
```

**Result:**
- Partition 0: 4 messages
- Partition 1: 4 messages
- Partition 2: 2 messages

---

## Best Practices

### ✅ Do's

1. **Use meaningful keys** - EventId + InvestmentId provides good distribution
2. **Match consumers to partitions** - 3 consumers for 3 partitions
3. **Monitor partition lag** - Ensure even processing across partitions
4. **Plan for growth** - Consider future volume increases

### ❌ Don'ts

1. **Don't use null keys** - Results in round-robin distribution (no ordering)
2. **Don't over-partition** - Too many partitions adds overhead
3. **Don't under-partition** - Too few limits parallelism
4. **Don't change partition count often** - Causes rebalancing

---

## Troubleshooting

### All Messages Going to One Partition

**Cause:** All messages have the same key

**Solution:** Ensure EventId and InvestmentId vary

### Uneven Partition Distribution

**Cause:** Key distribution is skewed

**Solution:** Review key generation logic

### Consumer Lag on Specific Partition

**Cause:** Heavy processing on specific keys

**Solution:** 
- Optimize processing logic
- Consider increasing partitions
- Add more consumer instances

---

## Files Modified

1. ✅ `application.yaml` - Added partition configuration
2. ✅ `KafkaTopicConfig.java` (NEW) - Auto-creates topic with 3 partitions
3. ✅ `TaxLotKafkaConfig.java` - Enabled concurrent consumption (3 threads)
4. ✅ `TaxLotConsumer.java` - Enhanced logging to show partition info
5. ✅ `TaxLotController.java` - Added test endpoint for partition testing

---

## Summary

✅ **3 partitions configured** for TaxLot topic  
✅ **Concurrent consumption** enabled (3 consumer threads)  
✅ **Key-based partitioning** ensures ordering for same keys  
✅ **Test endpoint** available to demonstrate distribution  
✅ **Enhanced logging** shows partition information  
✅ **Auto-topic creation** with retention policy  

**Your TaxLot messages will now be processed in parallel across 3 partitions!** 🚀

