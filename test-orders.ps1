# Test script for Kafka Avro Demo Application

Write-Host "=== Kafka Avro Demo Test Script ===" -ForegroundColor Green
Write-Host ""

# Test 1: Health check
Write-Host "1. Testing application health endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/test" -Method GET
    Write-Host "   ✓ Application is running: $response" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Application is not running. Please start it first." -ForegroundColor Red
    Write-Host "   Run: mvn spring-boot:run" -ForegroundColor Cyan
    exit 1
}

Write-Host ""

# Test 2: Create sample orders
Write-Host "2. Creating sample orders..." -ForegroundColor Yellow

$orders = @(
    @{
        customerId = "CUST-001"
        amount = 150.50
        status = "PENDING"
    },
    @{
        customerId = "CUST-002"
        amount = 299.99
        status = "CONFIRMED"
    },
    @{
        customerId = "CUST-003"
        amount = 75.25
        status = "PENDING"
    }
)

foreach ($order in $orders) {
    try {
        $body = $order | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
            -Method POST `
            -ContentType "application/json" `
            -Body $body

        Write-Host "   ✓ $response" -ForegroundColor Green
        Start-Sleep -Milliseconds 500
    } catch {
        Write-Host "   ✗ Failed to create order: $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Test Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  - Check application logs to see consumer processing messages"
Write-Host "  - Visit Kafka UI at http://localhost:8090 to view topics and messages"
Write-Host "  - View Schema Registry schemas at http://localhost:8081"
Write-Host ""

