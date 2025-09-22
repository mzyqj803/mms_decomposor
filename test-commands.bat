@echo off
chcp 65001 >nul
echo MMS制造管理系统命令测试工具
echo ================================
echo.

echo 测试各种命令的执行情况...
echo.

REM 测试Java命令
echo [1/6] 测试Java命令...
echo 执行: java -version
call java -version 2>&1
echo 错误代码: %errorlevel%
echo.

REM 测试Maven命令
echo [2/6] 测试Maven命令...
echo 执行: mvn -version
call mvn -version 2>&1
echo 错误代码: %errorlevel%
echo.

REM 测试Node.js命令
echo [3/6] 测试Node.js命令...
echo 执行: node --version
call node --version 2>&1
echo 错误代码: %errorlevel%
echo.

REM 测试npm命令
echo [4/6] 测试npm命令...
echo 执行: npm --version
call npm --version 2>&1
echo 错误代码: %errorlevel%
echo.

REM 测试Docker命令
echo [5/6] 测试Docker命令...
echo 执行: docker --version
call docker --version 2>&1
echo 错误代码: %errorlevel%
echo.

REM 测试Docker Compose命令
echo [6/6] 测试Docker Compose命令...
echo 执行: docker-compose --version
call docker-compose --version 2>&1
echo 错误代码: %errorlevel%
echo.

echo ================================
echo 测试完成！
echo.
echo 如果某个命令的错误代码不是0，说明该命令执行失败
echo 如果某个命令没有输出，说明该命令不存在或未配置环境变量
echo.
pause
