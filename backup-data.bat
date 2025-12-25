@echo off
echo ========================================
echo Kor Constructions - Data Backup
echo ========================================
echo.

REM Set backup directory
set BACKUP_BASE=C:\Backups\KorConstructions

REM Create date stamp (YYYYMMDD_HHMMSS)
set DATETIME=%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set DATETIME=%DATETIME: =0%

set BACKUP_DIR=%BACKUP_BASE%\%DATETIME%

echo Backup will be saved to:
echo %BACKUP_DIR%
echo.

REM Check if data folder exists
if not exist "data" (
    echo ERROR: data folder not found!
    echo Make sure you run this from the KorConstructions folder
    pause
    exit /b 1
)

REM Create backup directory
mkdir "%BACKUP_DIR%" 2>nul
if errorlevel 1 (
    echo Creating backup parent directory...
    mkdir "%BACKUP_BASE%" 2>nul
    mkdir "%BACKUP_DIR%"
)

echo Copying data files...
xcopy /E /I /Y data "%BACKUP_DIR%\data" >nul

if errorlevel 1 (
    echo ERROR: Backup failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Backup completed successfully!
echo ========================================
echo.
echo Backup location: %BACKUP_DIR%
echo.
echo Files backed up:
dir /b "%BACKUP_DIR%\data"
echo.
echo Total size:
dir "%BACKUP_DIR%\data" | find "File(s)"
echo.
echo ========================================
echo.
echo TIP: Run this backup regularly!
echo You can also copy the backup folder to an external drive.
echo.
pause
