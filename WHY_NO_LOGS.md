# Why You Don't See Producer/Consumer Logs in Docker

## THE ANSWER

**Your producer and consumer code is NOT running in Docker!**

The current setup only runs:
- ✅ Kafka (in Docker)
- ✅ Zookeeper (in Docker)
- ✅ Schema Registry (in Docker)
- ✅ Kafka UI (in Docker)
- ❌ **Your Spring Boot Application (NOT in Docker)**

## Where Are The Logs?

### Current Situation:
You need to run your application manually with:
```powershell
mvn spring-boot:run
```

**The logs will appear in that terminal window**, NOT in Docker!

### What You Should See:
When you run `mvn spring-boot:run` and send an order, you'll see:

```
INFO  c.s.k.producer.OrderProducer : Sending order to Kafka topic: order-details-topic with orderId: abc-123
INFO  c.s.k.producer.OrderProducer : Successfully sent order [abc-123] with offset [0]
INFO  c.s.k.consumer.OrderConsumer : Received order from topic: order-details-topic
INFO  c.s.k.consumer.OrderConsumer : Order Details - ID: abc-123, Customer: CUST-001...
INFO  c.s.k.consumer.OrderConsumer : Processing order with ID: abc-123
```

## Solution: Run Everything in Docker

### Step 1: Stop Current Setup
```powershell
# If you have mvn spring-boot:run running, press Ctrl+C

# Stop Docker services
docker-compose down
```

### Step 2: Build and Run with Docker
```powershell
# Use the full docker-compose file
docker-compose -f docker-compose-full.yml build

# Start everything (including your app)
docker-compose -f docker-compose-full.yml up -d
```

### Step 3: View Logs in Docker
```powershell
# Watch application logs
docker logs -f kafkaavrodemo-app

# You should see:
# - Spring Boot startup logs
# - "Kafka Avro Demo Application Started Successfully"
```

### Step 4: Send Test Order
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-001","amount":99.99,"status":"PENDING"}'
```

### Step 5: Watch the Logs
In the terminal running `docker logs -f kafkaavrodemo-app`, you'll see:
```
INFO  c.s.k.producer.OrderProducer : Sending order to Kafka topic...
INFO  c.s.k.producer.OrderProducer : Successfully sent order...
INFO  c.s.k.consumer.OrderConsumer : Received order from topic...
INFO  c.s.k.consumer.OrderConsumer : Processing order...
```

## Quick Start Guide

### Option A: Run Locally (Current Setup)

```powershell
# Terminal 1: Start Kafka
docker-compose up -d

# Terminal 2: Run Application
mvn spring-boot:run
# ★ LOGS APPEAR HERE ★

# Terminal 3: Send Orders
.\test-orders.ps1
```

**Logs Location:** Terminal 2 (where mvn spring-boot:run is running)

### Option B: Run Everything in Docker

```powershell
# Build and start
docker-compose -f docker-compose-full.yml up -d --build

# View logs
docker logs -f kafkaavrodemo-app
# ★ LOGS APPEAR HERE ★

# Send orders
.\test-orders.ps1
```

**Logs Location:** Docker container (use `docker logs`)

## Troubleshooting

### "I don't see any logs!"

**Checklist:**
1. ✅ Is Kafka running? `docker-compose ps` (should show kafka as "Up")
2. ✅ Is your app running?
   - Local: Check terminal with `mvn spring-boot:run`
   - Docker: `docker ps | findstr kafkaavrodemo-app`
3. ✅ Did you send a request? Run `.\test-orders.ps1`
4. ✅ Are you looking in the right place?
   - Local: Terminal where mvn is running
   - Docker: `docker logs kafkaavrodemo-app`

### "Application won't start in Docker"

```powershell
# Rebuild without cache
docker-compose -f docker-compose-full.yml build --no-cache

# Start and watch logs
docker-compose -f docker-compose-full.yml up

# Check for errors in the output
```

### "I see errors connecting to Kafka"

Wait 30 seconds after starting Docker services. Kafka takes time to initialize.

```powershell
# Restart everything
docker-compose -f docker-compose-full.yml down
docker-compose -f docker-compose-full.yml up -d

# Wait 30 seconds
Start-Sleep -Seconds 30

# Then start your app or send requests
```

## Summary

| Setup | Where Producer/Consumer Run | Where to Find Logs |
|-------|----------------------------|-------------------|
| **Current (Local)** | On your machine via Maven | Terminal where `mvn spring-boot:run` runs |
| **Docker (Full)** | Inside Docker container | `docker logs -f kafkaavrodemo-app` |

**You're probably looking for Docker logs, but your app isn't running in Docker!**

Run: `mvn spring-boot:run` and check that terminal for logs.

