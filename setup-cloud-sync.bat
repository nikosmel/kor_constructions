@echo off
echo ========================================
echo Cloud Storage Setup for Kor Constructions
echo ========================================
echo.
echo This script will help you configure the application
echo to use a cloud-synced data folder.
echo.
echo SUPPORTED CLOUD SERVICES:
echo - Dropbox
echo - Google Drive
echo - OneDrive
echo - Any other cloud storage with local sync
echo.
echo ========================================
echo.

echo STEP 1: Choose your cloud folder location
echo.
echo Examples:
echo   Dropbox:      C:\Users\YourName\Dropbox\KorConstructions-Data
echo   Google Drive: C:\Users\YourName\Google Drive\KorConstructions-Data
echo   OneDrive:     C:\Users\YourName\OneDrive\KorConstructions-Data
echo.

set /p CLOUD_PATH="Enter the FULL path to your cloud folder: "

if "%CLOUD_PATH%"=="" (
    echo ERROR: No path provided!
    pause
    exit /b 1
)

echo.
echo Checking if folder exists...

if not exist "%CLOUD_PATH%" (
    echo Folder does not exist. Creating it...
    mkdir "%CLOUD_PATH%"
    if errorlevel 1 (
        echo ERROR: Could not create folder!
        echo Please check the path and try again.
        pause
        exit /b 1
    )
    echo Folder created successfully!
)

echo.
echo STEP 2: Copy existing data (if any)...

if exist "data" (
    echo Copying current data to cloud folder...
    xcopy /E /I /Y data "%CLOUD_PATH%"
    echo Data copied!
) else (
    echo No existing data to copy.
    mkdir "%CLOUD_PATH%"
)

echo.
echo STEP 3: Creating configuration file...

echo app.data.directory=%CLOUD_PATH% > data-location.properties

echo Configuration saved!

echo.
echo ========================================
echo SETUP COMPLETE!
echo ========================================
echo.
echo Data folder location: %CLOUD_PATH%
echo.
echo IMPORTANT - ON THE SECOND LAPTOP:
echo 1. Install the same cloud storage app (Dropbox/Drive/OneDrive)
echo 2. Wait for the folder to sync
echo 3. Copy the entire KorConstructions folder to the second laptop
echo 4. Run this setup script again with the SAME cloud path
echo.
echo Both laptops will now share the same data!
echo.
echo ========================================
pause
