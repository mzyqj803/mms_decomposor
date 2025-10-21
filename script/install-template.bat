@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 切换到脚本所在目录
cd /d "%~dp0"

echo ============================================================
echo MMS制造管理系统 - INSTALL_TYPE_PLACEHOLDER
echo ============================================================
echo.

echo [1/4] 检查Docker环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Docker，请先安装Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker环境检查通过
echo.

echo [2/4] 加载Docker镜像...
echo [提示] 这可能需要几分钟时间，请耐心等待...
echo.
echo   加载 mariadb:11 ...
docker load -i docker-images\mariadb-11.tar
echo   加载 redis:6.0-alpine ...
docker load -i docker-images\redis-6.0-alpine.tar
echo   加载 mms-backend:latest ...
docker load -i docker-images\mms-backend-latest.tar
echo   加载 mms-frontend:latest ...
docker load -i docker-images\mms-frontend-latest.tar
echo [✓] 所有镜像加载完成
echo.

echo [3/4] 复制配置文件...
copy /Y project-files\docker-compose.yml .
echo [提示] 创建数据库初始化目录...
if not exist src\main\resources\sql\data_init mkdir src\main\resources\sql\data_init
xcopy /E /I /Y project-files\data_init src\main\resources\sql\data_init >nul
echo [✓] 配置文件复制完成
echo.

echo [4/4] 启动服务...
CLEAR_VOLUMES_PLACEHOLDER
echo [提示] 正在启动服务...
docker-compose up -d
if errorlevel 1 (
    echo [错误] 服务启动失败
    pause
    exit /b 1
)
echo [✓] 容器启动成功
echo.

echo [提示] 等待数据库就绪并初始化...
echo 这可能需要2-3分钟，请耐心等待...
echo.
:wait_db
docker exec mms-mariadb mariadb -uroot -ppassword -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo 等待中...
    timeout /t 10 /nobreak >nul
    goto :wait_db
)
echo [✓] 数据库已就绪
echo.

echo [提示] 等待后端服务启动...
echo 正在等待后端服务完成启动（约30秒）...
timeout /t 30 /nobreak >nul
echo [✓] 服务启动成功
echo.

echo.
echo ============================================================
echo 安装完成！
echo ============================================================
echo.
echo 服务访问地址：
echo   Frontend: http://localhost:9000
echo   Backend: http://localhost:8080/api  
echo   Database: localhost:3307
echo   Redis: localhost:6379
echo.
echo 常用命令：
echo   查看日志: docker-compose logs -f
echo   停止服务: docker-compose stop
echo   启动服务: docker-compose start
echo   重启服务: docker-compose restart
echo.
UPGRADE_NOTE_PLACEHOLDER
pause

