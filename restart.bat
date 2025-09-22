@echo off
chcp 65001 >nul
echo 重启MMS制造管理系统...

echo [1/2] 停止服务...
call stop.bat

echo.
echo [2/2] 启动服务...
call start.bat
