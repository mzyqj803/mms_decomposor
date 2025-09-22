@echo off
chcp 65001 >nul
echo MMS制造管理系统状态检查...
echo.

REM 检查Docker服务状态
echo [1/4] 检查Docker服务状态...
docker --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Docker 已安装
) else (
    echo ❌ Docker 未安装或未启动
    goto :end
)

REM 检查容器状态
echo.
echo [2/4] 检查容器状态...
docker-compose ps

REM 检查端口占用
echo.
echo [3/4] 检查端口占用...
echo 检查端口 3000 (前端):
netstat -an | findstr ":3000" >nul
if %errorlevel% equ 0 (
    echo ✅ 端口 3000 已被占用 (前端服务)
) else (
    echo ❌ 端口 3000 未被占用
)

echo 检查端口 8080 (后端):
netstat -an | findstr ":8080" >nul
if %errorlevel% equ 0 (
    echo ✅ 端口 8080 已被占用 (后端服务)
) else (
    echo ❌ 端口 8080 未被占用
)

echo 检查端口 3306 (数据库):
netstat -an | findstr ":3306" >nul
if %errorlevel% equ 0 (
    echo ✅ 端口 3306 已被占用 (数据库)
) else (
    echo ❌ 端口 3306 未被占用
)

echo 检查端口 6379 (Redis):
netstat -an | findstr ":6379" >nul
if %errorlevel% equ 0 (
    echo ✅ 端口 6379 已被占用 (Redis)
) else (
    echo ❌ 端口 6379 未被占用
)

REM 检查服务健康状态
echo.
echo [4/4] 检查服务健康状态...
echo 检查前端服务...
curl -s http://localhost:3000 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 前端服务运行正常
) else (
    echo ❌ 前端服务无响应
)

echo 检查后端服务...
curl -s http://localhost:8080/api/auth/me >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 后端服务运行正常
) else (
    echo ❌ 后端服务无响应
)

:end
echo.
echo 状态检查完成
echo.
echo 访问地址:
echo   前端应用: http://localhost:3000
echo   后端API: http://localhost:8080/api
echo.
pause
