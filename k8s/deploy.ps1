# PowerShell 部署脚本
# 使用方法: .\deploy.ps1

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "🚀 开始部署 MMS Decomposor 到 Kubernetes..." -ForegroundColor Cyan

# 1. 创建命名空间
Write-Host "📦 创建命名空间..." -ForegroundColor Green
kubectl apply -f "$ScriptDir\namespace.yaml"

# 2. 创建 Secret
Write-Host "🔐 创建 Secret..." -ForegroundColor Green
kubectl apply -f "$ScriptDir\secret.yaml"

# 3. 创建数据库初始化脚本 ConfigMap
Write-Host "📝 创建数据库初始化脚本 ConfigMap..." -ForegroundColor Green
if (Test-Path "$ScriptDir\create-init-scripts-configmap.ps1") {
    & "$ScriptDir\create-init-scripts-configmap.ps1"
} else {
    Write-Host "⚠️  请先运行 create-init-scripts-configmap.ps1 创建数据库初始化脚本 ConfigMap" -ForegroundColor Yellow
    exit 1
}

# 4. 创建持久化存储
Write-Host "💾 创建持久化存储..." -ForegroundColor Green
kubectl apply -f "$ScriptDir\persistentvolumeclaim.yaml"

# 5. 部署 MariaDB
Write-Host "🗄️  部署 MariaDB..." -ForegroundColor Green
kubectl apply -f "$ScriptDir\mariadb-deployment.yaml"
kubectl apply -f "$ScriptDir\mariadb-service.yaml"

# 等待数据库就绪
Write-Host "⏳ 等待 MariaDB 就绪..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=mariadb -n mms --timeout=300s

# 6. 部署 Redis
Write-Host "📦 部署 Redis..." -ForegroundColor Green
kubectl apply -f "$ScriptDir\redis-deployment.yaml"
kubectl apply -f "$ScriptDir\redis-service.yaml"

# 等待 Redis 就绪
Write-Host "⏳ 等待 Redis 就绪..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=redis -n mms --timeout=60s

# 7. 部署后端
Write-Host "🔧 部署后端服务..." -ForegroundColor Green
kubectl apply -f "$ScriptDir\backend-deployment.yaml"
kubectl apply -f "$ScriptDir\backend-service.yaml"

# 8. 部署前端
Write-Host "🎨 部署前端服务..." -ForegroundColor Green
kubectl apply -f "$ScriptDir\frontend-deployment.yaml"
kubectl apply -f "$ScriptDir\frontend-service.yaml"

# 9. 部署 Ingress（可选）
$deployIngress = Read-Host "是否部署 Ingress? (y/n)"
if ($deployIngress -eq "y" -or $deployIngress -eq "Y") {
    Write-Host "🌐 部署 Ingress..." -ForegroundColor Green
    kubectl apply -f "$ScriptDir\ingress.yaml"
}

Write-Host "✅ 部署完成！" -ForegroundColor Green
Write-Host ""
Write-Host "查看 Pod 状态:" -ForegroundColor Cyan
kubectl get pods -n mms
Write-Host ""
Write-Host "查看服务状态:" -ForegroundColor Cyan
kubectl get svc -n mms

