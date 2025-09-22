@echo off
chcp 65001 >nul
echo MMS制造管理系统开发模式启动...
echo.

REM 检查环境
echo [1/5] 检查开发环境...

REM 检查Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java未安装，请安装JDK 11+
    pause
    exit /b 1
)
echo ✅ Java环境正常

REM 检查Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven未安装，请安装Maven 3.6+
    pause
    exit /b 1
)
echo ✅ Maven环境正常

REM 检查Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js未安装，请安装Node.js 16+
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

REM 检查Docker
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker未安装，请安装Docker Desktop
    pause
    exit /b 1
)
echo ✅ Docker环境正常

echo.
echo [2/5] 启动基础服务 (数据库和Redis)...
docker-compose up -d mariadb redis

echo 等待数据库启动...
timeout /t 10 /nobreak >nul

echo.
echo [3/5] 安装前端依赖...
cd frontend
if not exist node_modules (
    echo 安装前端依赖包...
    call npm install
    if %errorlevel% neq 0 (
        echo ❌ 前端依赖安装失败
        pause
        exit /b 1
    )
) else (
    echo ✅ 前端依赖已存在
)
cd ..

echo.
echo [4/5] 启动后端开发服务器...
echo 后端服务将在 http://localhost:8080 启动
echo 按 Ctrl+C 停止后端服务
start "MMS Backend" cmd /k "mvn spring-boot:run"

echo 等待后端服务启动...
timeout /t 15 /nobreak >nul

echo.
echo [5/5] 启动前端开发服务器...
echo 前端服务将在 http://localhost:3000 启动
echo 按 Ctrl+C 停止前端服务
cd frontend
start "MMS Frontend" cmd /k "npm run dev"
cd ..

echo.
echo ✅ 开发环境启动完成！
echo.
echo 服务地址:
echo   前端开发服务器: http://localhost:3000
echo   后端API服务: http://localhost:8080/api
echo   数据库: localhost:3306
echo   Redis: localhost:6379
echo.
echo 开发提示:
echo   - 前端支持热重载，修改代码后自动刷新
echo   - 后端支持热重载，修改代码后自动重启
echo   - 数据库和Redis数据持久化保存
echo.
echo 停止开发环境:
echo   关闭对应的命令行窗口即可停止服务
echo   或运行 stop-dev.bat 脚本
echo.
pause
