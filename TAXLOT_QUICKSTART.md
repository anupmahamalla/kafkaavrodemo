# Quick Start: TaxLot Producer & Consumer

## ✅ What Was Created

### Core Components
1. **TaxLotProducer** - Publishes TaxLot events to Kafka
2. **TaxLotConsumer** - Consumes TaxLot events from Kafka  
3. **TaxLotController** - REST API endpoints for TaxLot operations
4. **KafkaConfig Updates** - Bean configurations for TaxLot
5. **application.yaml Updates** - Topic and consumer group config

### Test & Documentation
6. **test-taxlots.ps1** - PowerShell test script
7. **TAXLOT_API_DOCS.md** - Complete API documentation
8. **TAXLOT_IMPLEMENTATION_SUMMARY.md** - Implementation details

---

## 🚀 Quick Start (3 Steps)

### Step 1: Generate Avro Classes
```powershell
.\mvnw.cmd clean compile
```
> **Note:** Requires Java 17. If you get a Java version error, ensure Java 17 is active.

### Step 2: Start Application
```powershell
# Ensure Kafka is running
docker-compose up -d

# Start the application
.\mvnw.cmd spring-boot:run
```

### Step 3: Test It
```powershell
# Run the test script
.\test-taxlots.ps1
```

---

## 📡 API Endpoints

### POST /api/taxlots
Create and publish a tax lot

**Minimal Request:**
```json
{
  "investmentId": "INV-12345",
  "clientShortName": "CLIENT-A",
  "clientId": 1001,
  "fundShortName": "FUND-XYZ",
  "fundId": 5001,
  "genevaServer": "GENEVA-PROD-01",
  "taxLotId": 100001,
  "quantity": 1000.5,
  "tradeNotional": 250000.75,
  "tradePrice": 250.50,
  "resetPrice": 251.00,
  "swapCurrency": "USD",
  "spread": 0.025,
  "marketPrice": 252.25,
  "underlyingInvestmentId": "UNDERLYING-INV-001",
  "underlyingCurrency": "USD",
  "userTranId": "TXN-2024-001"
}
```

### GET /api/taxlots/test
Health check

**Response:** `"Tax lot service is running!"`

---

## 🔍 What Happens When You POST

```
1. REST Request → TaxLotController
   ↓
2. Convert JSON to TaxLotDetail (Avro)
   ↓
3. TaxLotProducer → Kafka Topic (taxlot-details-topic)
   ↓
4. TaxLotConsumer receives and logs
   ↓
5. processTaxLot() - Your business logic here
```

---

## 📋 Configuration

### Topic
- **Name:** `taxlot-details-topic`
- **Key:** `eventId:investmentId` (composite string)
- **Value:** TaxLotDetail (Avro)

### Consumer Group
- **Name:** `taxlot-consumer-group`

### Schema Registry
- **URL:** http://localhost:8081
- **Subject:** `taxlot-details-topic-value`

---

## 🎯 Key Features

✅ Type-safe Avro serialization/deserialization  
✅ Schema Registry integration  
✅ Composite key support (eventId:investmentId)  
✅ Decimal precision (18 digits, 9 decimal places)  
✅ Timestamp handling (epoch milliseconds)  
✅ Comprehensive logging  
✅ Error handling  
✅ REST API with auto-generated eventId  

---

## 📊 Logs to Expect

### Producer
```
INFO - Sending tax lot to Kafka topic: taxlot-details-topic with eventId: EVT-001, investmentId: INV-12345
INFO - Successfully sent tax lot [EventId: EVT-001, InvestmentId: INV-12345] with offset [0]
```

### Consumer
```
INFO - Received tax lot from Kafka - Topic: taxlot-details-topic, Partition: 0, Offset: 0, Key: EVT-001:INV-12345
INFO - Tax Lot Details - Client: CLIENT-A (ID: 1001), Fund: FUND-XYZ (ID: 5001), TaxLotId: 100001
INFO - Processing tax lot with ID: 100001 for client: CLIENT-A
```

---

## 🛠️ Troubleshooting

| Issue | Solution |
|-------|----------|
| Cannot resolve TaxLotDetail | Run `.\mvnw.cmd clean compile` |
| Java version error | Use Java 17: `java -version` |
| Connection refused | Start Kafka: `docker-compose up -d` |
| Schema Registry error | Check http://localhost:8081 is running |

---

## 📚 More Information

- **Full API Docs:** `TAXLOT_API_DOCS.md`
- **Implementation Details:** `TAXLOT_IMPLEMENTATION_SUMMARY.md`
- **Test Script:** `test-taxlots.ps1`

---

## 🎓 Next Steps

1. **Customize Business Logic:** Edit `TaxLotConsumer.processTaxLot()`
2. **Add Validation:** Add validation rules in the controller
3. **Database Integration:** Save tax lots to your database
4. **Add More Endpoints:** Create GET, UPDATE, DELETE endpoints
5. **Error Handling:** Add dead letter queue for failed messages
6. **Monitoring:** Add metrics and monitoring

---

**That's it! You're ready to use TaxLot producer and consumer!** 🎉

