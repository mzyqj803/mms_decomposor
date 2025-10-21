@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM MMS Docker 离线安装包导出脚本
REM ============================================================

echo.
echo ============================================================
echo MMS制造管理系统 - Docker离线安装包导出工具
echo ============================================================
echo.

REM 设置导出目录
REM 使用 PowerShell 获取日期，避免中文字符
for /f %%i in ('powershell -Command "Get-Date -Format 'yyyyMMdd'"') do set EXPORT_DATE=%%i
set PROJECT_ROOT=%~dp0..
set RELEASE_DIR=%PROJECT_ROOT%\release
set PACKAGE_NAME=mms-offline-package-%EXPORT_DATE%

echo [1/9] 检查Docker环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Docker，请先安装Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker环境检查通过

echo.
echo [2/9] 创建release目录...
if not exist "%RELEASE_DIR%" (
    mkdir "%RELEASE_DIR%"
    echo [✓] release目录创建完成
) else (
    echo [✓] release目录已存在
)

echo.
echo [3/9] 创建导出目录...
if exist "%RELEASE_DIR%\%PACKAGE_NAME%" (
    echo [提示] 目录已存在，正在清理...
    rd /s /q "%RELEASE_DIR%\%PACKAGE_NAME%"
)
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%"
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images"
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%\project-files"
echo [✓] 目录创建完成: %RELEASE_DIR%\%PACKAGE_NAME%

echo.
echo [4/9] 构建最新的项目镜像...
cd "%PROJECT_ROOT%"
echo [提示] 正在构建后端镜像...
docker-compose build backend
if errorlevel 1 (
    echo [错误] 后端镜像构建失败
    cd script
    pause
    exit /b 1
)

echo [提示] 正在构建前端镜像...
docker-compose build frontend
if errorlevel 1 (
    echo [错误] 前端镜像构建失败
    cd script
    pause
    exit /b 1
)
echo [✓] 项目镜像构建完成
cd script

echo.
echo [5/9] 导出Docker镜像...
echo [提示] 这可能需要几分钟时间，请耐心等待...

echo   导出 mariadb:11 ...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mariadb-11.tar" mariadb:11
if errorlevel 1 (
    echo [错误] MariaDB镜像导出失败
    pause
    exit /b 1
)

echo   导出 redis:6.0-alpine ...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\redis-6.0-alpine.tar" redis:6.0-alpine
if errorlevel 1 (
    echo [错误] Redis镜像导出失败
    pause
    exit /b 1
)

echo   导出 mms-backend:latest ...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mms-backend-latest.tar" mms-backend:latest
if errorlevel 1 (
    echo [错误] 后端镜像导出失败
    pause
    exit /b 1
)

echo   导出 mms-frontend:latest ...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mms-frontend-latest.tar" mms-frontend:latest
if errorlevel 1 (
    echo [错误] 前端镜像导出失败
    pause
    exit /b 1
)
echo [✓] 所有镜像导出完成

echo.
echo [6/9] 复制项目文件...
cd "%PROJECT_ROOT%"
copy /Y "docker-compose.yml" "%RELEASE_DIR%\%PACKAGE_NAME%\project-files\" >nul
xcopy /E /I /Y "src\main\resources\sql\data_init" "%RELEASE_DIR%\%PACKAGE_NAME%\project-files\data_init\" >nul
xcopy /E /I /Y "docs\*.md" "%RELEASE_DIR%\%PACKAGE_NAME%\project-files\docs\" >nul 2>nul
cd script
echo [✓] 项目文件复制完成

echo.
echo [7/9] 创建离线安装脚本...

