@echo off
echo ========================================
echo Starting Kor Constructions Application
echo (With Custom Data Location)
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

REM Check if JAR file exists
if not exist "target\kor-constructions-app-1.0.0.jar" (
    echo ERROR: JAR file not found!
    echo Please run "build-jar.bat" first
    pause
    exit /b 1
)

REM Check if custom data location is configured
if exist "data-location.properties" (
    echo Loading custom data location...
    echo.
    for /f "tokens=2 delims==" %%a in ('type data-location.properties ^| findstr "app.data.directory"') do set DATA_DIR=%%a
    echo Data Directory: %DATA_DIR%
    echo.
) else (
    echo Using default data location: ./data
    set DATA_DIR=./data
    echo.
)

echo Starting application...
echo.
echo Application will be available at: http://localhost:8080
echo Browser will open automatically in a few seconds...
echo.
echo To stop the application, close this window or press Ctrl+C
echo ========================================
echo.

REM Start the JAR file with custom properties
if exist "data-location.properties" (
    start /B java -jar target\kor-constructions-app-1.0.0.jar --spring.config.additional-location=file:./data-location.properties
) else (
    start /B java -jar target\kor-constructions-app-1.0.0.jar
)

REM Wait for application to start
echo Waiting for application to start...
timeout /t 15 /nobreak >nul

REM Open browser
start http://localhost:8080

echo.
echo Application is running with data in: %DATA_DIR%
echo.

REM Keep window open
cmd /k
