@echo off
chcp 65001 >nul
echo 启动MMS制造管理系统...

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Java未安装或未配置环境变量，请先安装JDK 11+
    pause
    exit /b 1
)

REM 检查Maven是否安装
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Maven未安装或未配置环境变量，请先安装Maven 3.6+
    pause
    exit /b 1
)

REM 检查Node.js是否安装
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Node.js未安装或未配置环境变量，请先安装Node.js 16+
    pause
    exit /b 1
)

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker未安装，请先安装Docker Desktop
    pause
    exit /b 1
)

REM 检查Docker Compose是否安装
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker Compose未安装，请先安装Docker Compose
    pause
    exit /b 1
)

echo 环境检查通过，开始构建和启动服务...

REM 构建后端应用
echo.
echo [1/4] 构建后端应用...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 错误: 后端应用构建失败
    pause
    exit /b 1
)

REM 安装前端依赖
echo.
echo [2/4] 安装前端依赖...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo 错误: 前端依赖安装失败
    pause
    exit /b 1
)

REM 构建前端应用
echo.
echo [3/4] 构建前端应用...
call npm run build
if %errorlevel% neq 0 (
    echo 错误: 前端应用构建失败
    pause
    exit /b 1
)
cd ..

REM 启动所有服务
echo.
echo [4/4] 启动所有服务...
docker-compose up -d

if %errorlevel% equ 0 (
    echo.
    echo ✅ MMS制造管理系统启动成功！
    echo.
    echo 访问地址:
    echo   前端应用: http://localhost:3000
    echo   后端API: http://localhost:8080/api
    echo.
    echo 数据库连接信息:
    echo   Host: localhost:3306
    echo   Database: mms_db
    echo   Username: mms_user
    echo   Password: mms_password
    echo.
    echo Redis连接信息:
    echo   Host: localhost:6379
    echo.
    echo 管理命令:
    echo   查看日志: docker-compose logs -f
    echo   停止服务: docker-compose down
    echo   重启服务: docker-compose restart
    echo.
    echo 按任意键打开浏览器访问应用...
    pause >nul
    start http://localhost:3000
) else (
    echo ❌ 服务启动失败，请检查日志
    echo 使用以下命令查看详细错误信息:
    echo docker-compose logs
    pause
    exit /b 1
)