REM 创建Windows安装脚本
(
echo @echo off
echo chcp 65001 ^>nul
echo setlocal enabledelayedexpansion
echo.
echo REM 切换到脚本所在目录
echo cd /d "%%~dp0"
echo.
echo echo ============================================================
echo echo MMS制造管理系统 - 离线安装脚本
echo echo ============================================================
echo echo.
echo.
echo echo [1/4] 检查Docker环境...
echo docker --version ^>nul 2^>^&1
echo if errorlevel 1 ^(
echo     echo [错误] 未检测到Docker，请先安装Docker Desktop
echo     pause
echo     exit /b 1
echo ^)
echo echo [✓] Docker环境检查通过
echo.
echo echo.
echo echo [2/4] 加载Docker镜像...
echo echo [提示] 这可能需要几分钟时间，请耐心等待...
echo.
echo echo   加载 mariadb:11 ...
echo docker load -i docker-images\mariadb-11.tar
echo echo   加载 redis:6.0-alpine ...
echo docker load -i docker-images\redis-6.0-alpine.tar
echo echo   加载 mms-backend:latest ...
echo docker load -i docker-images\mms-backend-latest.tar
echo echo   加载 mms-frontend:latest ...
echo docker load -i docker-images\mms-frontend-latest.tar
echo echo [✓] 所有镜像加载完成
echo.
echo echo.
echo echo [3/4] 复制配置文件...
echo copy /Y project-files\docker-compose.yml .
echo echo [提示] 创建数据库初始化目录...
echo if not exist src\main\resources\sql\data_init mkdir src\main\resources\sql\data_init
echo xcopy /E /I /Y project-files\data_init src\main\resources\sql\data_init ^>nul
echo echo [✓] 配置文件复制完成
echo.
echo echo.
echo echo [4/4] 启动服务...
echo echo [提示] 检查并清理旧容器和数据卷...
echo docker rm -f mms-redis mms-mariadb mms-backend mms-frontend ^>nul 2^>^&1
echo docker-compose down -v ^>nul 2^>^&1
echo echo [提示] 正在启动服务...
echo docker-compose up -d
echo if errorlevel 1 ^(
echo     echo [错误] 服务启动失败
echo     pause
echo     exit /b 1
echo ^)
echo echo [✓] 容器启动成功
echo.
echo echo [提示] 等待数据库就绪并初始化...
echo echo 这可能需要2-3分钟，请耐心等待...
echo echo.
echo :wait_db
echo docker exec mms-mariadb mariadb -uroot -ppassword -e "SELECT 1" ^>nul 2^>^&1
echo if errorlevel 1 ^(
echo     echo 等待中...
echo     timeout /t 10 /nobreak ^>nul
echo     goto :wait_db
echo ^)
echo echo [✓] 数据库已就绪
echo.
echo echo [提示] 等待后端服务启动...
echo echo 正在等待后端服务完成启动（约30秒）...
echo timeout /t 30 /nobreak ^>nul
echo echo [✓] 服务启动成功
echo.
echo echo.
echo echo ============================================================
echo echo 安装完成！
echo echo ============================================================
echo echo.
echo echo 服务访问地址：
echo echo   Frontend: http://localhost:9000
echo echo   Backend: http://localhost:8080/api  
echo echo   Database: localhost:3307
echo echo   Redis: localhost:6379
echo echo.
echo echo 常用命令：
echo echo   查看服务状态: docker-compose ps
echo echo   查看日志: docker-compose logs -f
echo echo   停止服务: docker-compose stop
echo echo   启动服务: docker-compose start
echo echo   重启服务: docker-compose restart
echo echo.
echo pause
) > "%RELEASE_DIR%\%PACKAGE_NAME%\install.bat" 2>nul

REM 创建Linux安装脚本
(
echo #!/bin/bash
echo set -e
echo.
echo echo "============================================================"
echo echo "MMS制造管理系统 - 离线安装脚本"
echo echo "============================================================"
echo echo ""
echo.
echo echo "[1/4] 检查Docker环境..."
echo if ! command -v docker ^&^> /dev/null; then
echo     echo "[错误] 未检测到Docker，请先安装Docker"
echo     exit 1
echo fi
echo echo "[✓] Docker环境检查通过"
echo echo ""
echo.
echo echo "[2/4] 加载Docker镜像..."
echo echo "[提示] 这可能需要几分钟时间，请耐心等待..."
echo echo ""
echo.
echo echo "  加载 mariadb:11 ..."
echo docker load -i docker-images/mariadb-11.tar
echo echo "  加载 redis:6.0-alpine ..."
echo docker load -i docker-images/redis-6.0-alpine.tar
echo echo "  加载 mms-backend:latest ..."
echo docker load -i docker-images/mms-backend-latest.tar
echo echo "  加载 mms-frontend:latest ..."
echo docker load -i docker-images/mms-frontend-latest.tar
echo echo "[✓] 所有镜像加载完成"
echo echo ""
echo.
echo echo "[3/4] 复制配置文件..."
echo cp -f project-files/docker-compose.yml .
echo echo "[✓] 配置文件复制完成"
echo echo ""
echo.
echo echo "[4/4] 启动服务..."
echo docker-compose up -d
echo echo "[✓] 服务启动成功"
echo echo ""
echo.
echo echo "============================================================"
echo echo "安装完成！"
echo echo "============================================================"
echo echo ""
echo echo "服务访问地址："
echo echo "  前端应用: http://localhost:9000"
echo echo "  后端API: http://localhost:8080/api"
echo echo "  数据库: localhost:3307"
echo echo "  Redis: localhost:6379"
echo echo ""
echo echo "常用命令："
echo echo "  查看服务状态: docker-compose ps"
echo echo "  查看日志: docker-compose logs -f"
echo echo "  停止服务: docker-compose stop"
echo echo "  启动服务: docker-compose start"
echo echo "  重启服务: docker-compose restart"
echo echo ""
) > "%RELEASE_DIR%\%PACKAGE_NAME%\install.sh"

