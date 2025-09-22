@echo off
chcp 65001 >nul
echo 停止MMS制造管理系统...

REM 停止所有服务
docker-compose down

if %errorlevel% equ 0 (
    echo ✅ MMS制造管理系统已停止
    echo.
    echo 如需完全清理，请运行: docker-compose down -v
    echo 这将删除所有数据卷，请谨慎使用！
) else (
    echo ❌ 停止服务失败
    echo 请检查Docker服务是否正在运行
    pause
    exit /b 1
)

echo.
echo 按任意键退出...
pause >nul
