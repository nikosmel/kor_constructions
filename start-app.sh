#!/bin/bash

echo "========================================"
echo "Starting Kor Constructions Application"
echo "========================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    echo "Download from: https://adoptium.net/"
    read -p "Press Enter to exit..."
    exit 1
fi

# Change to the script's directory
cd "$(dirname "$0")"

# Check if JAR file exists
if [ ! -f "target/kor-constructions-app-1.0.0.jar" ]; then
    echo "ERROR: JAR file not found!"
    echo ""
    echo "Please build the project first by running:"
    echo "mvn clean package"
    echo ""
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Starting application from JAR file..."
echo ""

# Check if custom data location is configured
if [ -f "data-location.properties" ]; then
    echo "[Cloud/Network Mode] Custom data location detected"
    DATA_DIR=$(grep 'app.data.directory' data-location.properties | cut -d'=' -f2)
    echo "Data Directory: $DATA_DIR"
    echo ""
else
    echo "[Local Mode] Using default data location: ./data"
    echo ""
fi

echo "Application will be available at: http://localhost:8080"
echo ""
echo "The browser will open automatically in a few seconds..."
echo ""
echo "To stop the application, press Ctrl+C"
echo "========================================"
echo ""

# Start the JAR file with or without custom properties
if [ -f "data-location.properties" ]; then
    java -jar target/kor-constructions-app-1.0.0.jar --spring.config.additional-location=file:./data-location.properties &
else
    java -jar target/kor-constructions-app-1.0.0.jar &
fi

APP_PID=$!

# Wait for application to start
echo "Waiting for application to start..."
sleep 15

# Open browser
open http://localhost:8080

echo ""
echo "Application is running!"
if [ -f "data-location.properties" ]; then
    echo "Data location: $DATA_DIR"
else
    echo "Data location: ./data (local)"
fi
echo ""
echo "Press Ctrl+C to stop"

# Wait for the application process
wait $APP_PID
