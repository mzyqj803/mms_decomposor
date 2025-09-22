@echo off
chcp 65001 >nul
echo 查看MMS制造管理系统日志...

echo 选择要查看的服务日志:
echo 1. 查看所有服务日志
echo 2. 查看后端服务日志
echo 3. 查看前端服务日志
echo 4. 查看数据库日志
echo 5. 查看Redis日志
echo 0. 退出
echo.

set /p choice=请输入选择 (0-5): 

if "%choice%"=="1" (
    echo 查看所有服务日志...
    docker-compose logs -f
) else if "%choice%"=="2" (
    echo 查看后端服务日志...
    docker-compose logs -f backend
) else if "%choice%"=="3" (
    echo 查看前端服务日志...
    docker-compose logs -f frontend
) else if "%choice%"=="4" (
    echo 查看数据库日志...
    docker-compose logs -f mariadb
) else if "%choice%"=="5" (
    echo 查看Redis日志...
    docker-compose logs -f redis
) else if "%choice%"=="0" (
    echo 退出
    exit /b 0
) else (
    echo 无效选择，请重新运行脚本
    pause
    exit /b 1
)

echo.
echo 按 Ctrl+C 停止查看日志
pause
