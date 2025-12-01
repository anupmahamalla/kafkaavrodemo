# Spring Cloud Stream - Test Script
# Run this after starting the application to verify everything works

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Spring Cloud Stream - Testing Script" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$baseUrl = "http://localhost:8080"
$testResults = @()

# Function to test endpoint
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [string]$Body = $null
    )

    Write-Host "Testing: $Name" -ForegroundColor Yellow

    try {
        $headers = @{
            "Content-Type" = "application/json"
        }

        if ($Body) {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Body -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -ErrorAction Stop
        }

        Write-Host "✅ PASSED: $Name" -ForegroundColor Green
        Write-Host "Response: $($response | ConvertTo-Json -Compress)" -ForegroundColor Gray
        Write-Host ""

        return @{
            Test = $Name
            Status = "PASSED"
            Response = $response
        }
    }
    catch {
        Write-Host "❌ FAILED: $Name" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""

        return @{
            Test = $Name
            Status = "FAILED"
            Error = $_.Exception.Message
        }
    }
}

# Wait for application to be ready
Write-Host "Checking if application is running..." -ForegroundColor Cyan
Start-Sleep -Seconds 2

# Test 1: Health Check (if actuator is enabled)
Write-Host "`n--- Test 1: Application Health ---" -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -ErrorAction SilentlyContinue
    Write-Host "✅ Application is UP" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Actuator not enabled or app not running on port 8080" -ForegroundColor Yellow
}

# Test 2: Order Service Test Endpoint
Write-Host "`n--- Test 2: Order Service Availability ---" -ForegroundColor Cyan
$testResults += Test-Endpoint -Name "Order Test Endpoint" -Url "$baseUrl/api/orders/test" -Method "GET"

# Test 3: TaxLot Service Test Endpoint
Write-Host "`n--- Test 3: TaxLot Service Availability ---" -ForegroundColor Cyan
$testResults += Test-Endpoint -Name "TaxLot Test Endpoint" -Url "$baseUrl/api/taxlots/test" -Method "GET"

# Test 4: Send Order Message
Write-Host "`n--- Test 4: Send Order via Spring Cloud Stream ---" -ForegroundColor Cyan
$orderBody = @{
    customerId = "CUST-TEST-001"
    amount = 1234.56
    status = "CREATED"
} | ConvertTo-Json

$testResults += Test-Endpoint -Name "Send Order Message" -Url "$baseUrl/api/orders" -Method "POST" -Body $orderBody

# Test 5: Send TaxLot Message
Write-Host "`n--- Test 5: Send TaxLot via Spring Cloud Stream ---" -ForegroundColor Cyan
$taxLotBody = @{
    eventId = "EVT-TEST-001"
    investmentId = "INV-TEST-001"
    clientShortName = "TEST-CLIENT"
    clientId = 9999
    fundShortName = "TEST-FUND"
    fundId = 8888
    genevaServer = "GENEVA-TEST"
    taxLotId = 777777
    swapCurrency = "USD"
    underlyingInvestmentId = "UND-TEST-001"
    underlyingCurrency = "USD"
    userTranId = "TXN-TEST-001"
} | ConvertTo-Json

$testResults += Test-Endpoint -Name "Send TaxLot Message" -Url "$baseUrl/api/taxlots" -Method "POST" -Body $taxLotBody

# Test 6: Send Multiple Orders
Write-Host "`n--- Test 6: Send Multiple Orders (Load Test) ---" -ForegroundColor Cyan
Write-Host "Sending 5 orders..." -ForegroundColor Gray

$orderCount = 0
for ($i = 1; $i -le 5; $i++) {
    $orderBody = @{
        customerId = "CUST-LOAD-$i"
        amount = [math]::Round((Get-Random -Minimum 100 -Maximum 5000) + (Get-Random) / 100, 2)
        status = "CREATED"
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/orders" -Method POST -Headers @{"Content-Type"="application/json"} -Body $orderBody -ErrorAction Stop
        $orderCount++
        Write-Host "  ✅ Order $i sent" -ForegroundColor Green
    }
    catch {
        Write-Host "  ❌ Order $i failed: $($_.Exception.Message)" -ForegroundColor Red
    }

    Start-Sleep -Milliseconds 100
}

