# Diagnostic Script - Check Producer/Consumer Status

Write-Host "=== Kafka Avro Demo - Diagnostic Check ===" -ForegroundColor Cyan
Write-Host ""

# Check 1: Docker Services
Write-Host "1. Checking Docker Services..." -ForegroundColor Yellow
$dockerServices = docker-compose ps --services
$runningServices = docker-compose ps --filter "status=running" --services

Write-Host "   Expected services: zookeeper, kafka, schema-registry, kafka-ui" -ForegroundColor White
Write-Host "   Running services: $runningServices" -ForegroundColor White

if ($runningServices -match "kafka") {
    Write-Host "   ✓ Kafka is running" -ForegroundColor Green
} else {
    Write-Host "   ✗ Kafka is NOT running!" -ForegroundColor Red
    Write-Host "   Run: docker-compose up -d" -ForegroundColor Cyan
    exit 1
}
Write-Host ""

# Check 2: Spring Boot Application
Write-Host "2. Checking Spring Boot Application..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/test" -Method GET -TimeoutSec 2 -ErrorAction Stop
    Write-Host "   ✓ Application is running: $response" -ForegroundColor Green
    Write-Host "   Mode: Running LOCALLY (not in Docker)" -ForegroundColor Cyan
    Write-Host "   Logs Location: Terminal where you ran 'mvn spring-boot:run'" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Application is NOT running on localhost:8080" -ForegroundColor Red
    Write-Host "   To run locally: mvn spring-boot:run" -ForegroundColor Cyan
    Write-Host "   To run in Docker: docker-compose -f docker-compose-full.yml up -d --build" -ForegroundColor Cyan
}
Write-Host ""

# Check 3: Kafka Topics
Write-Host "3. Checking Kafka Topics..." -ForegroundColor Yellow
$topicCheck = docker exec kafka kafka-topics --list --bootstrap-server localhost:9092 2>&1
if ($topicCheck -match "order-details-topic") {
    Write-Host "   ✓ Topic 'order-details-topic' exists" -ForegroundColor Green
} else {
    Write-Host "   ℹ Topic 'order-details-topic' not created yet (will auto-create on first message)" -ForegroundColor Yellow
}
Write-Host ""

# Check 4: Test Connectivity
Write-Host "4. Testing Application Connectivity..." -ForegroundColor Yellow
try {
    $testOrder = @{
        customerId = "DIAG-TEST-001"
        amount = 1.00
        status = "TEST"
    } | ConvertTo-Json

    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
        -Method POST `
        -ContentType "application/json" `
        -Body $testOrder `
        -TimeoutSec 5 `
        -ErrorAction Stop

    Write-Host "   ✓ Successfully sent test order" -ForegroundColor Green
    Write-Host "   Response: $result" -ForegroundColor White
    Write-Host "" -ForegroundColor White
    Write-Host "   ★ CHECK YOUR APPLICATION LOGS NOW ★" -ForegroundColor Yellow
    Write-Host "   You should see producer and consumer logs" -ForegroundColor White
} catch {
    Write-Host "   ✗ Could not send test order" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "=== Summary ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Where to find your logs:" -ForegroundColor Yellow
Write-Host ""
Write-Host "IF RUNNING LOCALLY (mvn spring-boot:run):" -ForegroundColor Cyan
Write-Host "  → Check the terminal where you ran 'mvn spring-boot:run'" -ForegroundColor White
Write-Host "  → Logs appear there in real-time" -ForegroundColor White
Write-Host ""
Write-Host "IF RUNNING IN DOCKER:" -ForegroundColor Cyan
Write-Host "  → Run: docker logs -f kafkaavrodemo-app" -ForegroundColor White
Write-Host "  → Or: docker-compose -f docker-compose-full.yml logs -f app" -ForegroundColor White
Write-Host ""
Write-Host "To view Kafka UI:" -ForegroundColor Yellow
Write-Host "  → http://localhost:8090" -ForegroundColor White
Write-Host ""

