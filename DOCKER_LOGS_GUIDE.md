# Docker Deployment Guide - Producer/Consumer Logs

## Understanding the Setup

### Current Situation
You have TWO ways to run this application:

1. **Local Mode** (what you're probably doing now):
   - Kafka runs in Docker (`docker-compose up -d`)
   - Spring Boot app runs locally (`mvn spring-boot:run`)
   - **Logs appear in your terminal**, NOT in Docker

2. **Full Docker Mode** (to see logs in Docker):
   - Everything runs in Docker containers
   - Use `docker-compose-full.yml`
   - View logs with `docker logs`

## Option 1: View Logs from Local Application

If you're running locally with `mvn spring-boot:run`:

### Where to Find Logs:
```powershell
# Logs appear in the terminal where you ran:
mvn spring-boot:run

# You should see output like:
INFO  c.s.k.producer.OrderProducer : Sending order to Kafka topic...
INFO  c.s.k.consumer.OrderConsumer : Received order from topic...
```

### If You Don't See Any Logs:
This means **no messages are being sent**. To fix:

1. **Ensure Kafka is running:**
   ```powershell
   docker-compose up -d
   docker-compose ps
   ```
   You should see: zookeeper, kafka, schema-registry, kafka-ui all "Up"

2. **Start your application:**
   ```powershell
   mvn spring-boot:run
   ```

3. **Send a test order:**
   ```powershell
   .\test-orders.ps1
   ```
   OR
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
     -Method POST `
     -ContentType "application/json" `
     -Body '{"customerId":"CUST-001","amount":99.99,"status":"PENDING"}'
   ```

## Option 2: Run Everything in Docker

To see producer/consumer logs in Docker containers:

### Step 1: Stop Local Application
If running locally, press `Ctrl+C` to stop it.

### Step 2: Stop Current Docker Services
```powershell
docker-compose down
```

### Step 3: Build and Start with Full Docker Compose
```powershell
# Build the application image
docker-compose -f docker-compose-full.yml build

# Start all services including the app
docker-compose -f docker-compose-full.yml up -d
```

### Step 4: View Application Logs
```powershell
# View application logs (producer/consumer)
docker logs -f kafkaavrodemo-app

# Or view all logs together
docker-compose -f docker-compose-full.yml logs -f
```

### Step 5: Send Test Orders
The application runs on the same port (8080):
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-001","amount":99.99,"status":"PENDING"}'
```

### Step 6: Watch the Logs
In the `docker logs -f kafkaavrodemo-app` window, you should see:
```
INFO  c.s.k.producer.OrderProducer : Sending order to Kafka topic: order-details-topic with orderId: <uuid>
INFO  c.s.k.producer.OrderProducer : Successfully sent order [<uuid>] with offset [0]
INFO  c.s.k.consumer.OrderConsumer : Received order from topic: order-details-topic, partition: 0, offset: 0
INFO  c.s.k.consumer.OrderConsumer : Order Details - ID: <uuid>, Customer: CUST-001, Amount: 99.99...
INFO  c.s.k.consumer.OrderConsumer : Processing order with ID: <uuid>
```

## Troubleshooting

### Problem: Kafka Container Not Running

Check if Kafka is running:
```powershell
docker-compose ps
```

If Kafka is missing, restart:
```powershell
docker-compose down
docker-compose up -d
# Wait 30 seconds
docker-compose ps
```

### Problem: No Logs After Sending Request

1. **Check if app is running:**
   ```powershell
   docker ps | Select-String "kafkaavrodemo"
   ```

2. **Check app logs for errors:**
   ```powershell
   docker logs kafkaavrodemo-app
   ```

3. **Check if Kafka is reachable:**
   ```powershell
   docker exec kafkaavrodemo-app ping kafka
   ```

### Problem: Application Won't Start in Docker

Check build logs:
```powershell
docker-compose -f docker-compose-full.yml build --no-cache
docker-compose -f docker-compose-full.yml up
```

### Problem: Schema Registry Connection Error

Ensure Schema Registry is healthy:
```powershell
docker logs schema-registry
curl http://localhost:8081/subjects
```

## Quick Commands Reference

### Local Mode (Current Setup)
```powershell
# Start Kafka infrastructure
docker-compose up -d

# Run app locally
mvn spring-boot:run

# View logs: Check your terminal where mvn is running

# Stop
# Ctrl+C in terminal, then:
docker-compose down
```

### Full Docker Mode
```powershell
# Build and start everything
docker-compose -f docker-compose-full.yml up -d --build

# View app logs
docker logs -f kafkaavrodemo-app

# View all logs
docker-compose -f docker-compose-full.yml logs -f

# Stop everything
docker-compose -f docker-compose-full.yml down
```

## Verifying Logs Are Working

After sending an order, you MUST see these log lines:

✅ **Producer Log:**
```
Sending order to Kafka topic: order-details-topic with orderId: xxx
Successfully sent order [xxx] with offset [0]
```

✅ **Consumer Log:**
```
Received order from topic: order-details-topic, partition: 0, offset: 0
Order Details - ID: xxx, Customer: CUST-001, Amount: 99.99, Status: PENDING
Processing order with ID: xxx
```

If you don't see these, either:
- No request was sent (check API response)
- Application isn't running
- Kafka isn't running
- There's a connection error (check error logs)

