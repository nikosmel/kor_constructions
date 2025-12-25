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
echo Please wait while the application starts...
echo.
echo Once started, the application will be available at:
echo http://localhost:8080
echo.
echo Press Ctrl+C to stop the application
echo ========================================
echo.

REM Start the application
mvn spring-boot:run

pause
