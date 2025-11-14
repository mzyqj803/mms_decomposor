#!/bin/bash

# Kubernetes 部署脚本
# 使用方法: ./deploy.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🚀 开始部署 MMS Decomposor 到 Kubernetes..."

# 1. 创建命名空间
echo "📦 创建命名空间..."
kubectl apply -f "$SCRIPT_DIR/namespace.yaml"

# 2. 创建 Secret
echo "🔐 创建 Secret..."
kubectl apply -f "$SCRIPT_DIR/secret.yaml"

# 3. 创建数据库初始化脚本 ConfigMap
echo "📝 创建数据库初始化脚本 ConfigMap..."
if [ -f "$SCRIPT_DIR/create-init-scripts-configmap.sh" ]; then
    bash "$SCRIPT_DIR/create-init-scripts-configmap.sh"
else
    echo "⚠️  请先运行 create-init-scripts-configmap.sh 创建数据库初始化脚本 ConfigMap"
    exit 1
fi

# 4. 创建持久化存储
echo "💾 创建持久化存储..."
kubectl apply -f "$SCRIPT_DIR/persistentvolumeclaim.yaml"

# 5. 部署 MariaDB
echo "🗄️  部署 MariaDB..."
kubectl apply -f "$SCRIPT_DIR/mariadb-deployment.yaml"
kubectl apply -f "$SCRIPT_DIR/mariadb-service.yaml"

# 等待数据库就绪
echo "⏳ 等待 MariaDB 就绪..."
kubectl wait --for=condition=ready pod -l app=mariadb -n mms --timeout=300s

# 6. 部署 Redis
echo "📦 部署 Redis..."
kubectl apply -f "$SCRIPT_DIR/redis-deployment.yaml"
kubectl apply -f "$SCRIPT_DIR/redis-service.yaml"

# 等待 Redis 就绪
echo "⏳ 等待 Redis 就绪..."
kubectl wait --for=condition=ready pod -l app=redis -n mms --timeout=60s

# 7. 部署后端
echo "🔧 部署后端服务..."
kubectl apply -f "$SCRIPT_DIR/backend-deployment.yaml"
kubectl apply -f "$SCRIPT_DIR/backend-service.yaml"

# 8. 部署前端
echo "🎨 部署前端服务..."
kubectl apply -f "$SCRIPT_DIR/frontend-deployment.yaml"
kubectl apply -f "$SCRIPT_DIR/frontend-service.yaml"

# 9. 部署 Ingress（可选）
read -p "是否部署 Ingress? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🌐 部署 Ingress..."
    kubectl apply -f "$SCRIPT_DIR/ingress.yaml"
fi

echo "✅ 部署完成！"
echo ""
echo "查看 Pod 状态:"
kubectl get pods -n mms
echo ""
echo "查看服务状态:"
kubectl get svc -n mms

