@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM MMS Docker 离线安装包导出脚本
REM ============================================================

echo.
echo ============================================================
echo MMS制造管理系统 - Docker离线安装包导出工具
echo ============================================================
echo.

REM 设置导出目录
REM 使用 PowerShell 获取日期，避免中文字符
for /f %%i in ('powershell -Command "Get-Date -Format 'yyyyMMdd'"') do set EXPORT_DATE=%%i
set PROJECT_ROOT=%~dp0..
set RELEASE_DIR=%PROJECT_ROOT%\release
set PACKAGE_NAME=mms-offline-package-%EXPORT_DATE%

echo [1/9] 检查Docker环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Docker，请先安装Docker Desktop
    pause
    exit /b 1
)
echo [✓] Docker环境检查通过

echo.
echo [2/9] 创建release目录...
if not exist "%RELEASE_DIR%" (
    mkdir "%RELEASE_DIR%"
    echo [✓] release目录创建完成
) else (
    echo [✓] release目录已存在
)

echo.
echo [3/9] 创建导出目录...
if exist "%RELEASE_DIR%\%PACKAGE_NAME%" (
    echo [提示] 目录已存在，正在清理...
    rd /s /q "%RELEASE_DIR%\%PACKAGE_NAME%"
)
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%"
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images"
mkdir "%RELEASE_DIR%\%PACKAGE_NAME%\project-files"
echo [✓] 目录创建完成: %RELEASE_DIR%\%PACKAGE_NAME%

echo.
echo [4/9] 构建最新的项目镜像...
cd "%PROJECT_ROOT%"
echo [提示] 正在构建后端镜像...
docker-compose build backend
if errorlevel 1 (
    echo [错误] 后端镜像构建失败
    cd script
    pause
    exit /b 1
)

echo [提示] 正在构建前端镜像...
docker-compose build frontend
if errorlevel 1 (
    echo [错误] 前端镜像构建失败
    cd script
    pause
    exit /b 1
)
echo [✓] 项目镜像构建完成
cd script

echo.
echo [5/9] 导出Docker镜像...
echo [提示] 这可能需要几分钟时间，请耐心等待...

echo   导出 mariadb:11 ...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mariadb-11.tar" mariadb:11
if errorlevel 1 (
    echo [错误] MariaDB镜像导出失败
    pause
    exit /b 1
)

echo   导出 redis:6.0-alpine ...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\redis-6.0-alpine.tar" redis:6.0-alpine
if errorlevel 1 (
    echo [错误] Redis镜像导出失败
    pause
    exit /b 1
)

echo   导出 mms-backend:latest ...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mms-backend-latest.tar" mms-backend:latest
if errorlevel 1 (
    echo [错误] 后端镜像导出失败
    pause
    exit /b 1
)

echo   导出 mms-frontend:latest ...
docker save -o "%RELEASE_DIR%\%PACKAGE_NAME%\docker-images\mms-frontend-latest.tar" mms-frontend:latest
if errorlevel 1 (
    echo [错误] 前端镜像导出失败
    pause
    exit /b 1
)
echo [✓] 所有镜像导出完成

echo.
echo [6/9] 复制项目文件...
cd "%PROJECT_ROOT%"
xcopy /E /I /Y "docker-compose.yml" "%RELEASE_DIR%\%PACKAGE_NAME%\project-files\" >nul
xcopy /E /I /Y "src\main\resources\sql\data_init" "%RELEASE_DIR%\%PACKAGE_NAME%\project-files\data_init\" >nul
xcopy /E /I /Y "docs\*.md" "%RELEASE_DIR%\%PACKAGE_NAME%\project-files\docs\" >nul 2>nul
cd script
echo [✓] 项目文件复制完成

echo.
echo [7/9] 创建离线安装脚本...

