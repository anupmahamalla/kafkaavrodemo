# Diagnostic Script - Check Producer/Consumer Status

Write-Host "=== Kafka Avro Demo - Diagnostic Check ===" -ForegroundColor Cyan
Write-Host ""

# Check 1: Docker Services
Write-Host "1. Checking Docker Services..." -ForegroundColor Yellow
docker-compose ps
Write-Host ""

# Check 2: Spring Boot Application
Write-Host "2. Checking Spring Boot Application on localhost:8080..." -ForegroundColor Yellow
$appRunning = $false
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/orders/test" -Method GET -ErrorAction SilentlyContinue
if ($null -ne $response -and $response.StatusCode -eq 200) {
    Write-Host "   ✓ Application IS running locally" -ForegroundColor Green
    Write-Host "   ★ LOGS ARE IN YOUR TERMINAL (where mvn spring-boot:run is running)" -ForegroundColor Yellow
    $appRunning = $true
} else {
    Write-Host "   ✗ Application is NOT running on localhost:8080" -ForegroundColor Red
    Write-Host "   To start: mvn spring-boot:run" -ForegroundColor Cyan
}
Write-Host ""

# Check 3: Kafka Topics
Write-Host "3. Kafka Topics:" -ForegroundColor Yellow
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
Write-Host ""

if ($appRunning) {
    Write-Host "=== SEND A TEST ORDER ===" -ForegroundColor Cyan
    Write-Host "Run this command to test:" -ForegroundColor Yellow
    Write-Host '.\test-orders.ps1' -ForegroundColor White
    Write-Host ""
    Write-Host "Then CHECK YOUR TERMINAL where mvn spring-boot:run is running!" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== WHERE TO FIND LOGS ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "If running LOCALLY:" -ForegroundColor Yellow
Write-Host "  → Terminal where 'mvn spring-boot:run' is running" -ForegroundColor White
Write-Host ""
Write-Host "If running in DOCKER:" -ForegroundColor Yellow
Write-Host "  → docker logs -f kafkaavrodemo-app" -ForegroundColor White
Write-Host ""

