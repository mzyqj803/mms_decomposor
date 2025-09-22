#!/bin/bash

echo "停止MMS制造管理系统..."

# 停止所有服务
docker-compose down

if [ $? -eq 0 ]; then
    echo "✅ MMS制造管理系统已停止"
else
    echo "❌ 停止服务失败"
    exit 1
fi
