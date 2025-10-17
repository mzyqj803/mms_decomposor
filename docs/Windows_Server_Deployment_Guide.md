# Windows Server 部署指南

## 📋 目录

- [系统要求](#系统要求)
- [软件下载与安装](#软件下载与安装)
- [环境配置](#环境配置)
- [项目构建](#项目构建)
- [数据库初始化](#数据库初始化)
- [应用部署](#应用部署)
- [服务配置](#服务配置)
- [监控与维护](#监控与维护)
- [故障排除](#故障排除)

## 系统要求

### 硬件要求
- **CPU**: 4核心以上 (推荐8核心)
- **内存**: 8GB以上 (推荐16GB)
- **硬盘**: 100GB以上可用空间 (推荐SSD)
- **网络**: 稳定的互联网连接

### 操作系统要求
- **Windows Server 2019/2022** (推荐)
- **Windows 10/11** (开发环境)
- 支持Hyper-V或Docker Desktop

## 软件下载与安装

### 1. Java Development Kit (JDK)

#### 1.1 下载JDK 21
```cmd
# 访问Oracle官网或使用OpenJDK
# 推荐使用Eclipse Temurin (AdoptOpenJDK)
https://adoptium.net/temurin/releases/?version=21
```

#### 1.2 安装JDK
1. 下载 `OpenJDK21U-jdk_x64_windows_hotspot_21.0.1_12.msi`
2. 双击运行安装程序
3. 选择安装路径 (推荐: `C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot\`)
4. 完成安装

#### 1.3 配置环境变量
```cmd
# 设置JAVA_HOME
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot"

# 添加到PATH
setx PATH "%PATH%;%JAVA_HOME%\bin"

# 验证安装
java -version
javac -version
```

### 2. Maven

#### 2.1 下载Maven
```cmd
# 访问Maven官网
https://maven.apache.org/download.cgi

# 下载 apache-maven-3.9.6-bin.zip
```

#### 2.2 安装Maven
1. 解压到 `C:\Program Files\Apache\maven\`
2. 配置环境变量:
```cmd
# 设置MAVEN_HOME
setx MAVEN_HOME "C:\Program Files\Apache\maven"

# 添加到PATH
setx PATH "%PATH%;%MAVEN_HOME%\bin"

# 验证安装
mvn -version
```

#### 2.3 配置Maven镜像源
```cmd
# 复制项目提供的Maven配置
copy docs\env\maven\settings.xml %USERPROFILE%\.m2\settings.xml
```

### 3. Node.js

#### 3.1 下载Node.js
```cmd
# 访问Node.js官网
https://nodejs.org/

# 下载 LTS版本 (推荐 v18.x 或 v20.x)
# 下载 node-v20.10.0-x64.msi
```

#### 3.2 安装Node.js
1. 双击运行安装程序
2. 选择安装路径 (推荐: `C:\Program Files\nodejs\`)
3. 勾选 "Add to PATH" 选项
4. 完成安装

#### 3.3 配置NPM镜像源
```cmd
# 复制项目提供的NPM配置
copy docs\env\node\.npmrc %USERPROFILE%\.npmrc

# 验证安装
node -v
npm -v
```

### 4. Docker Desktop

#### 4.1 下载Docker Desktop
```cmd
# 访问Docker官网
https://www.docker.com/products/docker-desktop/

# 下载 Docker Desktop Installer.exe
```

#### 4.2 安装Docker Desktop
1. 双击运行安装程序
2. 勾选 "Use WSL 2 instead of Hyper-V"
3. 完成安装并重启系统

#### 4.3 配置Docker镜像源
```cmd
# 复制项目提供的Docker配置
copy docs\env\docker\daemon.json %USERPROFILE%\.docker\daemon.json

# 重启Docker Desktop
# 验证安装
docker --version
docker-compose --version
```

### 5. Git

#### 5.1 下载Git
```cmd
# 访问Git官网
https://git-scm.com/download/win

# 下载 Git-2.43.0-64-bit.exe
```

#### 5.2 安装Git
1. 双击运行安装程序
2. 选择默认配置
3. 完成安装

#### 5.3 验证安装
```cmd
git --version
```

## 环境配置

### 1. 防火墙配置

#### 1.1 开放必要端口
```cmd
# 开放应用端口
netsh advfirewall firewall add rule name="MMS Backend" dir=in action=allow protocol=TCP localport=8080
netsh advfirewall firewall add rule name="MMS Frontend" dir=in action=allow protocol=TCP localport=9000
netsh advfirewall firewall add rule name="MariaDB" dir=in action=allow protocol=TCP localport=3307
netsh advfirewall firewall add rule name="Redis" dir=in action=allow protocol=TCP localport=6379
```

### 2. 系统优化

#### 2.1 禁用Windows Defender实时保护 (可选)
```cmd
# 仅用于开发环境，生产环境请谨慎使用
Set-MpPreference -DisableRealtimeMonitoring $true
```

#### 2.2 配置虚拟内存
1. 右键"此电脑" → "属性" → "高级系统设置"
2. 点击"性能" → "设置" → "高级"
3. 点击"虚拟内存" → "更改"
4. 设置虚拟内存大小为物理内存的1.5-2倍

## 项目构建与启动

### 1. 克隆项目

```cmd
# 创建项目目录
mkdir C:\Projects
cd C:\Projects

# 克隆项目
git clone <repository-url> mms_decomposor
cd mms_decomposor
```

### 2. 一键启动 (推荐)

项目提供了便捷的 `script\start.bat` 脚本，可以自动完成环境检查、项目构建和服务启动：

```cmd
# 一键启动所有服务
script\start.bat
```

**脚本功能：**
- ✅ 自动检查 Java、Maven、Node.js、Docker 环境
- ✅ 自动构建后端应用 (Maven clean package)
- ✅ 自动安装前端依赖 (npm install)
- ✅ 自动构建前端应用 (npm run build)
- ✅ 自动启动所有服务 (docker-compose up -d)
- ✅ 自动打开浏览器访问应用

### 3. 手动构建 (可选)

如果需要手动控制构建过程，可以分步执行：

#### 3.1 后端构建
```cmd
# 清理和打包
mvn clean package -DskipTests

# 验证构建结果
dir target\mms-decomposor-1.0-SNAPSHOT.jar
```

#### 3.2 前端构建
```cmd
# 安装依赖
cd frontend
npm install

# 构建应用
npm run build

# 验证构建结果
dir dist
cd ..
```

#### 3.3 启动服务
```cmd
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

## 数据库初始化

### 1. 自动初始化 (推荐)

使用 `script\start.bat` 脚本启动时，数据库会自动初始化。Docker Compose 配置中已经包含了数据库初始化脚本。

### 2. 手动初始化 (可选)

如果需要手动初始化数据库：

#### 2.1 启动数据库服务
```cmd
# 启动数据库和Redis
docker-compose up -d mariadb redis

# 检查服务状态
docker-compose ps
```

#### 2.2 验证数据库连接
```cmd
# 检查MariaDB容器
docker logs mms_decomposor-mariadb-1

# 检查Redis容器
docker logs mms_decomposor-redis-1
```

#### 2.3 导入初始数据
```cmd
# 导入数据库结构
docker exec -i mms_decomposor-mariadb-1 mysql -u mms_user -pmms_password mms_db < src/main/resources/sql/schema.sql

# 导入初始数据
docker exec -i mms_decomposor-mariadb-1 mysql -u mms_user -pmms_password mms_db < src/main/resources/sql/data_init.sql

# 导入紧固件数据
docker exec -i mms_decomposor-mariadb-1 mysql -u mms_user -pmms_password mms_db < src/main/resources/sql/fastener_warehouse_init.sql
```

## 应用部署

### 1. 一键部署 (推荐)

使用 `script\start.bat` 脚本已经完成了所有部署步骤，包括：
- Docker镜像构建
- 服务启动
- 数据库初始化

### 2. 手动部署 (可选)

如果需要手动控制部署过程：

#### 2.1 Docker镜像构建
```cmd
# 构建后端镜像
docker build -f Dockerfile.backend -t mms-backend:latest .

# 构建前端镜像
cd frontend
docker build -t mms-frontend:latest .
cd ..

# 验证镜像
docker images mms-backend mms-frontend
```

#### 2.2 启动完整服务
```cmd
# 启动所有服务
docker-compose up -d

# 检查服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f
```

#### 2.3 验证服务运行
```cmd
# 检查端口占用
netstat -an | findstr "8080 9000 3307 6379"

# 测试后端API
curl http://localhost:8080/api/health

# 测试前端页面
curl http://localhost:9000
```

## 便捷脚本使用

项目提供了多个便捷脚本，简化日常操作：

### 1. 启动脚本
```cmd
# 一键启动所有服务
script\start.bat

# 开发模式启动 (支持热重载)
script\dev-start.bat
```

### 2. 管理脚本
```cmd
# 停止所有服务
script\stop.bat

# 重启所有服务
script\restart.bat

# 清理构建文件和日志
script\clean.bat
```

### 3. 检查脚本
```cmd
# 检查环境依赖
script\check-env.bat

# 检查服务状态
script\status.bat

# 查看应用日志
script\logs.bat
```

### 4. 测试脚本
```cmd
# 运行测试命令
script\test-commands.bat

# 测试Redis缓存功能
script\test-redis-cache.bat
```

### 1. 快速启动指南

**最简单的启动方式：**

1. **安装必要软件** (JDK 21, Maven, Node.js, Docker Desktop)
2. **克隆项目**
   ```cmd
   git clone <repository-url> mms_decomposor
   cd mms_decomposor
   ```
3. **一键启动**
   ```cmd
   script\start.bat
   ```
4. **访问应用**
   - 前端: http://localhost:9000
   - 后端API: http://localhost:8080/api

**就这么简单！** 🎉

### 2. 环境检查

在启动前，可以使用环境检查脚本：
```cmd
# 检查所有环境依赖
script\check-env.bat
```

### 3. 服务管理

```cmd
# 查看服务状态
script\status.bat

# 查看应用日志
script\logs.bat

# 停止服务
script\stop.bat

# 重启服务
script\restart.bat
```

## 服务配置

### 1. Windows服务配置

#### 1.1 创建服务启动脚本
```cmd
# 创建服务目录
mkdir C:\Services\MMS

# 复制启动脚本
xcopy script C:\Services\MMS\script\ /E /I /Y
```

#### 1.2 配置Windows服务 (使用NSSM)
```cmd
# 下载NSSM
# 访问 https://nssm.cc/download
# 下载 nssm-2.24.zip

# 解压到 C:\Tools\nssm\
# 添加NSSM到PATH
setx PATH "%PATH%;C:\Tools\nssm\win64"

# 创建后端服务
nssm install MMS-Backend "C:\Services\MMS\script\start.bat"
nssm set MMS-Backend Description "MMS Manufacturing Management System Backend"
nssm set MMS-Backend Start SERVICE_AUTO_START

# 创建前端服务
nssm install MMS-Frontend "C:\Services\MMS\script\start.bat"
nssm set MMS-Frontend Description "MMS Manufacturing Management System Frontend"
nssm set MMS-Frontend Start SERVICE_AUTO_START
```

### 2. 自动启动配置

#### 2.1 配置开机自启
```cmd
# 启动服务
net start MMS-Backend
net start MMS-Frontend

# 设置服务为自动启动
sc config MMS-Backend start= auto
sc config MMS-Frontend start= auto
```

### 3. 负载均衡配置 (可选)

#### 3.1 使用Nginx作为反向代理
```cmd
# 下载Nginx for Windows
# 访问 http://nginx.org/en/download.html
# 下载 nginx-1.24.0.zip

# 解压到 C:\nginx\
# 配置nginx.conf
```

#### 3.2 Nginx配置示例
```nginx
# nginx.conf
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server localhost:8080;
    }
    
    upstream frontend {
        server localhost:9000;
    }
    
    server {
        listen 80;
        server_name localhost;
        
        location /api/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
        
        location / {
            proxy_pass http://frontend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
```

## 监控与维护

### 1. 日志管理

#### 1.1 配置日志轮转
```cmd
# 创建日志目录
mkdir C:\Logs\MMS

# 配置日志轮转脚本
# 创建 logrotate.bat
```

#### 1.2 日志轮转脚本
```batch
@echo off
REM logrotate.bat
set LOG_DIR=C:\Logs\MMS
set MAX_SIZE=100MB
set MAX_FILES=10

for /f "tokens=*" %%i in ('dir /b %LOG_DIR%\*.log') do (
    if %%~zi GTR 104857600 (
        echo Rotating log file: %%i
        move "%LOG_DIR%\%%i" "%LOG_DIR%\%%i.%date:~0,4%%date:~5,2%%date:~8,2%"
    )
)

REM 删除超过保留数量的日志文件
for /f "skip=%MAX_FILES% tokens=*" %%i in ('dir /b /o-d %LOG_DIR%\*.log.*') do (
    del "%LOG_DIR%\%%i"
)
```

### 2. 性能监控

#### 2.1 系统监控脚本
```cmd
# 创建监控脚本 monitor.bat
```

#### 2.2 监控脚本内容
```batch
@echo off
REM monitor.bat
echo ========================================
echo MMS System Monitor - %date% %time%
echo ========================================

echo.
echo === Docker Services Status ===
docker-compose ps

echo.
echo === System Resources ===
wmic cpu get loadpercentage /value
wmic OS get TotalVisibleMemorySize,FreePhysicalMemory /value

echo.
echo === Disk Usage ===
wmic logicaldisk get size,freespace,caption

echo.
echo === Network Connections ===
netstat -an | findstr "8080 9000 3307 6379"

echo.
echo === Application Logs (Last 10 lines) ===
docker-compose logs --tail=10

echo ========================================
```

### 3. 备份策略

#### 3.1 数据库备份脚本
```cmd
# 创建备份脚本 backup.bat
```

#### 3.2 备份脚本内容
```batch
@echo off
REM backup.bat
set BACKUP_DIR=C:\Backups\MMS
set DATE=%date:~0,4%%date:~5,2%%date:~8,2%
set TIME=%time:~0,2%%time:~3,2%%time:~6,2%

mkdir "%BACKUP_DIR%" 2>nul

echo Starting database backup...
docker exec mms_decomposor-mariadb-1 mysqldump -u mms_user -pmms_password mms_db > "%BACKUP_DIR%\mms_db_%DATE%_%TIME%.sql"

echo Starting application backup...
xcopy "C:\Projects\mms_decomposor" "%BACKUP_DIR%\app_%DATE%_%TIME%" /E /I /H /Y

echo Backup completed: %DATE%_%TIME%
```

## 故障排除

### 1. 常见问题

#### 1.1 端口占用问题
```cmd
# 检查端口占用
netstat -ano | findstr "8080"
netstat -ano | findstr "9000"

# 终止占用进程
taskkill /PID <PID> /F
```

#### 1.2 Docker服务问题
```cmd
# 重启Docker服务
docker-compose down
docker-compose up -d

# 清理Docker资源
docker system prune -a
```

#### 1.3 数据库连接问题
```cmd
# 检查数据库状态
docker logs mms_decomposor-mariadb-1

# 重启数据库
docker-compose restart mariadb
```

### 2. 日志分析

#### 2.1 应用日志
```cmd
# 查看应用日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 查看特定时间日志
docker-compose logs --since="2024-01-01T00:00:00" backend
```

#### 2.2 系统日志
```cmd
# 查看Windows事件日志
eventvwr.msc

# 查看Docker日志
docker logs <container_id>
```

### 3. 性能优化

#### 3.1 JVM调优
```cmd
# 在docker-compose.yml中调整JVM参数
environment:
  - JAVA_OPTS=-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

#### 3.2 数据库优化
```sql
-- 在MariaDB中执行
SET GLOBAL innodb_buffer_pool_size = 2G;
SET GLOBAL max_connections = 200;
SET GLOBAL query_cache_size = 128M;
```

## 安全配置

### 1. 网络安全

#### 1.1 SSL证书配置
```cmd
# 生成自签名证书 (仅用于测试)
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# 配置HTTPS (在nginx.conf中)
```

#### 1.2 防火墙规则
```cmd
# 限制访问来源
netsh advfirewall firewall add rule name="MMS Backend Restricted" dir=in action=allow protocol=TCP localport=8080 remoteip=192.168.1.0/24
```

### 2. 数据安全

#### 2.1 数据库安全
```sql
-- 创建只读用户
CREATE USER 'mms_readonly'@'%' IDENTIFIED BY 'readonly_password';
GRANT SELECT ON mms_db.* TO 'mms_readonly'@'%';

-- 创建备份用户
CREATE USER 'mms_backup'@'localhost' IDENTIFIED BY 'backup_password';
GRANT SELECT, LOCK TABLES ON mms_db.* TO 'mms_backup'@'localhost';
```

## 更新与维护

### 1. 应用更新

#### 1.1 滚动更新
```cmd
# 停止服务
docker-compose down

# 拉取最新代码
git pull origin master

# 重新构建镜像
docker-compose build

# 启动服务
docker-compose up -d
```

#### 1.2 数据库迁移
```cmd
# 备份当前数据库
docker exec mms_decomposor-mariadb-1 mysqldump -u mms_user -pmms_password mms_db > backup.sql

# 执行迁移脚本
docker exec -i mms_decomposor-mariadb-1 mysql -u mms_user -pmms_password mms_db < migration.sql
```

### 2. 系统维护

#### 2.1 定期维护任务
```cmd
# 创建维护脚本 maintenance.bat
```

#### 2.2 维护脚本内容
```batch
@echo off
REM maintenance.bat
echo Starting system maintenance...

echo 1. Cleaning Docker images...
docker image prune -f

echo 2. Cleaning Docker volumes...
docker volume prune -f

echo 3. Updating system packages...
# 如果有包管理器，在这里添加更新命令

echo 4. Checking disk space...
dir C:\ /-c

echo 5. Checking service status...
docker-compose ps

echo Maintenance completed.
```

## 联系支持

如果在部署过程中遇到问题，请：

1. 查看本文档的故障排除部分
2. 检查应用日志和系统日志
3. 联系技术支持团队

---

**注意**: 本指南基于Windows Server环境编写，部分命令可能需要根据实际环境进行调整。建议在生产环境部署前先在测试环境验证所有步骤。
