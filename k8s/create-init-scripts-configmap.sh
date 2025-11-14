#!/bin/bash

# 创建 MariaDB 初始化脚本 ConfigMap 的脚本
# 使用方法: ./create-init-scripts-configmap.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SQL_DIR="$PROJECT_ROOT/src/main/resources/sql/data_init"
NAMESPACE="mms"

echo "创建临时目录..."
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

echo "复制 SQL 文件到临时目录（按执行顺序）..."
cp "$SQL_DIR/schema.sql" "$TEMP_DIR/01-schema.sql"
cp "$SQL_DIR/data_init.sql" "$TEMP_DIR/02-data_init.sql"
cp "$SQL_DIR/cleanup_duplicate_components_with_fk.sql" "$TEMP_DIR/03-cleanup_duplicates.sql"
cp "$SQL_DIR/add_quantity_to_components_relationship.sql" "$TEMP_DIR/04-add_quantity_field.sql"
cp "$SQL_DIR/update_components_relationship_quantity_from_spec.sql" "$TEMP_DIR/05-update_quantity_from_spec.sql"
cp "$SQL_DIR/fastener_warehouse_data.sql" "$TEMP_DIR/06-fastener_warehouse_data.sql"
cp "$SQL_DIR/create_component_fastener_views.sql" "$TEMP_DIR/07-create_component_fastener_views.sql"
cp "$SQL_DIR/update_procurement_flag.sql" "$TEMP_DIR/08-update_procurement_flag.sql"
cp "$SQL_DIR/update_common_parts_flag.sql" "$TEMP_DIR/09-update_common_parts_flag.sql"
cp "$SQL_DIR/fix_duplicate_component_specs.sql" "$TEMP_DIR/10-fix_duplicate_specs.sql"
cp "$SQL_DIR/create_users_and_roles.sql" "$TEMP_DIR/11-create_users_and_roles.sql"
cp "$SQL_DIR/create_permissions.sql" "$TEMP_DIR/12-create_permissions.sql"

echo "检查命名空间是否存在..."
if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
    echo "命名空间 $NAMESPACE 不存在，正在创建..."
    kubectl create namespace "$NAMESPACE"
fi

echo "删除已存在的 ConfigMap（如果存在）..."
kubectl delete configmap mariadb-init-scripts -n "$NAMESPACE" --ignore-not-found=true

echo "创建 ConfigMap..."
kubectl create configmap mariadb-init-scripts \
    --from-file="$TEMP_DIR/" \
    -n "$NAMESPACE"

echo "验证 ConfigMap..."
kubectl get configmap mariadb-init-scripts -n "$NAMESPACE"
kubectl describe configmap mariadb-init-scripts -n "$NAMESPACE"

echo "✅ ConfigMap 创建成功！"

