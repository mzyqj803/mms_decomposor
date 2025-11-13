@echo off
setlocal enabledelayedexpansion

REM MMS Docker Offline Package Export Script
REM Using English to avoid encoding issues

echo ============================================================
echo MMS System - Docker Offline Package Export
echo ============================================================
echo.

REM Get date
for /f %%i in ('powershell -Command "Get-Date -Format yyyyMMdd"') do set EXPORT_DATE=%%i
set PROJECT_ROOT=%~dp0..
set RELEASE_DIR=%PROJECT_ROOT%\release
set PACKAGE_NAME=mms-offline-package-%EXPORT_DATE%

echo [1/7] Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker not found
    pause
    exit /b 1
)
echo OK: Docker found
echo.

echo [2/7] Checking 7-Zip...
set "SEVENZIP_PATH="
where 7z >nul 2>&1
if errorlevel 1 (
    echo 7-Zip not found in PATH, checking Program Files...
    if exist "%ProgramFiles%\7-Zip\7z.exe" (
        set "SEVENZIP_PATH=%ProgramFiles%\7-Zip\7z.exe"
        echo OK: 7-Zip found in Program Files
        set SKIP_7Z=0
    ) else if exist "%ProgramFiles(x86)%\7-Zip\7z.exe" (
        set "SEVENZIP_PATH=%ProgramFiles(x86)%\7-Zip\7z.exe"
        echo OK: 7-Zip found in Program Files x86
        set SKIP_7Z=0
    ) else (
        echo WARNING: 7-Zip not found
        echo Will skip creating archive
        echo Please install 7-Zip from 7-zip.org
        set SKIP_7Z=1
    )
) else (
    set "SEVENZIP_PATH=7z"
    echo OK: 7-Zip found in PATH
    set SKIP_7Z=0
)
echo.

echo [3/7] Creating directories...
if not exist "%RELEASE_DIR%" mkdir "%RELEASE_DIR%"
if exist "%RELEASE_DIR%\%PACKAGE_NAME%" rd /s /q "%RELEASE_DIR%\%PACKAGE_NAME%"
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%"
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images"
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%\project-files"
echo OK: Directories created
echo.

echo [4/7] Building project images...
cd "%PROJECT_ROOT%"
docker-compose build backend
if errorlevel 1 (
    echo ERROR: Backend build failed
    cd script
    pause
    exit /b 1
)
docker-compose build frontend
if errorlevel 1 (
    echo ERROR: Frontend build failed
    cd script
    pause
    exit /b 1
)
cd script
echo OK: Images built
echo.

echo [5/7] Exporting Docker images...
echo This may take several minutes...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mariadb-11.tar" mariadb:11
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\redis-6.0-alpine.tar" redis:6.0-alpine
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mms-backend-latest.tar" mms-backend:latest
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mms-frontend-latest.tar" mms-frontend:latest
echo OK: Images exported
echo.

echo [6/7] Copying project files and generating install scripts...
cd "%PROJECT_ROOT%"
copy /Y "docker-compose.yml" "%RELEASE_DIR%\%PACKAGE_NAME%\project-files\" >nul
xcopy /E /I /Y "src\main\resources\sql\data_init" "%RELEASE_DIR%\%PACKAGE_NAME%\project-files\data_init\" >nul

echo Generating install.bat...
copy /Y "%PROJECT_ROOT%\script\install-template.bat" "%RELEASE_DIR%\%PACKAGE_NAME%\install.bat" >nul
powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%\script\generate-install.ps1" "%RELEASE_DIR%\%PACKAGE_NAME%\install.bat" "upgrade"

echo Generating initial_install.bat...
copy /Y "%PROJECT_ROOT%\script\install-template.bat" "%RELEASE_DIR%\%PACKAGE_NAME%\initial_install.bat" >nul
powershell -ExecutionPolicy Bypass -File "%PROJECT_ROOT%\script\generate-install.ps1" "%RELEASE_DIR%\%PACKAGE_NAME%\initial_install.bat" "fresh"

echo Generating README.md...
copy /Y "%PROJECT_ROOT%\docs\README-INSTALL.md" "%RELEASE_DIR%\%PACKAGE_NAME%\README.md" >nul 2>&1
if errorlevel 1 (
    echo # MMS System - Offline Installation Package > "%RELEASE_DIR%\%PACKAGE_NAME%\README.md"
    echo. >> "%RELEASE_DIR%\%PACKAGE_NAME%\README.md"
    echo ## Installation >> "%RELEASE_DIR%\%PACKAGE_NAME%\README.md"
    echo. >> "%RELEASE_DIR%\%PACKAGE_NAME%\README.md"
    echo - Use install.bat for upgrade installation (keeps data^) >> "%RELEASE_DIR%\%PACKAGE_NAME%\README.md"
    echo - Use initial_install.bat for fresh installation (clears all data^) >> "%RELEASE_DIR%\%PACKAGE_NAME%\README.md"
)

cd script
echo OK: Files copied and install scripts generated
echo.

echo [7/7] Creating self-extracting archive...
if !SKIP_7Z! equ 0 (
    echo This may take several minutes...
    cd "%RELEASE_DIR%"
    "%SEVENZIP_PATH%" a -sfx -mx=5 "%PACKAGE_NAME%.exe" "%PACKAGE_NAME%\" >nul 2>&1
    if errorlevel 1 (
        echo WARNING: Failed to create self-extracting archive
        echo Trying standard 7z archive instead...
        "%SEVENZIP_PATH%" a -t7z -mx=5 "%PACKAGE_NAME%.7z" "%PACKAGE_NAME%\" >nul 2>&1
        if errorlevel 1 (
            echo WARNING: Failed to create 7z archive
        ) else (
            echo OK: 7z archive created
            echo File: %PACKAGE_NAME%.7z
            set HAS_7Z=1
        )
    ) else (
        echo OK: Self-extracting archive created
        echo File: %PACKAGE_NAME%.exe
        set HAS_SFX=1
    )
    cd "%PROJECT_ROOT%\script"
)
echo.

echo Package ready!
echo.
echo ============================================================
echo Export Complete!
echo ============================================================
echo.
echo Package folder: %RELEASE_DIR%\%PACKAGE_NAME%
if defined HAS_SFX (
    echo Self-extracting archive: %RELEASE_DIR%\%PACKAGE_NAME%.exe
    echo.
    echo To install from the .exe file:
    echo 1. Run %PACKAGE_NAME%.exe to extract
    echo 2. Navigate to the extracted folder
    echo 3. Run install.bat to upgrade or initial_install.bat for fresh install
) else (
    if defined HAS_7Z (
        echo 7z archive: %RELEASE_DIR%\%PACKAGE_NAME%.7z
        echo.
        echo To install from the .7z file:
        echo 1. Extract %PACKAGE_NAME%.7z using 7-Zip
        echo 2. Navigate to the extracted folder
        echo 3. Run install.bat to upgrade or initial_install.bat for fresh install
    ) else (
        if !SKIP_7Z! equ 0 (
            echo.
            echo NOTE: Archive creation failed
            echo Please use the folder directly
        ) else (
            echo.
            echo NOTE: Install 7-Zip to enable archive creation
            echo Download from 7-zip.org
        )
    )
)
echo.
echo Installation scripts in the package:
echo - install.bat: Upgrade installation, keeps data
echo - initial_install.bat: Fresh installation, clears all data
echo - install.sh: Linux installation script
echo - README.md: Documentation
echo.
pause
