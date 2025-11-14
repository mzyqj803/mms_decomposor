# MMS Decomposor Kubernetes 部署配置

本目录包含将 MMS Decomposor 应用部署到 Kubernetes 集群所需的所有配置文件。

## 文件说明

- `namespace.yaml` - 创建 mms 命名空间
- `secret.yaml` - 存储敏感信息（数据库密码、JWT密钥等）
- `persistentvolumeclaim.yaml` - 持久化存储卷声明（MariaDB 和 Redis）
- `mariadb-deployment.yaml` - MariaDB 数据库部署配置
- `mariadb-service.yaml` - MariaDB 服务配置
- `redis-deployment.yaml` - Redis 缓存部署配置
- `redis-service.yaml` - Redis 服务配置
- `backend-deployment.yaml` - 后端服务部署配置
- `backend-service.yaml` - 后端服务配置
- `frontend-deployment.yaml` - 前端服务部署配置
- `frontend-service.yaml` - 前端服务配置
- `ingress.yaml` - Ingress 配置（用于外部访问）
- `kustomization.yaml` - Kustomize 配置文件

## 部署前准备

### 1. 构建和推送 Docker 镜像

确保您已经构建并推送了以下镜像到您的镜像仓库：

```bash
# 构建后端镜像
docker build -f Dockerfile.backend -t your-registry/mms-backend:latest .
docker push your-registry/mms-backend:latest

# 构建前端镜像
cd frontend
docker build -f Dockerfile -t your-registry/mms-frontend:latest .
docker push your-registry/mms-frontend:latest
```

### 2. 创建数据库初始化脚本 ConfigMap

由于 SQL 初始化脚本文件较大，需要单独创建 ConfigMap：

```bash
# 创建包含所有 SQL 初始化脚本的 ConfigMap
kubectl create configmap mariadb-init-scripts \
  --from-file=../src/main/resources/sql/data_init/schema.sql \
  --from-file=../src/main/resources/sql/data_init/data_init.sql \
  --from-file=../src/main/resources/sql/data_init/cleanup_duplicate_components_with_fk.sql \
  --from-file=../src/main/resources/sql/data_init/add_quantity_to_components_relationship.sql \
  --from-file=../src/main/resources/sql/data_init/update_components_relationship_quantity_from_spec.sql \
  --from-file=../src/main/resources/sql/data_init/fastener_warehouse_data.sql \
  --from-file=../src/main/resources/sql/data_init/create_component_fastener_views.sql \
  --from-file=../src/main/resources/sql/data_init/update_procurement_flag.sql \
  --from-file=../src/main/resources/sql/data_init/update_common_parts_flag.sql \
  --from-file=../src/main/resources/sql/data_init/fix_duplicate_component_specs.sql \
  --from-file=../src/main/resources/sql/data_init/create_users_and_roles.sql \
  --from-file=../src/main/resources/sql/data_init/create_permissions.sql \
  -n mms
```

或者使用脚本文件创建（需要按顺序命名）：

```bash
# 创建脚本目录
mkdir -p init-scripts

# 复制并重命名 SQL 文件（按执行顺序）
cp ../src/main/resources/sql/data_init/schema.sql init-scripts/01-schema.sql
cp ../src/main/resources/sql/data_init/data_init.sql init-scripts/02-data_init.sql
cp ../src/main/resources/sql/data_init/cleanup_duplicate_components_with_fk.sql init-scripts/03-cleanup_duplicates.sql
cp ../src/main/resources/sql/data_init/add_quantity_to_components_relationship.sql init-scripts/04-add_quantity_field.sql
cp ../src/main/resources/sql/data_init/update_components_relationship_quantity_from_spec.sql init-scripts/05-update_quantity_from_spec.sql
cp ../src/main/resources/sql/data_init/fastener_warehouse_data.sql init-scripts/06-fastener_warehouse_data.sql
cp ../src/main/resources/sql/data_init/create_component_fastener_views.sql init-scripts/07-create_component_fastener_views.sql
cp ../src/main/resources/sql/data_init/update_procurement_flag.sql init-scripts/08-update_procurement_flag.sql
cp ../src/main/resources/sql/data_init/update_common_parts_flag.sql init-scripts/09-update_common_parts_flag.sql
cp ../src/main/resources/sql/data_init/fix_duplicate_component_specs.sql init-scripts/10-fix_duplicate_specs.sql
cp ../src/main/resources/sql/data_init/create_users_and_roles.sql init-scripts/11-create_users_and_roles.sql
cp ../src/main/resources/sql/data_init/create_permissions.sql init-scripts/12-create_permissions.sql

# 创建 ConfigMap
kubectl create configmap mariadb-init-scripts --from-file=init-scripts/ -n mms
```