echo [✓] 安装脚本创建完成

echo.
echo [8/9] 创建说明文档...
> "%RELEASE_DIR%\%PACKAGE_NAME%\README.md" (
echo # MMS Manufacturing Management System - Docker Offline Package
echo.
echo ## Package Contents
echo.
echo - docker-images/ - 4 Docker image files
echo - project-files/ - Configuration and initialization files
echo - install.bat - Windows installation script
echo - install.sh - Linux installation script
echo - README.md - This document
echo.
echo ## System Requirements
echo.
echo ### Hardware
echo - CPU: 2+ cores
echo - Memory: 4GB+ RAM
echo - Disk: 10GB available space
echo.
echo ### Software
echo - Docker 20.10+
echo - Docker Compose 2.0+
echo.
echo ## Installation
echo.
echo ### Windows
echo.
echo 1. Extract the package to any directory
echo 2. Run install.bat
echo 3. Wait 3-5 minutes for installation to complete
echo 4. Access the system at http://localhost:9000
echo.
echo ### Linux
echo.
echo 1. Extract: tar -xzf mms-offline-package.tar.gz
echo 2. Run: sudo ./install.sh
echo 3. Wait 3-5 minutes for installation to complete
echo 4. Access the system at http://localhost:9000
echo.
echo ## Default Credentials
echo.
echo ### Database
echo - Host: localhost:3307
echo - Database: mms_db
echo - Username: mms_user
echo - Password: mms_password
echo.
echo ### Redis
echo - Host: localhost:6379
echo - No password
echo.
echo ## Common Commands
echo.
echo - Check status: docker-compose ps
echo - View logs: docker-compose logs -f
echo - Stop services: docker-compose stop
echo - Start services: docker-compose start
echo - Restart services: docker-compose restart
echo - Remove all: docker-compose down
echo.
echo ## Access URLs
echo.
echo - Frontend: http://localhost:9000
echo - Backend API: http://localhost:8080/api
echo - Database: localhost:3307
echo - Redis: localhost:6379
echo.
echo ## Troubleshooting
echo.
echo ### Port Conflicts
echo Check if ports 9000, 8080, 3307, 6379 are available.
echo Modify port mappings in docker-compose.yml if needed.
echo.
echo ### Memory Issues
echo Increase Docker memory limit in Docker Desktop settings.
echo.
echo ### Image Load Failures
echo Ensure Docker is running and sufficient disk space is available.
echo.
echo ## Version Information
echo.
echo - Export Date: %EXPORT_DATE%
echo - System Version: v3.0
echo - Docker Compose: 2.0+
echo.
echo ## License
echo.
echo For internal use only. Unauthorized distribution prohibited.
)

echo [✓] 说明文档创建完成

echo.
echo [9/9] 打包离线安装包...
echo [提示] 正在压缩文件，这可能需要几分钟...

REM 检查是否有7-Zip
set SEVENZIP_CMD=
where 7z >nul 2>&1
if not errorlevel 1 (
    set SEVENZIP_CMD=7z
) else (
    REM Try full path if not in PATH
    if exist "%ProgramFiles%\7-Zip\7z.exe" (
        set "SEVENZIP_CMD=%ProgramFiles%\7-Zip\7z.exe"
    ) else if exist "%ProgramFiles(x86)%\7-Zip\7z.exe" (
        set "SEVENZIP_CMD=%ProgramFiles(x86)%\7-Zip\7z.exe"
    )
)

