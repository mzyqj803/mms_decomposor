param(
    [string]$FilePath,
    [string]$Type
)

$content = Get-Content $FilePath -Raw -Encoding UTF8

if ($Type -eq "upgrade") {
    $content = $content -replace 'INSTALL_TYPE_PLACEHOLDER', '升级安装脚本'
    $content = $content -replace 'CLEAR_VOLUMES_PLACEHOLDER', @"
echo [提示] 检查并清理旧容器 (保留数据卷)...
docker rm -f mms-redis mms-mariadb mms-backend mms-frontend >nul 2>&1
docker-compose down >nul 2>&1
"@
    $content = $content -replace 'UPGRADE_NOTE_PLACEHOLDER', @"
echo 注意：本次安装保留了数据库数据
echo 如需全新安装并清除所有数据，请使用 initial_install.bat
echo.
"@
} else {
    $content = $content -replace 'INSTALL_TYPE_PLACEHOLDER', '全新安装脚本'
    $content = $content -replace 'CLEAR_VOLUMES_PLACEHOLDER', @"
echo [提示] 检查并清理旧容器和数据卷...
docker rm -f mms-redis mms-mariadb mms-backend mms-frontend >nul 2>&1
docker-compose down -v >nul 2>&1
"@
    $content = $content -replace 'UPGRADE_NOTE_PLACEHOLDER', ''
}

# 使用 UTF8 编码写入文件（带BOM）
[System.IO.File]::WriteAllText($FilePath, $content, [System.Text.Encoding]::UTF8)

