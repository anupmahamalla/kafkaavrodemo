# TaxLot API Documentation

## Overview
This document describes the TaxLot producer and consumer implementation for handling TaxLot detail events in Kafka using Avro schema.

## Components Created

### 1. TaxLotProducer
**Location:** `src/main/java/com/ssnc/kafkaavrodemo/producer/TaxLotProducer.java`

**Purpose:** Produces TaxLot events to Kafka topic

**Key Features:**
- Sends TaxLotDetail messages to the configured Kafka topic
- Uses composite key format: `eventId:investmentId`
- Provides async completion logging for success/failure
- Integrates with Schema Registry for Avro serialization

**Methods:**
- `sendTaxLot(TaxLotDetail taxLotDetail, String eventId, String investmentId)` - Sends a tax lot to Kafka

### 2. TaxLotConsumer
**Location:** `src/main/java/com/ssnc/kafkaavrodemo/consumer/TaxLotConsumer.java`

**Purpose:** Consumes TaxLot events from Kafka topic

**Key Features:**
- Listens to the taxlot-details-topic
- Uses dedicated consumer group: `taxlot-consumer-group`
- Logs comprehensive tax lot details including client, fund, trade, and swap information
- Placeholder for business logic processing

**Methods:**
- `consumeTaxLot(ConsumerRecord<String, TaxLotDetail> record)` - Processes incoming tax lot messages
- `processTaxLot(TaxLotDetail taxLotDetail)` - Business logic placeholder

### 3. TaxLotController
**Location:** `src/main/java/com/ssnc/kafkaavrodemo/controller/TaxLotController.java`

**Purpose:** REST API endpoint for creating tax lots

**Endpoints:**

#### POST /api/taxlots
Creates a new tax lot and publishes it to Kafka

**Request Body:** JSON object with the following fields:
```json
{
  "eventId": "EVT-001",                    // Optional, auto-generated if not provided
  "investmentId": "INV-12345",             // Required
  "clientShortName": "CLIENT-A",           // Required
  "clientId": 1001,                        // Required
  "fundShortName": "FUND-XYZ",             // Required
  "fundId": 5001,                          // Required
  "genevaServer": "GENEVA-PROD-01",        // Required
  "taxLotId": 100001,                      // Required
  "accrualDate": 1701388800000,            // Optional, epoch millis UTC
  "tradeDate": 1701388800000,              // Optional, epoch millis UTC
  "effectiveDate": 1701388800000,          // Optional, epoch millis UTC
  "quantity": 1000.500,                    // Required, decimal with 9 places
  "tradeNotional": 250000.75,              // Required, decimal with 9 places
  "tradePrice": 250.50,                    // Required, decimal with 9 places
  "resetPrice": 251.00,                    // Required, decimal with 9 places
  "swapCurrency": "USD",                   // Required
  "spread": 0.025,                         // Required, decimal with 9 places
  "marketPrice": 252.25,                   // Required, decimal with 9 places
  "underlyingInvestmentId": "UND-001",     // Required
  "underlyingCurrency": "USD",             // Required
  "userTranId": "TXN-2024-001",            // Required
  "knowledgeDate": 1701388800000           // Optional, epoch millis UTC
}
```

**Response:** 
- Success (201): "Tax lot created successfully with EventId: {eventId}"
- Error (500): Error message

#### GET /api/taxlots/test
Health check endpoint

**Response:** "Tax lot service is running!"

## Configuration

### Application YAML
The following configuration has been added to `application.yaml`:

```yaml
spring:
  kafka:
    topic:
      taxlots: taxlot-details-topic
    consumer:
      taxlot-group-id: taxlot-consumer-group
```

### Kafka Configuration
The following beans have been added to `KafkaConfig.java`:

1. **TaxLot Producer Beans:**
   - `taxLotProducerFactory()` - Factory for TaxLot producers
   - `taxLotKafkaTemplate()` - KafkaTemplate for TaxLot messages

2. **TaxLot Consumer Beans:**
   - `taxLotConsumerFactory()` - Factory for TaxLot consumers
   - `taxLotKafkaListenerContainerFactory()` - Container factory for TaxLot listeners

## Avro Schema

### TaxLot.avsc
**Location:** `src/main/resources/avro/TaxLot.avsc`

**Schema Name:** `TaxLotDetail`

**Namespace:** `com.ssnc.avroModels`

**Fields:**
- Client information: ClientShortName, ClientId
- Fund information: FundShortName, FundId
- Server: GenevaServer
- Identifiers: TaxLotId
- Dates: AccrualDate, TradeDate, EffectiveDate, KnowledgeDate, CreatedAt (all as timestamp-millis)
- Trade details: Quantity, TradeNotional, TradePrice, ResetPrice (all as decimal(18,9))
- Swap details: SwapCurrency, Spread, MarketPrice
- Underlying: UnderlyingInvestmentId, UnderlyingCurrency
- Transaction: UserTranId

