@echo off
chcp 65001 >nul
echo 停止MMS制造管理系统开发环境...

echo [1/2] 停止基础服务...
docker-compose stop mariadb redis

echo.
echo [2/2] 停止开发服务器...
echo 正在停止前端和后端开发服务器...

REM 停止Java进程 (Spring Boot)
taskkill /f /im java.exe >nul 2>&1

REM 停止Node.js进程 (Vite)
taskkill /f /im node.exe >nul 2>&1

echo ✅ 开发环境已停止
echo.
echo 注意: 数据库和Redis容器仍在运行，数据已保存
echo 如需完全停止，请运行: docker-compose down
echo.
pause