REM 创建Windows安装脚本
(
echo @echo off
echo chcp 65001 ^>nul
echo setlocal enabledelayedexpansion
echo.
echo echo ============================================================
echo echo MMS制造管理系统 - 离线安装脚本
echo echo ============================================================
echo echo.
echo.
echo echo [1/4] 检查Docker环境...
echo docker --version ^>nul 2^>^&1
echo if errorlevel 1 ^(
echo     echo [错误] 未检测到Docker，请先安装Docker Desktop
echo     pause
echo     exit /b 1
echo ^)
echo echo [✓] Docker环境检查通过
echo.
echo echo.
echo echo [2/4] 加载Docker镜像...
echo echo [提示] 这可能需要几分钟时间，请耐心等待...
echo.
echo echo   加载 mariadb:11 ...
echo docker load -i docker-images\mariadb-11.tar
echo echo   加载 redis:6.0-alpine ...
echo docker load -i docker-images\redis-6.0-alpine.tar
echo echo   加载 mms-backend:latest ...
echo docker load -i docker-images\mms-backend-latest.tar
echo echo   加载 mms-frontend:latest ...
echo docker load -i docker-images\mms-frontend-latest.tar
echo echo [✓] 所有镜像加载完成
echo.
echo echo.
echo echo [3/4] 复制配置文件...
echo copy /Y project-files\docker-compose.yml .
echo echo [✓] 配置文件复制完成
echo.
echo echo.
echo echo [4/4] 启动服务...
echo docker-compose up -d
echo if errorlevel 1 ^(
echo     echo [错误] 服务启动失败
echo     pause
echo     exit /b 1
echo ^)
echo echo [✓] 服务启动成功
echo.
echo echo.
echo echo ============================================================
echo echo 安装完成！
echo echo ============================================================
echo echo.
echo echo 服务访问地址：
echo echo   前端应用: http://localhost:9000
echo echo   后端API: http://localhost:8080/api
echo echo   数据库: localhost:3307
echo echo   Redis: localhost:6379
echo echo.
echo echo 常用命令：
echo echo   查看服务状态: docker-compose ps
echo echo   查看日志: docker-compose logs -f
echo echo   停止服务: docker-compose stop
echo echo   启动服务: docker-compose start
echo echo   重启服务: docker-compose restart
echo echo.
echo pause
) > "%RELEASE_DIR%\%PACKAGE_NAME%\install.bat"

REM 创建Linux安装脚本
(
echo #!/bin/bash
echo set -e
echo.
echo echo "============================================================"
echo echo "MMS制造管理系统 - 离线安装脚本"
echo echo "============================================================"
echo echo ""
echo.
echo echo "[1/4] 检查Docker环境..."
echo if ! command -v docker ^&^> /dev/null; then
echo     echo "[错误] 未检测到Docker，请先安装Docker"
echo     exit 1
echo fi
echo echo "[✓] Docker环境检查通过"
echo echo ""
echo.
echo echo "[2/4] 加载Docker镜像..."
echo echo "[提示] 这可能需要几分钟时间，请耐心等待..."
echo echo ""
echo.
echo echo "  加载 mariadb:11 ..."
echo docker load -i docker-images/mariadb-11.tar
echo echo "  加载 redis:6.0-alpine ..."
echo docker load -i docker-images/redis-6.0-alpine.tar
echo echo "  加载 mms-backend:latest ..."
echo docker load -i docker-images/mms-backend-latest.tar
echo echo "  加载 mms-frontend:latest ..."
echo docker load -i docker-images/mms-frontend-latest.tar
echo echo "[✓] 所有镜像加载完成"
echo echo ""
echo.
echo echo "[3/4] 复制配置文件..."
echo cp -f project-files/docker-compose.yml .
echo echo "[✓] 配置文件复制完成"
echo echo ""
echo.
echo echo "[4/4] 启动服务..."
echo docker-compose up -d
echo echo "[✓] 服务启动成功"
echo echo ""
echo.
echo echo "============================================================"
echo echo "安装完成！"
echo echo "============================================================"
echo echo ""
echo echo "服务访问地址："
echo echo "  前端应用: http://localhost:9000"
echo echo "  后端API: http://localhost:8080/api"
echo echo "  数据库: localhost:3307"
echo echo "  Redis: localhost:6379"
echo echo ""
echo echo "常用命令："
echo echo "  查看服务状态: docker-compose ps"
echo echo "  查看日志: docker-compose logs -f"
echo echo "  停止服务: docker-compose stop"
echo echo "  启动服务: docker-compose start"
echo echo "  重启服务: docker-compose restart"
echo echo ""
) > "%RELEASE_DIR%\%PACKAGE_NAME%\install.sh"