if defined SEVENZIP_CMD (
    echo [INFO] Found 7-Zip: %SEVENZIP_CMD%
    cd "%RELEASE_DIR%"
    
    REM Find 7z.sfx file
    set SFX_MODULE=
    if exist "%ProgramFiles%\7-Zip\7z.sfx" set "SFX_MODULE=%ProgramFiles%\7-Zip\7z.sfx"
    if exist "%ProgramFiles(x86)%\7-Zip\7z.sfx" set "SFX_MODULE=%ProgramFiles(x86)%\7-Zip\7z.sfx"
    
    if defined SFX_MODULE (
        echo [INFO] Creating self-extracting installer...
        
        REM Create SFX config
        (
            echo ;!@Install@!UTF-8!
            echo Title="MMS System Offline Installer"
            echo BeginPrompt="MMS Manufacturing Management System\n\nThis will:\n1. Extract Docker images and files\n2. Run installation script\n\nRequires Docker Desktop.\n\nClick OK to continue..."
            echo ExecuteFile="%PACKAGE_NAME%\install.bat"
            echo ExecuteParameters=""
            echo GUIMode="2"
            echo ;!@InstallEnd@!
        ) > "%PACKAGE_NAME%_config.txt"
        
        REM Create 7z archive
        "%SEVENZIP_CMD%" a -t7z "%PACKAGE_NAME%.7z" "%PACKAGE_NAME%" -mx=5
        
        REM Merge: 7z.sfx + config + archive = .exe
        copy /b "!SFX_MODULE!" + "%PACKAGE_NAME%_config.txt" + "%PACKAGE_NAME%.7z" "%PACKAGE_NAME%-installer.exe"
        
        REM Cleanup
        del "%PACKAGE_NAME%_config.txt"
        del "%PACKAGE_NAME%.7z"
        
        cd "%PROJECT_ROOT%\script"
        echo [DONE] Self-extracting installer created: %PACKAGE_NAME%-installer.exe
        echo [INFO] Double-click .exe to install
    ) else (
        REM Create traditional archive
        "%SEVENZIP_CMD%" a -ttar "%PACKAGE_NAME%.tar" "%PACKAGE_NAME%"
        "%SEVENZIP_CMD%" a -tgzip "%PACKAGE_NAME%.tar.gz" "%PACKAGE_NAME%.tar"
        del "%PACKAGE_NAME%.tar"
        cd "%PROJECT_ROOT%\script"
        echo [DONE] Offline package created: %PACKAGE_NAME%.tar.gz
        echo [INFO] 7z.sfx not found, cannot create self-extracting installer
    )
) else (
    echo [INFO] 7-Zip not detected, creating uncompressed package
    echo [DONE] Offline package directory: %PACKAGE_NAME%
    echo [INFO] Install 7-Zip to create self-extracting installer
)

echo.
echo ============================================================
echo Export Complete!
echo ============================================================
echo.
echo Package Location:
echo   Directory: %RELEASE_DIR%\%PACKAGE_NAME%
if exist "%RELEASE_DIR%\%PACKAGE_NAME%-installer.exe" (
    echo   Installer: %RELEASE_DIR%\%PACKAGE_NAME%-installer.exe
)
if exist "%RELEASE_DIR%\%PACKAGE_NAME%.tar.gz" (
    echo   Archive: %RELEASE_DIR%\%PACKAGE_NAME%.tar.gz
)
echo.
echo Package Contents:
echo   - Docker images ^(4 files^)
echo   - Configuration files
echo   - install.bat ^(Windows^)
echo   - install.sh ^(Linux^)
echo   - README.md
echo.
echo Usage:
if exist "%RELEASE_DIR%\%PACKAGE_NAME%-installer.exe" (
    echo   [Auto Install]
    echo   1. Copy %PACKAGE_NAME%-installer.exe to target server
    echo   2. Double-click to run
    echo.
    echo   [Manual Install]
)
echo   1. Copy package to target server
if exist "%RELEASE_DIR%\%PACKAGE_NAME%.tar.gz" (
    echo   2. Extract: tar -xzf %PACKAGE_NAME%.tar.gz
)
echo   3. Windows: Run install.bat
echo   4. Linux: Run sudo ./install.sh
echo.
pause