Write-Host "Sent $orderCount/5 orders successfully" -ForegroundColor Cyan
Write-Host ""

# Test 7: Send Multiple TaxLots (Different Partitions)
Write-Host "`n--- Test 7: Send TaxLots to Different Partitions ---" -ForegroundColor Cyan
Write-Host "Sending 3 tax lots with different keys..." -ForegroundColor Gray

$taxLotCount = 0
$investmentIds = @("INV-A", "INV-B", "INV-C")

for ($i = 0; $i -lt $investmentIds.Length; $i++) {
    $taxLotBody = @{
        eventId = "EVT-PART-$($i+1)"
        investmentId = $investmentIds[$i]
        clientShortName = "CLIENT-$([char](65+$i))"
        clientId = 1001 + $i
        fundShortName = "FUND-$([char](65+$i))"
        fundId = 5001 + $i
        genevaServer = "GENEVA-PROD-0$($i+1)"
        taxLotId = 100001 + $i
        swapCurrency = "USD"
        underlyingInvestmentId = "UND-00$($i+1)"
        underlyingCurrency = "USD"
        userTranId = "TXN-00$($i+1)"
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/taxlots" -Method POST -Headers @{"Content-Type"="application/json"} -Body $taxLotBody -ErrorAction Stop
        $taxLotCount++
        Write-Host "  ✅ TaxLot $($i+1) sent with key: $($investmentIds[$i])" -ForegroundColor Green
    }
    catch {
        Write-Host "  ❌ TaxLot $($i+1) failed: $($_.Exception.Message)" -ForegroundColor Red
    }

    Start-Sleep -Milliseconds 100
}

Write-Host "Sent $taxLotCount/3 tax lots successfully" -ForegroundColor Cyan
Write-Host ""

# Summary
Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

$passed = ($testResults | Where-Object { $_.Status -eq "PASSED" }).Count
$failed = ($testResults | Where-Object { $_.Status -eq "FAILED" }).Count

Write-Host "Total Tests: $($testResults.Count)" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor $(if ($failed -gt 0) { "Red" } else { "White" })
Write-Host ""

if ($failed -eq 0) {
    Write-Host "✅ All tests passed! Spring Cloud Stream is working correctly." -ForegroundColor Green
} else {
    Write-Host "⚠️  Some tests failed. Check the errors above." -ForegroundColor Yellow
}

Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "Next Steps" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "1. Check application logs for consumer messages" -ForegroundColor White
Write-Host "2. Verify messages in Kafka topics:" -ForegroundColor White
Write-Host "   - order-details-topic" -ForegroundColor Gray
Write-Host "   - taxlot-details-topic" -ForegroundColor Gray
Write-Host "3. Check Schema Registry for registered schemas:" -ForegroundColor White
Write-Host "   http://localhost:8081/subjects" -ForegroundColor Gray
Write-Host "4. Monitor bindings via Actuator (if enabled):" -ForegroundColor White
Write-Host "   http://localhost:8080/actuator/bindings" -ForegroundColor Gray
Write-Host ""

# Optional: Check logs
Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host "Log Analysis" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Looking for consumer log entries in application output..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Expected log patterns:" -ForegroundColor White
Write-Host "  [Producer] 'Sending order via Spring Cloud Stream'" -ForegroundColor Gray
Write-Host "  [Producer] 'Successfully sent order'" -ForegroundColor Gray
Write-Host "  [Consumer] 'Received order from Spring Cloud Stream'" -ForegroundColor Gray
Write-Host "  [Consumer] 'Processing order with ID'" -ForegroundColor Gray
Write-Host ""
Write-Host "Check your application console for these messages!" -ForegroundColor Cyan
Write-Host ""

