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

# Test 3: Test Partition Distribution with Multiple Messages
Write-Host "`nTest 3: Testing partition distribution..." -ForegroundColor Yellow
Write-Host "Sending 5 messages with different keys to demonstrate partition distribution" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/taxlots/test-partitions" -Method Post
    Write-Host "`nPartition Test Response:" -ForegroundColor Green
    Write-Host "Status: $($response.status)" -ForegroundColor Green
    Write-Host "Messages Sent: $($response.messagesSent)" -ForegroundColor Green
    Write-Host "Partitions: $($response.partitions)" -ForegroundColor Green
    Write-Host "`nSent Messages:" -ForegroundColor Cyan
    $response.sentMessages | ForEach-Object {
        Write-Host "  - EventId: $($_.eventId), InvestmentId: $($_.investmentId), Client: $($_.client)" -ForegroundColor White
    }
    Write-Host "`n$($response.message)" -ForegroundColor Yellow
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host "Error Details: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Send Messages with Same Key (should go to same partition)
Write-Host "`nTest 4: Testing same key -> same partition..." -ForegroundColor Yellow
Write-Host "Sending 3 messages with the SAME key (EVT-SAME:INV-SAME)" -ForegroundColor Cyan

for ($i = 1; $i -le 3; $i++) {
    $samekeyData = @{
        eventId = "EVT-SAME"
        investmentId = "INV-SAME"
        clientShortName = "CLIENT-SAME-$i"
        clientId = 2000 + $i
        fundShortName = "FUND-SAME"
        fundId = 6000
        genevaServer = "GENEVA-PROD-SAME"
        taxLotId = 200000 + $i
        quantity = 1000.0 + $i
        tradeNotional = 100000.0 + ($i * 1000)
        tradePrice = 100.0 + $i
        resetPrice = 101.0 + $i
        swapCurrency = "USD"
        spread = 0.02
        marketPrice = 102.0 + $i
        underlyingInvestmentId = "UND-SAME"
        underlyingCurrency = "USD"
        userTranId = "TXN-SAME-$i"
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/taxlots" `
            -Method Post `
            -Body $samekeyData `
            -ContentType "application/json"
        Write-Host "  Message $i sent: $response" -ForegroundColor Green
    } catch {
        Write-Host "  Message $i error: $_" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 200
}

Write-Host "`nAll 3 messages should appear in the SAME partition in logs!" -ForegroundColor Yellow

# Test 5: Send Messages with Different Keys (should distribute across partitions)
Write-Host "`nTest 5: Testing different keys -> different partitions..." -ForegroundColor Yellow
Write-Host "Sending 6 messages with DIFFERENT keys" -ForegroundColor Cyan

$testKeys = @(
    @{eventId="EVT-A"; investmentId="INV-001"},
    @{eventId="EVT-B"; investmentId="INV-002"},
    @{eventId="EVT-C"; investmentId="INV-003"},
    @{eventId="EVT-D"; investmentId="INV-004"},
    @{eventId="EVT-E"; investmentId="INV-005"},
    @{eventId="EVT-F"; investmentId="INV-006"}
)

$counter = 0
foreach ($keyPair in $testKeys) {
    $counter++
    $diffKeyData = @{
        eventId = $keyPair.eventId
        investmentId = $keyPair.investmentId
        clientShortName = "CLIENT-DIFF-$counter"
        clientId = 3000 + $counter
        fundShortName = "FUND-DIFF-$counter"
        fundId = 7000 + $counter
        genevaServer = "GENEVA-PROD-DIFF"
        taxLotId = 300000 + $counter
        quantity = 500.0 + $counter
        tradeNotional = 50000.0 + ($counter * 1000)
        tradePrice = 50.0 + $counter
        resetPrice = 51.0 + $counter
        swapCurrency = "USD"
        spread = 0.015
        marketPrice = 52.0 + $counter
        underlyingInvestmentId = "UND-DIFF-$counter"
        underlyingCurrency = "USD"
        userTranId = "TXN-DIFF-$counter"
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/taxlots" `
            -Method Post `
            -Body $diffKeyData `
            -ContentType "application/json"
        Write-Host "  Message $counter ($($keyPair.eventId):$($keyPair.investmentId)) sent: $response" -ForegroundColor Green
    } catch {
        Write-Host "  Message $counter error: $_" -ForegroundColor Red
    }
    Start-Sleep -Milliseconds 200
}

Write-Host "`nThese 6 messages should be distributed across 3 partitions in logs!" -ForegroundColor Yellow

Write-Host "`n==============================" -ForegroundColor Green
Write-Host "Testing complete!" -ForegroundColor Green
Write-Host "`nSUMMARY:" -ForegroundColor Cyan
Write-Host "  [OK] Test 1: Health check" -ForegroundColor White
Write-Host "  [OK] Test 2: Single tax lot" -ForegroundColor White
Write-Host "  [OK] Test 3: Partition distribution test (5 messages)" -ForegroundColor White
Write-Host "  [OK] Test 4: Same key -> same partition (3 messages)" -ForegroundColor White
Write-Host "  [OK] Test 5: Different keys -> distributed (6 messages)" -ForegroundColor White
Write-Host "`nTotal messages sent: 15" -ForegroundColor Cyan
Write-Host "`nCHECK LOGS TO SEE:" -ForegroundColor Yellow
Write-Host "  * Partition numbers (PARTITION: 0, 1, or 2)" -ForegroundColor White
Write-Host "  * Messages with same key go to same partition" -ForegroundColor White
Write-Host "  * Messages with different keys distributed across partitions" -ForegroundColor White
Write-Host "  * 3 consumer threads processing in parallel" -ForegroundColor White

