@echo off
chcp 65001 >nul
echo MMS制造管理系统启动 (调试模式)
echo ================================
echo.

REM 检查参数
if "%1"=="--help" (
    echo 用法: start-debug.bat [选项]
    echo.
    echo 选项:
    echo   --help     显示此帮助信息
    echo   --verbose  显示详细输出
    echo   --skip-build 跳过构建步骤
    echo.
    pause
    exit /b 0
)

set "verbose_mode=false"
set "skip_build=false"

if "%1"=="--verbose" set "verbose_mode=true"
if "%2"=="--verbose" set "verbose_mode=true"
if "%1"=="--skip-build" set "skip_build=true"
if "%2"=="--skip-build" set "skip_build=true"

echo 调试模式已启用
if "%verbose_mode%"=="true" echo 详细输出模式已启用
if "%skip_build%"=="true" echo 跳过构建模式已启用
echo.

REM 检查Java
echo [调试] 检查Java环境...
if "%verbose_mode%"=="true" (
    echo 执行命令: java -version
    java -version
    echo 错误代码: %errorlevel%
) else (
    java -version >nul 2>&1
)
if %errorlevel% neq 0 (
    echo ❌ Java检查失败 (错误代码: %errorlevel%)
    echo 详细错误信息:
    java -version 2>&1
    pause
    exit /b 1
) else (
    echo ✅ Java环境正常
)

echo.

REM 检查Maven
echo [调试] 检查Maven环境...
if "%verbose_mode%"=="true" (
    echo 执行命令: mvn -version
    mvn -version
    echo 错误代码: %errorlevel%
) else (
    mvn -version >nul 2>&1
)
if %errorlevel% neq 0 (
    echo ❌ Maven检查失败 (错误代码: %errorlevel%)
    echo 详细错误信息:
    mvn -version 2>&1
    echo.
    echo 可能的解决方案:
    echo 1. 检查Maven是否正确安装
    echo 2. 检查MAVEN_HOME环境变量
    echo 3. 检查PATH环境变量是否包含Maven bin目录
    echo 4. 重启命令提示符
    pause
    exit /b 1
) else (
    echo ✅ Maven环境正常
)

echo.

REM 检查Node.js
echo [调试] 检查Node.js环境...
if "%verbose_mode%"=="true" (
    echo 执行命令: node --version
    node --version
    echo 错误代码: %errorlevel%
) else (
    node --version >nul 2>&1
)
if %errorlevel% neq 0 (
    echo ❌ Node.js检查失败 (错误代码: %errorlevel%)
    echo 详细错误信息:
    node --version 2>&1
    pause
    exit /b 1
) else (
    echo ✅ Node.js环境正常
)

echo.

REM 检查Docker
echo [调试] 检查Docker环境...
if "%verbose_mode%"=="true" (
    echo 执行命令: docker --version
    docker --version
    echo 错误代码: %errorlevel%
) else (
    docker --version >nul 2>&1
)
if %errorlevel% neq 0 (
    echo ❌ Docker检查失败 (错误代码: %errorlevel%)
    echo 详细错误信息:
    docker --version 2>&1
    pause
    exit /b 1
) else (
    echo ✅ Docker环境正常
)

echo.

REM 检查Docker Compose
echo [调试] 检查Docker Compose环境...
if "%verbose_mode%"=="true" (
    echo 执行命令: docker-compose --version
    docker-compose --version
    echo 错误代码: %errorlevel%
) else (
    docker-compose --version >nul 2>&1
)
if %errorlevel% neq 0 (
    echo ❌ Docker Compose检查失败 (错误代码: %errorlevel%)
    echo 详细错误信息:
    docker-compose --version 2>&1
    pause
    exit /b 1
) else (
    echo ✅ Docker Compose环境正常
)

echo.
echo ✅ 环境检查通过，开始构建和启动服务...

if "%skip_build%"=="true" (
    echo [调试] 跳过构建步骤
    goto :start_services
)

REM 构建后端应用
echo.
echo [调试] [1/4] 构建后端应用...
if "%verbose_mode%"=="true" (
    echo 执行命令: mvn clean package -DskipTests
    call mvn clean package -DskipTests
) else (
    call mvn clean package -DskipTests
)
if %errorlevel% neq 0 (
    echo ❌ 后端构建失败 (错误代码: %errorlevel%)
    echo 请检查Maven配置和项目依赖
    pause
    exit /b 1
) else (
    echo ✅ 后端构建成功
)

REM 安装前端依赖
echo.
echo [调试] [2/4] 安装前端依赖...
cd frontend
if "%verbose_mode%"=="true" (
    echo 执行命令: npm install
    call npm install
) else (
    call npm install
)
if %errorlevel% neq 0 (
    echo ❌ 前端依赖安装失败 (错误代码: %errorlevel%)
    echo 请检查Node.js和npm配置
    cd ..
    pause
    exit /b 1
) else (
    echo ✅ 前端依赖安装成功
)

REM 构建前端应用
echo.
echo [调试] [3/4] 构建前端应用...
if "%verbose_mode%"=="true" (
    echo 执行命令: npm run build
    call npm run build
) else (
    call npm run build
)
if %errorlevel% neq 0 (
    echo ❌ 前端构建失败 (错误代码: %errorlevel%)
    echo 请检查前端代码和依赖
    cd ..
    pause
    exit /b 1
) else (
    echo ✅ 前端构建成功
)
cd ..

:start_services
REM 启动所有服务
echo.
echo [调试] [4/4] 启动所有服务...
if "%verbose_mode%"=="true" (
    echo 执行命令: docker-compose up -d
    docker-compose up -d
) else (
    docker-compose up -d
)
if %errorlevel% neq 0 (
    echo ❌ 服务启动失败 (错误代码: %errorlevel%)
    echo 请检查Docker服务状态和配置
    echo 尝试运行: docker-compose logs
    pause
    exit /b 1
) else (
    echo ✅ 服务启动成功
)

echo.
echo ✅ MMS制造管理系统启动成功！
echo.
echo 访问地址:
echo   前端应用: http://localhost:3000
echo   后端API: http://localhost:8080/api
echo.
echo 调试命令:
echo   查看日志: docker-compose logs -f
echo   检查状态: docker-compose ps
echo   停止服务: docker-compose down
echo.
pause
