@echo off
echo ========================================
echo Starting Kor Constructions Application
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Java and Maven found!
echo.
echo Starting application...
echo.

REM Start the application in the background and wait for it to start
start /B mvn spring-boot:run

echo Waiting for application to start...
timeout /t 15 /nobreak >nul

REM Open the default browser
echo Opening browser...
start http://localhost:8080

echo.
echo ========================================
echo Application is running!
echo URL: http://localhost:8080
echo.
echo To stop the application:
echo 1. Close this window, OR
echo 2. Press Ctrl+C in this window
echo ========================================
echo.

REM Keep the window open
pause
