@echo off
REM Quick Start Script for Spring Cloud Stream Kafka Demo
REM This script will help you get started quickly

echo ================================================
echo Spring Cloud Stream Kafka Demo - Quick Start
echo ================================================
echo.

:menu
echo What would you like to do?
echo.
echo 1. Start Docker Infrastructure (Kafka + Schema Registry)
echo 2. Build the Application
echo 3. Run the Application
echo 4. Run Tests
echo 5. Stop Docker Infrastructure
echo 6. View Logs
echo 7. Clean Everything
echo 8. Exit
echo.
set /p choice="Enter your choice (1-8): "

if "%choice%"=="1" goto start_docker
if "%choice%"=="2" goto build
if "%choice%"=="3" goto run
if "%choice%"=="4" goto test
if "%choice%"=="5" goto stop_docker
if "%choice%"=="6" goto logs
if "%choice%"=="7" goto clean
if "%choice%"=="8" goto end

echo Invalid choice. Please try again.
echo.
goto menu

:start_docker
echo.
echo Starting Docker infrastructure...
docker-compose up -d
echo.
echo Waiting for services to be ready...
timeout /t 10
echo.
echo Checking services...
docker ps
echo.
echo Services started! Press any key to return to menu...
pause > nul
goto menu

:build
echo.
echo Building the application...
call mvnw.cmd clean package -DskipTests
echo.
echo Build complete! Press any key to return to menu...
pause > nul
goto menu

:run
echo.
echo Starting the application...
echo (Press Ctrl+C to stop)
echo.
call mvnw.cmd spring-boot:run
pause
goto menu

:test
echo.
echo Running tests...
powershell -ExecutionPolicy Bypass -File test-spring-cloud-stream.ps1
echo.
echo Tests complete! Press any key to return to menu...
pause > nul
goto menu

:stop_docker
echo.
echo Stopping Docker infrastructure...
docker-compose down
echo.
echo Services stopped! Press any key to return to menu...
pause > nul
goto menu

:logs
echo.
echo Recent Docker logs:
echo.
docker-compose logs --tail=50
echo.
echo Press any key to return to menu...
pause > nul
goto menu

:clean
echo.
echo Cleaning everything...
echo.
echo Stopping Docker...
docker-compose down -v
echo.
echo Cleaning Maven build...
call mvnw.cmd clean
echo.
echo Clean complete! Press any key to return to menu...
pause > nul
goto menu

:end
echo.
echo Thank you for using Spring Cloud Stream Kafka Demo!
echo.
exit /b 0