echo [✓] 安装脚本创建完成

echo.
echo [8/9] 创建说明文档...
(
echo # MMS制造管理系统 - Docker离线安装包
echo.
echo ## 包内容
echo.
echo - `docker-images/` - Docker镜像文件
echo   - `mariadb-11.tar` - MariaDB 11 数据库镜像
echo   - `redis-6.0-alpine.tar` - Redis 6.0 缓存镜像
echo   - `mms-backend-latest.tar` - MMS后端服务镜像
echo   - `mms-frontend-latest.tar` - MMS前端服务镜像
echo - `project-files/` - 项目配置文件
echo   - `docker-compose.yml` - Docker Compose配置
echo   - `data_init/` - 数据库初始化脚本
echo - `install.bat` - Windows安装脚本
echo - `install.sh` - Linux安装脚本
echo - `README.md` - 本说明文档
echo.
echo ## 系统要求
echo.
echo ### 硬件要求
echo - CPU: 2核及以上
echo - 内存: 4GB及以上
echo - 磁盘: 10GB可用空间
echo.
echo ### 软件要求
echo - Docker 20.10+
echo - Docker Compose 2.0+
echo.
echo ### Windows系统
echo - Windows 10/11 Pro、Enterprise 或 Education（支持Hyper-V）
echo - 或 Windows 10/11 Home with WSL2
echo - Docker Desktop for Windows
echo.
echo ### Linux系统
echo - Ubuntu 20.04+、CentOS 7+、Debian 10+ 或其他主流Linux发行版
echo - Docker Engine 20.10+
echo - Docker Compose 2.0+
echo.
echo ## 安装步骤
echo.
echo ### Windows系统
echo.
echo 1. **解压安装包**
echo    ```
echo    解压到任意目录，例如：C:\mms-system
echo    ```
echo.
echo 2. **运行安装脚本**
echo    ```
echo    双击运行 install.bat
echo    或在命令行执行：
echo    install.bat
echo    ```
echo.
echo 3. **等待安装完成**
echo    - 脚本会自动加载所有Docker镜像
echo    - 自动启动所有服务
echo    - 通常需要3-5分钟
echo.
echo 4. **访问系统**
echo    ```
echo    前端应用: http://localhost:9000
echo    后端API: http://localhost:8080/api
echo    ```
echo.
echo ### Linux系统
echo.
echo 1. **解压安装包**
echo    ```bash
echo    tar -xzf mms-offline-package.tar.gz
echo    cd mms-offline-package
echo    ```
echo.
echo 2. **添加执行权限**
echo    ```bash
echo    chmod +x install.sh
echo    ```
echo.
echo 3. **运行安装脚本**
echo    ```bash
echo    sudo ./install.sh
echo    ```
echo.
echo 4. **等待安装完成**
echo    - 脚本会自动加载所有Docker镜像
echo    - 自动启动所有服务
echo    - 通常需要3-5分钟
echo.
echo 5. **访问系统**
echo    ```
echo    前端应用: http://localhost:9000
echo    后端API: http://localhost:8080/api
echo    ```
echo.
echo ## 常用命令
echo.
echo ### 查看服务状态
echo ```bash
echo docker-compose ps
echo ```
echo.
echo ### 查看日志
echo ```bash
echo # 查看所有服务日志
echo docker-compose logs -f
echo.
echo # 查看特定服务日志
echo docker-compose logs -f backend
echo docker-compose logs -f frontend
echo docker-compose logs -f mariadb
echo docker-compose logs -f redis
echo ```
echo.
echo ### 停止服务
echo ```bash
echo docker-compose stop
echo ```
echo.
echo ### 启动服务
echo ```bash
echo docker-compose start
echo ```
echo.
echo ### 重启服务
echo ```bash
echo docker-compose restart
echo ```
echo.
echo ### 停止并删除所有容器
echo ```bash
echo docker-compose down
echo ```
echo.
echo ### 完全清理（包括数据卷）
echo ```bash
echo docker-compose down -v
echo ```
echo.
echo ## 默认账号
echo.
echo ### 数据库
echo - 主机: localhost:3307
echo - 数据库: mms_db
echo - 用户名: mms_user
echo - 密码: mms_password
echo - Root密码: password
echo.
echo ### Redis
echo - 主机: localhost:6379
echo - 密码: 无
echo.
echo ## 故障排查
echo.
echo ### 端口冲突
echo 如果遇到端口被占用的错误，请检查以下端口是否被其他程序占用：
echo - 9000 ^(前端^)
echo - 8080 ^(后端^)
echo - 3307 ^(数据库^)
echo - 6379 ^(Redis^)
echo.
echo 可以修改 `docker-compose.yml` 中的端口映射。
echo.
echo ### 内存不足
echo 如果系统内存不足，可以尝试：
echo 1. 关闭其他不必要的程序
echo 2. 增加Docker的内存限制（Docker Desktop设置）
echo.
echo ### 镜像加载失败
echo 如果镜像加载失败，请检查：
echo 1. Docker服务是否正常运行
echo 2. 磁盘空间是否充足
echo 3. 镜像文件是否完整（重新导出）
echo.
echo ## 技术支持
echo.
echo 如遇到问题，请检查：
echo 1. Docker版本是否符合要求
echo 2. 系统资源是否充足
echo 3. 防火墙或安全软件是否阻止了Docker
echo 4. 查看Docker日志获取详细错误信息
echo.
echo ## 版本信息
echo.
echo - 导出日期: %EXPORT_DATE%
echo - 系统版本: v3.0
echo - Docker Compose版本: 2.0+
echo.
echo ## 许可证
echo.
echo 本软件仅供内部使用，禁止未经授权的复制和分发。
) > "%RELEASE_DIR%\%PACKAGE_NAME%\README.md"

