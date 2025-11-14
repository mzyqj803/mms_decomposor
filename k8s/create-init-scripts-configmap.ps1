# PowerShell 脚本：创建 MariaDB 初始化脚本 ConfigMap
# 使用方法: .\create-init-scripts-configmap.ps1

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$SqlDir = Join-Path $ProjectRoot "src\main\resources\sql\data_init"
$Namespace = "mms"

Write-Host "创建临时目录..." -ForegroundColor Green
$TempDir = New-TemporaryFile | ForEach-Object { Remove-Item $_; New-Item -ItemType Directory -Path $_ }

try {
    Write-Host "复制 SQL 文件到临时目录（按执行顺序）..." -ForegroundColor Green
    
    Copy-Item (Join-Path $SqlDir "schema.sql") (Join-Path $TempDir "01-schema.sql")
    Copy-Item (Join-Path $SqlDir "data_init.sql") (Join-Path $TempDir "02-data_init.sql")
    Copy-Item (Join-Path $SqlDir "cleanup_duplicate_components_with_fk.sql") (Join-Path $TempDir "03-cleanup_duplicates.sql")
    Copy-Item (Join-Path $SqlDir "add_quantity_to_components_relationship.sql") (Join-Path $TempDir "04-add_quantity_field.sql")
    Copy-Item (Join-Path $SqlDir "update_components_relationship_quantity_from_spec.sql") (Join-Path $TempDir "05-update_quantity_from_spec.sql")
    Copy-Item (Join-Path $SqlDir "fastener_warehouse_data.sql") (Join-Path $TempDir "06-fastener_warehouse_data.sql")
    Copy-Item (Join-Path $SqlDir "create_component_fastener_views.sql") (Join-Path $TempDir "07-create_component_fastener_views.sql")
    Copy-Item (Join-Path $SqlDir "update_procurement_flag.sql") (Join-Path $TempDir "08-update_procurement_flag.sql")
    Copy-Item (Join-Path $SqlDir "update_common_parts_flag.sql") (Join-Path $TempDir "09-update_common_parts_flag.sql")
    Copy-Item (Join-Path $SqlDir "fix_duplicate_component_specs.sql") (Join-Path $TempDir "10-fix_duplicate_specs.sql")
    Copy-Item (Join-Path $SqlDir "create_users_and_roles.sql") (Join-Path $TempDir "11-create_users_and_roles.sql")
    Copy-Item (Join-Path $SqlDir "create_permissions.sql") (Join-Path $TempDir "12-create_permissions.sql")

    Write-Host "检查命名空间是否存在..." -ForegroundColor Green
    $namespaceExists = kubectl get namespace $Namespace 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "命名空间 $Namespace 不存在，正在创建..." -ForegroundColor Yellow
        kubectl create namespace $Namespace
    }

    Write-Host "删除已存在的 ConfigMap（如果存在）..." -ForegroundColor Green
    kubectl delete configmap mariadb-init-scripts -n $Namespace --ignore-not-found=true 2>&1 | Out-Null

    Write-Host "创建 ConfigMap..." -ForegroundColor Green
    kubectl create configmap mariadb-init-scripts `
        --from-file="$TempDir" `
        -n $Namespace

    Write-Host "验证 ConfigMap..." -ForegroundColor Green
    kubectl get configmap mariadb-init-scripts -n $Namespace
    kubectl describe configmap mariadb-init-scripts -n $Namespace

    Write-Host "✅ ConfigMap 创建成功！" -ForegroundColor Green
}
finally {
    Write-Host "清理临时目录..." -ForegroundColor Green
    Remove-Item -Recurse -Force $TempDir -ErrorAction SilentlyContinue
}

