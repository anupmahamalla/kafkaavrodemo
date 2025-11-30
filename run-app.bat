@echo off
REM Quick start script for Kafka Avro Demo

echo ========================================
echo Starting Kafka Avro Demo Application
echo ========================================
echo.

cd /d "%~dp0"

echo Checking for running Java processes...
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I /N "java.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo WARNING: Java process already running!
    echo Please stop it first or use a different terminal.
    pause
    exit /b 1
)

echo Starting application from JAR...
echo.
java -jar target\kafkaavrodemo-0.0.1-SNAPSHOT.jar

pause