### TaxLotKey.avsc
**Location:** `src/main/resources/avro/TaxLotKey.avsc`

**Schema Name:** `TaxLotDetailKey`

**Fields:**
- EventId: Event identifier (VARCHAR(32))
- InvestmentId: Investment identifier (VARCHAR(255))

## Testing

### Using PowerShell Script
Run the test script to verify the implementation:

```powershell
.\test-taxlots.ps1
```

This script will:
1. Test the health check endpoint
2. Create two sample tax lots with different data
3. Display responses and any errors

### Manual Testing with cURL

**Health Check:**
```bash
curl http://localhost:8080/api/taxlots/test
```

**Create Tax Lot:**
```bash
curl -X POST http://localhost:8080/api/taxlots \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "EVT-001",
    "investmentId": "INV-12345",
    "clientShortName": "CLIENT-A",
    "clientId": 1001,
    "fundShortName": "FUND-XYZ",
    "fundId": 5001,
    "genevaServer": "GENEVA-PROD-01",
    "taxLotId": 100001,
    "quantity": 1000.500,
    "tradeNotional": 250000.75,
    "tradePrice": 250.50,
    "resetPrice": 251.00,
    "swapCurrency": "USD",
    "spread": 0.025,
    "marketPrice": 252.25,
    "underlyingInvestmentId": "UNDERLYING-INV-001",
    "underlyingCurrency": "USD",
    "userTranId": "TXN-2024-001"
  }'
```

## Building and Running

### 1. Generate Avro Classes
First, compile the project to generate Java classes from Avro schemas:

```powershell
.\mvnw.cmd clean compile
```

This will generate:
- `com.ssnc.avroModels.TaxLotDetail`
- `com.ssnc.avroModels.TaxLotDetailKey`

### 2. Start Kafka Infrastructure
Ensure Kafka and Schema Registry are running:

```powershell
docker-compose up -d
```

### 3. Run the Application
```powershell
.\mvnw.cmd spring-boot:run
```

### 4. Test the Endpoints
```powershell
.\test-taxlots.ps1
```

## Monitoring

### Consumer Logs
The consumer logs will show when tax lots are received and processed:

```
INFO  - Received tax lot from Kafka - Topic: taxlot-details-topic, Partition: 0, Offset: 0, Key: EVT-001:INV-12345
INFO  - Tax Lot Details - Client: CLIENT-A (ID: 1001), Fund: FUND-XYZ (ID: 5001), TaxLotId: 100001, Server: GENEVA-PROD-01
INFO  - Trade Info - TradeDate: 2024-11-30T12:00:00Z, EffectiveDate: 2024-11-30T12:00:00Z, Quantity: [B@..., TradePrice: [B@...
INFO  - Processing tax lot with ID: 100001 for client: CLIENT-A
```

### Producer Logs
The producer logs will show when tax lots are sent:

```
INFO  - Sending tax lot to Kafka topic: taxlot-details-topic with eventId: EVT-001, investmentId: INV-12345
INFO  - Successfully sent tax lot [EventId: EVT-001, InvestmentId: INV-12345] with offset [0]
```

## Schema Registry

The TaxLot schemas will be automatically registered with the Schema Registry when the first message is produced. You can view the schemas at:

- Subject: `taxlot-details-topic-value`
- URL: http://localhost:8081/subjects/taxlot-details-topic-value/versions/latest

## Key Features

1. **Type-Safe Avro Integration:** Uses generated Java classes from Avro schemas
2. **Decimal Precision:** Handles decimal fields with 18-digit precision and 9 decimal places
3. **Timestamp Handling:** Uses epoch milliseconds for all date/time fields
4. **Composite Keys:** Uses eventId:investmentId as the Kafka message key
5. **Schema Evolution:** Supports schema registry for backward/forward compatibility
6. **Error Handling:** Comprehensive error logging and handling
7. **RESTful API:** Easy-to-use REST endpoints for testing and integration

## Business Logic Extension Points

The `TaxLotConsumer.processTaxLot()` method is a placeholder where you can add:

1. **Data Validation:** Validate tax lot data against business rules
2. **Database Persistence:** Save tax lot to database
3. **Accrual Calculations:** Calculate accruals based on the AccrualDate
4. **Position Updates:** Update position tracking systems
5. **Workflow Triggers:** Trigger downstream processing workflows
6. **Notifications:** Send alerts or notifications based on tax lot events
7. **Audit Logging:** Log tax lot events for compliance

## Notes

- All timestamps are in epoch milliseconds (UTC)
- Decimal fields use precision 18 and scale 9
- The eventId is auto-generated if not provided
- CreatedAt timestamp is always set to current time
- The consumer uses specific Avro reader for type safety

