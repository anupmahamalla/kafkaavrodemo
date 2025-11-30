# API Request Examples

## 1. Health Check

### Request
```http
GET http://localhost:8080/api/orders/test
```

### cURL (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders/test" -Method GET
```

### Expected Response
```
Order service is running!
```

---

## 2. Create Order - Example 1 (Pending Order)

### Request
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "CUST-001",
  "amount": 150.50,
  "status": "PENDING"
}
```

### cURL (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-001","amount":150.50,"status":"PENDING"}'
```

### Expected Response
```
Order created successfully with ID: <uuid>
```

---

## 3. Create Order - Example 2 (Confirmed Order)

### Request
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "CUST-002",
  "amount": 299.99,
  "status": "CONFIRMED"
}
```

### cURL (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-002","amount":299.99,"status":"CONFIRMED"}'
```

---

## 4. Create Order - Example 3 (Large Amount)

### Request
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "CUST-003",
  "amount": 1500.00,
  "status": "PROCESSING"
}
```

### cURL (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-003","amount":1500.00,"status":"PROCESSING"}'
```

---

## 5. Create Order - Minimal (Status defaults to "CREATED")

### Request
```http
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "CUST-004",
  "amount": 75.25
}
```

### cURL (PowerShell)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-004","amount":75.25}'
```

---

## Using Postman

1. Create a new request
2. Set method to `POST`
3. URL: `http://localhost:8080/api/orders`
4. Headers: `Content-Type: application/json`
5. Body (raw JSON):
```json
{
  "customerId": "CUST-123",
  "amount": 99.99,
  "status": "PENDING"
}
```

---

## Expected Application Logs

After creating an order, you should see:

**Producer Log:**
```
INFO  c.s.k.producer.OrderProducer : Sending order to Kafka topic: order-details-topic with orderId: abc-123-def-456
INFO  c.s.k.producer.OrderProducer : Successfully sent order [abc-123-def-456] with offset [0]
```

**Consumer Log:**
```
INFO  c.s.k.consumer.OrderConsumer : Received order from topic: order-details-topic, partition: 0, offset: 0
INFO  c.s.k.consumer.OrderConsumer : Order Details - ID: abc-123-def-456, Customer: CUST-001, Amount: 150.5, Status: PENDING, Created At: 2025-11-17T00:24:00.123
INFO  c.s.k.consumer.OrderConsumer : Processing order with ID: abc-123-def-456
```

---

## Verify in Kafka UI

1. Open http://localhost:8090
2. Click on "Topics"
3. Click on "order-details-topic"
4. View messages in the topic
5. You should see the OrderDetails in Avro format

---

## Check Schema Registry

Visit http://localhost:8081/subjects to see registered schemas:

```json
[
  "order-details-topic-value"
]
```

To see the schema details:
http://localhost:8081/subjects/order-details-topic-value/versions/latest

