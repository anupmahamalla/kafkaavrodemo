# Kafka Avro Demo - Start Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Kafka Avro Demo - Startup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Start Docker services
Write-Host "Step 1: Starting Kafka infrastructure with Docker..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start Docker services. Please ensure Docker is running." -ForegroundColor Red
    exit 1
}

Write-Host "✓ Docker services started" -ForegroundColor Green
Write-Host ""

# Step 2: Wait for services to be ready
Write-Host "Step 2: Waiting for services to be ready (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Write-Host "✓ Services should be ready" -ForegroundColor Green
Write-Host ""

# Step 3: Check Docker services
Write-Host "Step 3: Checking Docker services status..." -ForegroundColor Yellow
docker-compose ps
Write-Host ""

# Step 4: Build application
Write-Host "Step 4: Building application..." -ForegroundColor Yellow
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build application" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Application built successfully" -ForegroundColor Green
Write-Host ""

# Step 5: Instructions to run
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Ready to Start!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Services Running:" -ForegroundColor Green
Write-Host "  • Kafka:           http://localhost:9092" -ForegroundColor White
Write-Host "  • Schema Registry: http://localhost:8081" -ForegroundColor White
Write-Host "  • Kafka UI:        http://localhost:8090" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Start the application:" -ForegroundColor White
Write-Host "     mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
Write-Host "  2. In another terminal, test the application:" -ForegroundColor White
Write-Host "     .\test-orders.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "  3. View messages in Kafka UI:" -ForegroundColor White
Write-Host "     http://localhost:8090" -ForegroundColor Cyan
Write-Host ""
Write-Host "To stop everything:" -ForegroundColor Yellow
Write-Host "  docker-compose down" -ForegroundColor Cyan
Write-Host ""

