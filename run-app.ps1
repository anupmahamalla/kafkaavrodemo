# Quick Start Script for Kafka Avro Demo
# Run this to start your application after all fixes

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Starting Kafka Avro Demo Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Change to script directory
Set-Location -Path $PSScriptRoot

# Check for running Java processes
$javaProcesses = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "WARNING: Java process already running!" -ForegroundColor Yellow
    Write-Host "Process IDs: $($javaProcesses.Id -join ', ')" -ForegroundColor Yellow
    $response = Read-Host "Do you want to stop them and continue? (y/n)"
    if ($response -eq 'y') {
        $javaProcesses | Stop-Process -Force
        Write-Host "Java processes stopped." -ForegroundColor Green
        Start-Sleep -Seconds 2
    } else {
        Write-Host "Exiting. Please stop Java processes manually." -ForegroundColor Red
        exit 1
    }
}

Write-Host "Starting application from JAR..." -ForegroundColor Green
Write-Host ""

# Run the application
java -jar target\kafkaavrodemo-0.0.1-SNAPSHOT.jar

