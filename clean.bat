@echo off
chcp 65001 >nul
echo MMS制造管理系统清理工具...
echo.

echo 警告: 此操作将删除所有容器、镜像和数据卷！
echo 请确保您已经备份了重要数据。
echo.

set /p confirm=确定要继续吗？(y/N): 

if /i not "%confirm%"=="y" (
    echo 操作已取消
    pause
    exit /b 0
)

echo.
echo 选择清理级别:
echo 1. 轻度清理 - 仅停止和删除容器
echo 2. 中度清理 - 停止容器并删除镜像
echo 3. 重度清理 - 删除所有容器、镜像和数据卷
echo 0. 取消
echo.

set /p level=请输入选择 (0-3): 

if "%level%"=="1" (
    echo 执行轻度清理...
    docker-compose down
    echo ✅ 轻度清理完成
) else if "%level%"=="2" (
    echo 执行中度清理...
    docker-compose down
    docker-compose down --rmi all
    echo ✅ 中度清理完成
) else if "%level%"=="3" (
    echo 执行重度清理...
    docker-compose down -v --rmi all
    docker system prune -f
    echo ✅ 重度清理完成
    echo 注意: 所有数据已被删除，下次启动将重新初始化数据库
) else if "%level%"=="0" (
    echo 操作已取消
    pause
    exit /b 0
) else (
    echo 无效选择
    pause
    exit /b 1
)

echo.
echo 清理完成！
echo.
pause
