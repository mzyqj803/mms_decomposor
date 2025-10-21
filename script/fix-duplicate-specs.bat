@echo off
chcp 65001 >nul
echo ========================================
echo 修复同一零部件下重复的规格参数问题
echo ========================================
echo.

REM 切换到项目根目录
cd /d "%~dp0.."

REM 检查Docker容器是否运行
echo [1/4] 检查数据库容器状态...
docker ps | findstr mms-mariadb >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ MariaDB容器未运行，请先启动服务
    echo 运行: docker-compose up -d mariadb
    pause
    exit /b 1
)
echo ✅ MariaDB容器运行正常

echo.
echo [2/4] 备份当前数据库...
set BACKUP_FILE=backup_before_fix_specs_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.sql
set BACKUP_FILE=%BACKUP_FILE: =0%
echo 备份文件: %BACKUP_FILE%
docker exec mms-mariadb mysqldump -u mms_user -pmms_password mms_db > %BACKUP_FILE%
if %errorlevel% neq 0 (
    echo ❌ 数据库备份失败
    pause
    exit /b 1
)
echo ✅ 数据库备份成功

echo.
echo [3/4] 执行修复脚本...
echo 这将删除同一零部件下重复的规格参数记录...
echo 保留每个(Component_ID, Spec_Code)组合的第一条记录
echo.
choice /C YN /M "确认执行修复脚本吗？"
if errorlevel 2 (
    echo 用户取消操作
    pause
    exit /b 0
)

docker exec -i mms-mariadb mysql -u mms_user -pmms_password mms_db < src\main\resources\sql\data_init\fix_duplicate_component_specs.sql
if %errorlevel% neq 0 (
    echo ❌ 修复脚本执行失败
    echo 可以使用备份恢复: docker exec -i mms-mariadb mysql -u mms_user -pmms_password mms_db ^< %BACKUP_FILE%
    pause
    exit /b 1
)
echo ✅ 修复脚本执行成功

echo.
echo [4/4] 清空Redis缓存...
docker exec mms-redis redis-cli FLUSHALL >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️ 清空Redis缓存失败，请手动执行
) else (
    echo ✅ Redis缓存已清空
)

echo.
echo ========================================
echo 修复完成！
echo ========================================
echo.
echo 备份文件: %BACKUP_FILE%
echo.
echo 建议操作:
echo 1. 重启后端服务: docker-compose restart backend
echo 2. 刷新前端页面验证修复结果
echo.
echo 验证命令:
echo docker exec mms-mariadb mysql -u mms_user -pmms_password mms_db -e "SELECT Component_ID, Spec_Code, COUNT(*) FROM components_spec GROUP BY Component_ID, Spec_Code HAVING COUNT(*) ^> 1;"
echo.
pause