### 3. 修改配置

在部署前，请根据您的环境修改以下配置：

1. **secret.yaml**: 修改数据库密码和 JWT 密钥（生产环境必须修改）
2. **backend-deployment.yaml** 和 **frontend-deployment.yaml**: 
   - 修改 `image` 字段为您的镜像仓库地址
   - 根据需求调整 `replicas` 数量
3. **persistentvolumeclaim.yaml**: 
   - 根据您的存储类修改 `storageClassName`
   - 根据需要调整存储大小
4. **ingress.yaml**: 
   - 修改 `host` 为您的域名
   - 根据您的 Ingress Controller 修改 `ingressClassName`
   - 如需启用 HTTPS，配置 TLS 证书

## 部署步骤

### 方式一：使用 kubectl 直接部署

```bash
# 1. 创建命名空间
kubectl apply -f namespace.yaml

# 2. 创建 Secret
kubectl apply -f secret.yaml

# 3. 创建数据库初始化脚本 ConfigMap（见上面的准备步骤）

# 4. 创建持久化存储
kubectl apply -f persistentvolumeclaim.yaml

# 5. 部署 MariaDB
kubectl apply -f mariadb-deployment.yaml
kubectl apply -f mariadb-service.yaml

# 6. 部署 Redis
kubectl apply -f redis-deployment.yaml
kubectl apply -f redis-service.yaml

# 7. 等待数据库就绪后，部署后端
kubectl apply -f backend-deployment.yaml
kubectl apply -f backend-service.yaml

# 8. 部署前端
kubectl apply -f frontend-deployment.yaml
kubectl apply -f frontend-service.yaml

# 9. 部署 Ingress（可选）
kubectl apply -f ingress.yaml
```

### 方式二：使用 Kustomize 部署

```bash
# 确保已创建数据库初始化脚本 ConfigMap（见上面的准备步骤）

# 使用 kustomize 部署所有资源
kubectl apply -k .
```

## 验证部署

```bash
# 查看所有 Pod 状态
kubectl get pods -n mms

# 查看服务状态
kubectl get svc -n mms

# 查看 Pod 日志
kubectl logs -f deployment/backend -n mms
kubectl logs -f deployment/frontend -n mms
kubectl logs -f deployment/mariadb -n mms
kubectl logs -f deployment/redis -n mms

# 检查后端健康状态
kubectl exec -it deployment/backend -n mms -- curl http://localhost:8080/api/actuator/health
```

## 访问应用

- **通过 Ingress**: 如果配置了 Ingress，通过配置的域名访问
- **通过 Port Forward**: 
  ```bash
  # 前端
  kubectl port-forward svc/frontend 9000:80 -n mms
  
  # 后端
  kubectl port-forward svc/backend 8080:8080 -n mms
  ```
- **通过 NodePort**: 修改 Service 类型为 NodePort 或 LoadBalancer

## 注意事项

1. **生产环境安全**:
   - 必须修改 `secret.yaml` 中的所有密码和密钥
   - 建议使用 Kubernetes Secrets 管理工具（如 Sealed Secrets、External Secrets Operator）
   - 启用 TLS/SSL 加密

2. **存储**:
   - 确保您的 Kubernetes 集群已配置存储类（StorageClass）
   - 根据实际需求调整 PVC 的存储大小

3. **资源限制**:
   - 根据实际负载调整 Deployment 中的 `resources` 配置
   - 监控 Pod 的资源使用情况

4. **高可用**:
   - 数据库和 Redis 当前配置为单副本，生产环境建议配置主从复制或集群模式
   - 后端和前端可以增加副本数以提高可用性

5. **备份**:
   - 定期备份 MariaDB 数据
   - 考虑使用 Velero 等工具进行集群备份

## 卸载

```bash
# 删除所有资源
kubectl delete -k .

# 或逐个删除
kubectl delete -f ingress.yaml
kubectl delete -f frontend-service.yaml
kubectl delete -f frontend-deployment.yaml
kubectl delete -f backend-service.yaml
kubectl delete -f backend-deployment.yaml
kubectl delete -f redis-service.yaml
kubectl delete -f redis-deployment.yaml
kubectl delete -f mariadb-service.yaml
kubectl delete -f mariadb-deployment.yaml
kubectl delete -f persistentvolumeclaim.yaml
kubectl delete configmap mariadb-init-scripts -n mms
kubectl delete -f secret.yaml
kubectl delete -f namespace.yaml
```

