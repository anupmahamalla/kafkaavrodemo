# Quick Start Guide

## Step 1: Start Kafka Infrastructure

```powershell
docker-compose up -d
```

Wait ~30 seconds for all services to start. Check status:
```powershell
docker-compose ps
```

All services should show "Up" status.

## Step 2: Build and Run Application

```powershell
# Generate Avro classes and build
mvn clean package

# Run the application
mvn spring-boot:run
```

Wait for the message: "Kafka Avro Demo Application Started Successfully"

## Step 3: Test the Application

In a new PowerShell window, run the test script:
```powershell
.\test-orders.ps1
```

Or manually create an order:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-123","amount":99.99,"status":"PENDING"}'
```

## Step 4: Verify

1. **Check Application Logs**: You should see:
   - Producer sending messages
   - Consumer receiving and processing messages

2. **View in Kafka UI**: 
   - Open http://localhost:8090
   - Navigate to Topics > order-details-topic
   - View messages

3. **Check Schema Registry**:
   - Open http://localhost:8081/subjects
   - See registered Avro schemas

## Stop Everything

```powershell
# Stop application (Ctrl+C in the window running mvn spring-boot:run)

# Stop Docker services
docker-compose down
```

## Troubleshooting

**Application won't start?**
- Ensure Docker services are running: `docker-compose ps`
- Check ports 8080, 8081, 8090, 9092 are free

**Can't connect to Kafka?**
- Wait 30-60 seconds after `docker-compose up`
- Check Kafka logs: `docker logs kafka`

**Avro classes not found?**
- Run: `mvn clean generate-sources`
- Check: `target/generated-sources/avro/com/ssnc/avroModels/OrderDetails.java` exists

## Example Output

When you create an order, you should see logs like:

```
INFO  c.s.k.producer.OrderProducer    : Sending order to Kafka topic: order-details-topic with orderId: abc-123
INFO  c.s.k.producer.OrderProducer    : Successfully sent order [abc-123] with offset [0]
INFO  c.s.k.consumer.OrderConsumer    : Received order from topic: order-details-topic, partition: 0, offset: 0
INFO  c.s.k.consumer.OrderConsumer    : Order Details - ID: abc-123, Customer: CUST-123, Amount: 99.99, Status: PENDING
INFO  c.s.k.consumer.OrderConsumer    : Processing order with ID: abc-123
```

