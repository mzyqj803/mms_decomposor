@echo off
chcp 65001 >nul 2>&1
echo Starting MMS Manufacturing Management System...

REM Check if Java is installed
echo Checking Java environment...
call java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not configured in environment variables
    echo Please install JDK 21+ first
    echo.
    echo Solution:
    echo 1. Download JDK: https://adoptium.net/
    echo 2. Install JDK 21 or higher version
    echo 3. Configure JAVA_HOME environment variable
    echo 4. Add %%JAVA_HOME%%\bin to PATH environment variable
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Java environment is ready
)

REM Check if Maven is installed
echo Checking Maven environment...
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed or not configured in environment variables
    echo Please install Maven 3.6+ first
    echo.
    echo Solution:
    echo 1. Download Maven: https://maven.apache.org/download.cgi
    echo 2. Extract to specified directory
    echo 3. Configure MAVEN_HOME environment variable
    echo 4. Add %%MAVEN_HOME%%\bin to PATH environment variable
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Maven environment is ready
)

REM Check if Node.js is installed
echo Checking Node.js environment...
call node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed or not configured in environment variables
    echo Please install Node.js 16+ first
    echo.
    echo Solution:
    echo 1. Download Node.js: https://nodejs.org/
    echo 2. Choose LTS version to install
    echo 3. PATH environment variable will be configured automatically during installation
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Node.js environment is ready
)

REM Check if Docker is installed
echo Checking Docker environment...
call docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed, please install Docker Desktop first
    echo.
    echo Solution:
    echo 1. Download Docker Desktop: https://www.docker.com/products/docker-desktop
    echo 2. Install Docker Desktop
    echo 3. Start Docker Desktop service
    echo 4. Ensure Docker service is running
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Docker environment is ready
)

REM Check if Docker Compose is installed
echo Checking Docker Compose environment...
call docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose is not installed, please install Docker Compose first
    echo.
    echo Solution:
    echo 1. Docker Desktop usually includes Docker Compose
    echo 2. Ensure Docker Desktop is properly installed
    echo 3. Restart Docker Desktop service
    echo.
    pause
    exit /b 1
) else (
    echo [OK] Docker Compose environment is ready
)

echo [OK] Environment check passed, starting to build and launch services...
echo.

REM Build backend application
echo.
echo [1/4] Building backend application...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Backend application build failed
    pause
    exit /b 1
)

REM Install frontend dependencies
echo.
echo [2/4] Installing frontend dependencies...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo [ERROR] Frontend dependencies installation failed
    pause
    exit /b 1
)

REM Build frontend application
echo.
echo [3/4] Building frontend application...
call npm run build
if %errorlevel% neq 0 (
    echo [ERROR] Frontend application build failed
    pause
    exit /b 1
)
cd ..

REM Start all services
echo.
echo [4/4] Starting all services...
docker-compose up -d

if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] MMS Manufacturing Management System started successfully!
    echo.
    echo Access URLs:
    echo   Frontend Application: http://localhost:3000
    echo   Backend API: http://localhost:8080/api
    echo.
    echo Database connection info:
    echo   Host: localhost:3306
    echo   Database: mms_db
    echo   Username: mms_user
    echo   Password: mms_password
    echo.
    echo Redis connection info:
    echo   Host: localhost:6379
    echo.
    echo Management commands:
    echo   View logs: docker-compose logs -f
    echo   Stop services: docker-compose down
    echo   Restart services: docker-compose restart
    echo.
    echo Press any key to open browser and access the application...
    pause >nul
    start http://localhost:3000
) else (
    echo [ERROR] Service startup failed, please check logs
    echo Use the following command to view detailed error information:
    echo docker-compose logs
    pause
    exit /b 1
)