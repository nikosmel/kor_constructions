@echo off
echo ========================================
echo Building Kor Constructions Application
echo ========================================
echo.
echo This will create a standalone JAR file that can run without Maven
echo.

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven to build the JAR file
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Building JAR file...
echo This may take a few minutes...
echo.

REM Clean and package
call mvn clean package -DskipTests

if errorlevel 1 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build successful!
echo JAR file created at: target\kor-constructions-app-1.0.0.jar
echo.
echo You can now use "run-jar.bat" to start the application
echo without needing Maven installed!
echo ========================================
echo.
pause
