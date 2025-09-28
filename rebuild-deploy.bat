@echo off
chcp 65001 >nul
echo ========================================
echo MMS制造管理系统 - 重新编译部署
echo ========================================
echo.

REM 检查环境
echo [1/6] 检查环境依赖...
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
echo [2/6] 停止现有服务...
docker-compose down
if %errorlevel% neq 0 (
    echo ⚠️ 停止服务时出现警告，继续执行...
)

echo.
echo [3/6] 清理旧的镜像和容器...
docker system prune -f
docker image prune -f

echo.
echo [4/6] 重新构建后端应用...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ 后端应用构建失败
    pause
    exit /b 1
)
echo ✅ 后端应用构建成功

echo.
echo [5/6] 重新构建前端应用...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo ❌ 前端依赖安装失败
    pause
    exit /b 1
)

call npm run build
if %errorlevel% neq 0 (
    echo ❌ 前端应用构建失败
    pause
    exit /b 1
)
echo ✅ 前端应用构建成功
cd ..

echo.
echo [6/6] 重新构建并启动Docker镜像...
docker-compose build --no-cache
if %errorlevel% neq 0 (
    echo ❌ Docker镜像构建失败
    pause
    exit /b 1
)

docker-compose up -d
if %errorlevel% neq 0 (
    echo ❌ 服务启动失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ 重新编译部署完成！
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