echo [✓] 说明文档创建完成

echo.
echo [9/9] 打包离线安装包...
echo [提示] 正在压缩文件，这可能需要几分钟...

REM 检查是否有7-Zip
where 7z >nul 2>&1
if not errorlevel 1 (
    cd "%RELEASE_DIR%"
    7z a -ttar "%PACKAGE_NAME%.tar" "%PACKAGE_NAME%"
    7z a -tgzip "%PACKAGE_NAME%.tar.gz" "%PACKAGE_NAME%.tar"
    del "%PACKAGE_NAME%.tar"
    cd "%PROJECT_ROOT%\script"
    echo [✓] 离线安装包创建完成: %PACKAGE_NAME%.tar.gz
) else (
    echo [提示] 未检测到7-Zip，将创建未压缩的安装包
    echo [✓] 离线安装包目录: %PACKAGE_NAME%
    echo [提示] 建议安装7-Zip以创建压缩包，或手动压缩该目录
)

echo.
echo ============================================================
echo 导出完成！
echo ============================================================
echo.
echo 安装包位置：
echo   目录: %RELEASE_DIR%\%PACKAGE_NAME%
if exist "%RELEASE_DIR%\%PACKAGE_NAME%.tar.gz" (
    echo   压缩包: %RELEASE_DIR%\%PACKAGE_NAME%.tar.gz
)
echo.
echo 安装包内容：
echo   - Docker镜像（4个）
echo   - 项目配置文件
echo   - Windows安装脚本（install.bat）
echo   - Linux安装脚本（install.sh）
echo   - 说明文档（README.md）
echo.
echo 使用方法：
echo   1. 将安装包复制到目标服务器
if exist "%PACKAGE_NAME%.tar.gz" (
    echo   2. 解压: tar -xzf %PACKAGE_NAME%.tar.gz
)
echo   3. Windows: 运行 install.bat
echo   4. Linux: 运行 sudo ./install.sh
echo.
pause

