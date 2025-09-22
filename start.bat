@echo off
chcp 65001 >nul
echo 启动MMS制造管理系统...

REM 检查Java是否安装
echo 检查Java环境...
call java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java未安装或未配置环境变量，请先安装JDK 21+
    echo.
    echo 解决方案:
    echo 1. 下载JDK: https://adoptium.net/
    echo 2. 安装JDK 21或更高版本
    echo 3. 配置JAVA_HOME环境变量
    echo 4. 将%%JAVA_HOME%%\bin添加到PATH环境变量
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Java环境正常
)

REM 检查Maven是否安装
echo 检查Maven环境...
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven未安装或未配置环境变量，请先安装Maven 3.6+
    echo.
    echo 解决方案:
    echo 1. 下载Maven: https://maven.apache.org/download.cgi
    echo 2. 解压到指定目录
    echo 3. 配置MAVEN_HOME环境变量
    echo 4. 将%%MAVEN_HOME%%\bin添加到PATH环境变量
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Maven环境正常
)


REM 检查Node.js是否安装
echo 检查Node.js环境...
call node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js未安装或未配置环境变量，请先安装Node.js 16+
    echo.
    echo 解决方案:
    echo 1. 下载Node.js: https://nodejs.org/
    echo 2. 选择LTS版本安装
    echo 3. 安装时自动配置PATH环境变量
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Node.js环境正常
)

REM 检查Docker是否安装
echo 检查Docker环境...
call docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker未安装，请先安装Docker Desktop
    echo.
    echo 解决方案:
    echo 1. 下载Docker Desktop: https://www.docker.com/products/docker-desktop
    echo 2. 安装Docker Desktop
    echo 3. 启动Docker Desktop服务
    echo 4. 确保Docker服务正在运行
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Docker环境正常
)

REM 检查Docker Compose是否安装
echo 检查Docker Compose环境...
call docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose未安装，请先安装Docker Compose
    echo.
    echo 解决方案:
    echo 1. Docker Desktop通常包含Docker Compose
    echo 2. 确保Docker Desktop已正确安装
    echo 3. 重启Docker Desktop服务
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Docker Compose环境正常
)

echo ✅ 环境检查通过，开始构建和启动服务...
echo.

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
