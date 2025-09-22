# Windows环境启动指南

## 环境要求

### 必需软件
1. **JDK 11+** - Java开发环境
2. **Maven 3.6+** - Java项目构建工具
3. **Node.js 16+** - 前端开发环境
4. **Docker Desktop** - 容器化部署工具
5. **Git** - 版本控制工具

### 安装步骤

#### 1. 安装JDK 11
- 下载地址: https://adoptium.net/
- 选择Windows x64版本
- 安装后配置JAVA_HOME环境变量

#### 2. 安装Maven
- 下载地址: https://maven.apache.org/download.cgi
- 解压到指定目录
- 配置MAVEN_HOME和PATH环境变量

#### 3. 安装Node.js
- 下载地址: https://nodejs.org/
- 选择LTS版本
- 安装时自动配置PATH环境变量

#### 4. 安装Docker Desktop
- 下载地址: https://www.docker.com/products/docker-desktop
- 安装后启动Docker Desktop
- 确保Docker服务正在运行

#### 5. 安装Git
- 下载地址: https://git-scm.com/download/win
- 安装时选择默认配置

## 快速启动

### 方式一：生产环境启动 (推荐)
```cmd
# 双击运行或在命令行执行
start.bat
```

### 方式二：开发环境启动
```cmd
# 开发模式启动，支持热重载
dev-start.bat
```

## 脚本说明

### 主要脚本

| 脚本名称 | 功能描述 | 使用场景 |
|---------|---------|---------|
| `start.bat` | 一键启动生产环境 | 正式部署使用 |
| `stop.bat` | 停止所有服务 | 停止系统 |
| `restart.bat` | 重启系统 | 重启服务 |
| `dev-start.bat` | 开发环境启动 | 开发调试使用 |
| `stop-dev.bat` | 停止开发环境 | 停止开发服务 |

### 管理脚本

| 脚本名称 | 功能描述 |
|---------|---------|
| `status.bat` | 检查系统状态 |
| `logs.bat` | 查看服务日志 |
| `clean.bat` | 清理系统资源 |

## 启动流程

### 生产环境启动流程
1. **环境检查** - 验证Java、Maven、Node.js、Docker是否安装
2. **构建后端** - 使用Maven编译打包Spring Boot应用
3. **安装前端依赖** - 使用npm安装Vue项目依赖
4. **构建前端** - 使用Vite构建生产版本
5. **启动服务** - 使用Docker Compose启动所有服务

### 开发环境启动流程
1. **环境检查** - 验证开发环境
2. **启动基础服务** - 启动MariaDB和Redis
3. **启动后端开发服务器** - Spring Boot热重载模式
4. **启动前端开发服务器** - Vite热重载模式

## 访问地址

启动成功后，可以通过以下地址访问：

- **前端应用**: http://localhost:3000
- **后端API**: http://localhost:8080/api
- **数据库**: localhost:3306 (用户名: mms_user, 密码: mms_password)
- **Redis**: localhost:6379

## 常见问题

### 1. 端口被占用
```cmd
# 检查端口占用
netstat -an | findstr ":3000"
netstat -an | findstr ":8080"

# 停止占用端口的进程
taskkill /f /pid <进程ID>
```

### 2. Docker服务未启动
- 启动Docker Desktop
- 等待Docker服务完全启动
- 检查Docker状态: `docker --version`

### 3. 环境变量未配置
- 检查JAVA_HOME: `echo %JAVA_HOME%`
- 检查MAVEN_HOME: `echo %MAVEN_HOME%`
- 检查PATH: `echo %PATH%`

### 4. 权限问题
- 以管理员身份运行命令提示符
- 检查文件夹读写权限
- 确保防火墙允许相关端口

### 5. 内存不足
- 关闭其他占用内存的应用程序
- 增加Docker Desktop的内存分配
- 检查系统可用内存

## 开发调试

### 前端开发
```cmd
cd frontend
npm run dev
```
- 支持热重载
- 自动打开浏览器
- 实时错误提示

### 后端开发
```cmd
mvn spring-boot:run
```
- 支持热重载
- 自动重启
- 详细日志输出

### 数据库管理
- 使用Docker命令连接数据库
- 使用数据库管理工具 (如Navicat, DBeaver)
- 查看数据库日志: `docker-compose logs mariadb`

## 日志查看

### 查看所有服务日志
```cmd
logs.bat
```

### 查看特定服务日志
```cmd
# 查看后端日志
docker-compose logs -f backend

# 查看前端日志
docker-compose logs -f frontend

# 查看数据库日志
docker-compose logs -f mariadb
```

## 系统维护

### 清理系统
```cmd
clean.bat
```
- 轻度清理: 仅删除容器
- 中度清理: 删除容器和镜像
- 重度清理: 删除所有数据 (谨慎使用)

### 备份数据
```cmd
# 备份数据库
docker-compose exec mariadb mysqldump -u mms_user -p mms_db > backup.sql

# 备份Redis数据
docker-compose exec redis redis-cli BGSAVE
```

### 更新系统
```cmd
# 拉取最新代码
git pull

# 重新构建和启动
restart.bat
```

## 性能优化

### Windows系统优化
1. **关闭Windows Defender实时保护** (开发环境)
2. **增加虚拟内存**
3. **关闭不必要的启动项**
4. **使用SSD硬盘**

### Docker优化
1. **增加Docker内存分配** (建议4GB+)
2. **启用WSL2后端**
3. **配置镜像加速器**

### 开发工具优化
1. **使用VS Code** 作为开发IDE
2. **安装相关插件**:
   - Java Extension Pack
   - Vue Language Features (Volar)
   - Docker
   - GitLens

## 故障排除

### 启动失败
1. 检查环境变量配置
2. 检查端口占用情况
3. 查看详细错误日志
4. 重启Docker服务

### 服务无响应
1. 检查容器状态: `docker-compose ps`
2. 查看服务日志: `logs.bat`
3. 检查网络连接
4. 重启相关服务

### 数据丢失
1. 检查数据卷挂载
2. 查看数据库日志
3. 从备份恢复数据
4. 重新初始化数据库

## 联系支持

如遇到问题，请：
1. 查看日志文件
2. 检查系统状态
3. 参考本文档故障排除部分
4. 联系技术支持团队
