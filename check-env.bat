@echo off
chcp 65001 >nul
echo MMS制造管理系统环境检查工具
echo ================================
echo.

set "all_ok=true"

REM 检查Java
echo [1/5] 检查Java环境...
call java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java未安装或未配置环境变量
    echo    当前PATH: %PATH%
    echo    建议: 安装JDK 11+并配置JAVA_HOME环境变量
    set "all_ok=false"
) else (
    echo ✅ Java环境正常
    call java -version 2>&1 | findstr "version"
)

echo.

REM 检查Maven
echo [2/5] 检查Maven环境...
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven未安装或未配置环境变量
    echo    当前PATH: %PATH%
    echo    建议: 安装Maven 3.6+并配置MAVEN_HOME环境变量
    set "all_ok=false"
) else (
    echo ✅ Maven环境正常
    call mvn -version 2>&1 | findstr "Apache Maven"
)

echo.

REM 检查Node.js
echo [3/5] 检查Node.js环境...
call node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js未安装或未配置环境变量
    echo    当前PATH: %PATH%
    echo    建议: 安装Node.js 16+ LTS版本
    set "all_ok=false"
) else (
    echo ✅ Node.js环境正常
    echo    版本: 
    call node --version
)

echo.

REM 检查npm
echo [4/5] 检查npm环境...
call npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ npm未安装或未配置环境变量
    echo    建议: npm通常随Node.js一起安装
    set "all_ok=false"
) else (
    echo ✅ npm环境正常
    echo    版本: 
    call npm --version
)

echo.

REM 检查Docker
echo [5/5] 检查Docker环境...
call docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker未安装或未启动
    echo    建议: 安装Docker Desktop并启动服务
    set "all_ok=false"
) else (
    echo ✅ Docker环境正常
    call docker --version
)

echo.

REM 检查Docker Compose
echo [6/6] 检查Docker Compose环境...
call docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose未安装
    echo    建议: Docker Desktop通常包含Docker Compose
    set "all_ok=false"
) else (
    echo ✅ Docker Compose环境正常
    call docker-compose --version
)

echo.
echo ================================

if "%all_ok%"=="true" (
    echo ✅ 所有环境检查通过！可以运行 start.bat 启动系统
) else (
    echo ❌ 环境检查发现问题，请根据上述建议进行修复
    echo.
    echo 常见解决方案:
    echo 1. 重启命令提示符或重新登录
    echo 2. 检查环境变量配置
    echo 3. 确保软件已正确安装
    echo 4. 以管理员身份运行命令提示符
)

echo.
echo 按任意键退出...
pause >nul
