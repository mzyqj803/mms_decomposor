@echo off
chcp 65001 >nul
echo ========================================
echo MMS制造管理系统 - 完整重新构建部署
echo ========================================
echo.

REM 设置错误处理
setlocal enabledelayedexpansion

REM 检查环境依赖
echo [1/7] 检查环境依赖...
call java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java未安装，请安装JDK 21+
    pause
    exit /b 1
)
echo ✅ Java环境正常

call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven未安装，请安装Maven 3.6+
    pause
    exit /b 1
)
echo ✅ Maven环境正常

call node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js未安装，请安装Node.js 16+
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

call docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker未安装，请安装Docker Desktop
    pause
    exit /b 1
)
echo ✅ Docker环境正常

call docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose未安装
    pause
    exit /b 1
)
echo ✅ Docker Compose环境正常

echo.
echo [2/7] 停止并删除前后端容器和镜像...
echo 停止所有服务...
docker-compose down
if %errorlevel% neq 0 (
    echo ⚠️ 停止服务时出现警告，继续执行...
)

echo 删除前后端镜像...
docker rmi mms-backend:latest mms-frontend:latest 2>nul
if %errorlevel% neq 0 (
    echo ⚠️ 删除镜像时出现警告，可能镜像不存在，继续执行...
)

echo 清理Docker系统...
docker system prune -f
docker image prune -f

echo ✅ 容器和镜像清理完成

echo.
echo [3/7] 清理前端编译生成的文件...
cd frontend

REM 删除dist目录
if exist dist (
    echo 删除dist目录...
    rmdir /s /q dist
    echo ✅ dist目录已删除
) else (
    echo ✅ dist目录不存在，无需删除
)

REM 保留node_modules目录，不删除
echo ✅ 保留node_modules目录，使用现有依赖

echo.
echo [4/7] 执行前端npm构建...
echo 安装前端依赖...
call npm install
if %errorlevel% neq 0 (
    echo ❌ 前端依赖安装失败
    cd ..
    pause
    exit /b 1
)
echo ✅ 前端依赖安装成功

echo 构建前端应用...
call npm run build
if %errorlevel% neq 0 (
    echo ❌ 前端应用构建失败
    cd ..
    pause
    exit /b 1
)
echo ✅ 前端应用构建成功

cd ..

echo.
echo [5/7] 执行Maven clean package构建（跳过测试）...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ 后端应用构建失败
    pause
    exit /b 1
)
echo ✅ 后端应用构建成功

echo.
echo [6/7] 重新构建前后端Docker镜像...
echo 构建后端镜像...
docker build -f Dockerfile.backend -t mms-backend:latest .
if %errorlevel% neq 0 (
    echo ❌ 后端镜像构建失败
    pause
    exit /b 1
)
echo ✅ 后端镜像构建成功

echo 构建前端镜像...
docker build -f frontend/Dockerfile -t mms-frontend:latest frontend/
if %errorlevel% neq 0 (
    echo ❌ 前端镜像构建失败
    pause
    exit /b 1
)
echo ✅ 前端镜像构建成功

echo.
echo [7/7] 部署最新的前后端镜像...
echo 启动所有服务...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ❌ 服务启动失败
    pause
    exit /b 1
)

echo 等待服务启动...
timeout /t 10 /nobreak >nul

echo 检查服务状态...
docker-compose ps

echo.
echo ========================================
echo ✅ 完整重新构建部署完成！
echo ========================================
echo.
echo 服务地址:
echo   前端应用: http://localhost:9000
echo   后端API: http://localhost:8080/api
echo   数据库: localhost:3307
echo   Redis: localhost:6379
echo.
echo 管理命令:
echo   查看日志: docker-compose logs -f
echo   停止服务: docker-compose down
echo   重启服务: docker-compose restart
echo   查看状态: docker-compose ps
echo.
echo 按任意键打开浏览器访问应用...
pause >nul
start http://localhost:9000
