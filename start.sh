#!/bin/bash

echo "启动MMS制造管理系统..."

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装，请先安装Docker"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 构建后端应用
echo "构建后端应用..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "错误: 后端应用构建失败"
    exit 1
fi

# 启动所有服务
echo "启动所有服务..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo "✅ MMS制造管理系统启动成功！"
    echo ""
    echo "访问地址:"
    echo "  前端应用: http://localhost:3000"
    echo "  后端API: http://localhost:8080/api"
    echo ""
    echo "数据库连接信息:"
    echo "  Host: localhost:3306"
    echo "  Database: mms_db"
    echo "  Username: mms_user"
    echo "  Password: mms_password"
    echo ""
    echo "Redis连接信息:"
    echo "  Host: localhost:6379"
    echo ""
    echo "使用 'docker-compose logs -f' 查看日志"
    echo "使用 'docker-compose down' 停止服务"
else
    echo "❌ 服务启动失败，请检查日志"
    exit 1
fi
