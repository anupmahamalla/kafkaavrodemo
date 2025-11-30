# Kafka Avro Demo Application

A Spring Boot application demonstrating Kafka integration with Avro serialization for order processing.

## Features

- **Avro Schema**: OrderDetails schema with automatic Java class generation
- **Kafka Producer**: REST API to create and publish orders to Kafka
- **Kafka Consumer**: Automatically consumes and processes orders from Kafka topic
- **Schema Registry**: Confluent Schema Registry for Avro schema management
- **Docker Support**: Complete Docker Compose setup for Kafka ecosystem

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose

## Project Structure

```
kafkaavrodemo/
├── src/main/
│   ├── java/com/ssnc/
│   │   ├── kafkaavrodemo/
│   │   │   ├── config/KafkaConfig.java           # Kafka configuration
│   │   │   ├── producer/OrderProducer.java       # Kafka producer service
│   │   │   ├── consumer/OrderConsumer.java       # Kafka consumer service
│   │   │   ├── controller/OrderController.java   # REST API controller
│   │   │   └── KafkaavrodemoApplication.java    # Main application
│   │   └── orders/Order.java
│   └── resources/
│       ├── avro/orderDetails.avsc                # Avro schema definition
│       └── application.yaml                       # Application configuration
├── target/generated-sources/avro/                # Auto-generated Avro classes
├── docker-compose.yml                            # Docker Compose configuration
├── Dockerfile                                     # Application Docker image
└── pom.xml
```

## Getting Started

### 1. Start Kafka Infrastructure with Docker

Start Kafka, Zookeeper, Schema Registry, and Kafka UI:

```powershell
docker-compose up -d
```

Wait for all services to be healthy (approximately 30-60 seconds). Verify services are running:

```powershell
docker-compose ps
```

### 2. Generate Avro Classes

Generate Java classes from Avro schema:

```powershell
mvn clean generate-sources
```

This will create `OrderDetails.java` in `target/generated-sources/avro/com/ssnc/avroModels/`

### 3. Build the Application

```powershell
mvn clean package
```

### 4. Run the Application

```powershell
mvn spring-boot:run
```

Or run the JAR file:

```powershell
java -jar target/kafkaavrodemo-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Health Check
```
GET http://localhost:8080/api/orders/test
```

### Create Order (Publish to Kafka)
```
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": "CUST-12345",
  "amount": 299.99,
  "status": "PENDING"
}
```

Response:
```json
Order created successfully with ID: <generated-uuid>
```

## Docker Services

| Service | Port | Description |
|---------|------|-------------|
| Kafka | 9092 | Kafka broker |
| Zookeeper | 2181 | Zookeeper for Kafka |
| Schema Registry | 8081 | Confluent Schema Registry |
| Kafka UI | 8090 | Web UI for Kafka management |

### Access Kafka UI

Open your browser and navigate to:
```
http://localhost:8090
```

You can view:
- Topics and messages
- Consumer groups
- Schema Registry schemas
- Broker information

## Testing the Application

### 1. Using cURL (PowerShell)

```powershell
# Create an order
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"customerId":"CUST-001","amount":150.50,"status":"PENDING"}'
```

### 2. Using cURL (Command Prompt)

```cmd
curl -X POST http://localhost:8080/api/orders ^
  -H "Content-Type: application/json" ^
  -d "{\"customerId\":\"CUST-001\",\"amount\":150.50,\"status\":\"PENDING\"}"
```

### 3. View Logs

Watch the application logs to see:
- Producer sending messages
- Consumer receiving and processing messages

```powershell
# If running with Maven
# Check the console output

# If running with Docker
docker logs -f <container-name>
```

## Avro Schema

The `OrderDetails` schema is defined in `src/main/resources/avro/orderDetails.avsc`:

```json
{
  "type": "record",
  "name": "OrderDetails",
  "namespace": "com.ssnc.avroModels",
  "fields": [
    { "name": "orderId", "type": "string" },
    { "name": "customerId", "type": "string" },
    { "name": "amount", "type": "double" },
    { "name": "status", "type": "string" },
    { "name": "createdAt", "type": "string" }
  ]
}
```

## Configuration

Key configurations in `application.yaml`:

- **Kafka Bootstrap Servers**: localhost:9092
- **Schema Registry URL**: http://localhost:8081
- **Topic Name**: order-details-topic
- **Consumer Group**: order-consumer-group

## Stop and Clean Up

### Stop all Docker services:
```powershell
docker-compose down
```

### Remove volumes (clean state):
```powershell
docker-compose down -v
```

## Troubleshooting

### Issue: Cannot connect to Kafka
- Ensure Docker containers are running: `docker-compose ps`
- Check Kafka logs: `docker logs kafka`
- Verify port 9092 is not in use by another process

### Issue: Schema Registry errors
- Check Schema Registry is running: `docker logs schema-registry`
- Verify Schema Registry URL in application.yaml matches Docker service

### Issue: Avro classes not found
- Run `mvn generate-sources` to generate Avro classes
- Check `target/generated-sources/avro` directory exists
- Ensure your IDE recognizes `target/generated-sources/avro` as a source folder

## Next Steps

- Add database integration for order persistence
- Implement error handling and dead letter queues
- Add unit and integration tests
- Implement order status updates
- Add monitoring with Prometheus and Grafana

## License

This is a demo project for learning purposes.

