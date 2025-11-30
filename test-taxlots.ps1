# Test script for TaxLot API endpoints
# This script tests the TaxLot producer endpoint

Write-Host "Testing TaxLot API Endpoints" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green

# Base URL
$baseUrl = "http://localhost:8080"

# Test 1: Health check endpoint
Write-Host "`nTest 1: Testing health check endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/taxlots/test" -Method Get
    Write-Host "Response: $response" -ForegroundColor Green
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

# Test 2: Create a sample TaxLot
Write-Host "`nTest 2: Creating a sample tax lot..." -ForegroundColor Yellow

$currentTimestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

$taxLotData = @{
    eventId = "EVT-001"
    investmentId = "INV-12345"
    clientShortName = "CLIENT-A"
    clientId = 1001
    fundShortName = "FUND-XYZ"
    fundId = 5001
    genevaServer = "GENEVA-PROD-01"
    taxLotId = 100001
    accrualDate = $currentTimestamp
    tradeDate = $currentTimestamp
    effectiveDate = $currentTimestamp
    quantity = 1000.500
    tradeNotional = 250000.75
    tradePrice = 250.50
    resetPrice = 251.00
    swapCurrency = "USD"
    spread = 0.025
    marketPrice = 252.25
    underlyingInvestmentId = "UNDERLYING-INV-001"
    underlyingCurrency = "USD"
    userTranId = "TXN-2024-001"
    knowledgeDate = $currentTimestamp
} | ConvertTo-Json

Write-Host "Request Body:" -ForegroundColor Cyan
Write-Host $taxLotData

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/taxlots" `
        -Method Post `
        -Body $taxLotData `
        -ContentType "application/json"
    Write-Host "`nResponse: $response" -ForegroundColor Green
} catch {
    Write-Host "`nError: $_" -ForegroundColor Red
    Write-Host "Error Details: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Create another TaxLot with different data
Write-Host "`nTest 3: Creating another tax lot..." -ForegroundColor Yellow

$taxLotData2 = @{
    eventId = "EVT-002"
    investmentId = "INV-67890"
    clientShortName = "CLIENT-B"
    clientId = 1002
    fundShortName = "FUND-ABC"
    fundId = 5002
    genevaServer = "GENEVA-PROD-02"
    taxLotId = 100002
    quantity = 2500.750
    tradeNotional = 500000.50
    tradePrice = 200.25
    resetPrice = 201.00
    swapCurrency = "EUR"
    spread = 0.030
    marketPrice = 202.50
    underlyingInvestmentId = "UNDERLYING-INV-002"
    underlyingCurrency = "EUR"
    userTranId = "TXN-2024-002"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/taxlots" `
        -Method Post `
        -Body $taxLotData2 `
        -ContentType "application/json"
    Write-Host "Response: $response" -ForegroundColor Green
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

Write-Host "`n==============================" -ForegroundColor Green
Write-Host "Testing complete!" -ForegroundColor Green
Write-Host "`nNote: Check the application logs to see the consumer processing these messages" -ForegroundColor Cyan

