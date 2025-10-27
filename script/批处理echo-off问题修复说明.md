# 批处理 @echo off 不生效问题修复

## 问题现象

生成的 `initial_install.bat` 和 `install.bat` 虽然第一行有 `@echo off`，但运行时仍然显示所有命令：

```batch
E:\...\>setlocal enabledelayedexpansion
E:\...\>REM 切换到脚本所在目录
E:\...\>cd /d "E:\..."
```

## 根本原因

**UTF-8 BOM（Byte Order Mark）导致批处理无法识别第一行的 `@echo off`**

- UTF-8 BOM 是文件开头的3个字节：`EF BB BF`
- Windows 批处理解释器无法识别这些字节
- 导致第一行的 `@echo off` 无法生效
- 结果是所有命令都被回显

### 技术细节

当 PowerShell 使用以下代码写入文件时：
```powershell
[System.IO.File]::WriteAllText($FilePath, $content, [System.Text.Encoding]::UTF8)
```

会自动添加 UTF-8 BOM，导致批处理文件变成：
```
[EF BB BF]@echo off
```

批处理解释器看到的是：
```
[乱码]@echo off  ← 这行无法被识别为有效命令
```

## 解决方案

修改 `generate-install.ps1` 使用**不带BOM的UTF-8编码**：

### 修改前（有BOM）
```powershell
# 使用 UTF8 编码写入文件（带BOM）
[System.IO.File]::WriteAllText($FilePath, $content, [System.Text.Encoding]::UTF8)
```

### 修改后（无BOM）✅
```powershell
# 使用不带BOM的UTF8编码写入文件
# 这样可以确保 @echo off 能够正确生效（BOM会导致批处理无法识别第一行）
# 同时配合脚本中的 chcp 65001 可以正确显示中文
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($FilePath, $content, $utf8NoBom)
```

## 为什么使用UTF-8（无BOM）而不是GBK

1. **中文显示**：脚本中有 `chcp 65001 >nul` 切换到UTF-8代码页
2. **跨平台**：UTF-8是通用编码，GBK只在中文Windows上正确
3. **无乱码**：无BOM的UTF-8配合UTF-8代码页可以完美显示中文
4. **@echo off生效**：没有BOM不会破坏第一行的命令识别

## 各种编码对比

| 编码方式 | @echo off生效? | 中文显示 | 跨平台 | 推荐 |
|---------|---------------|---------|--------|------|
| UTF-8 (带BOM) | ❌ 不生效 | ✅ 正常 | ✅ 好 | ❌ |
| UTF-8 (无BOM) | ✅ 生效 | ✅ 正常 | ✅ 好 | ✅ 推荐 |
| GBK/ANSI | ✅ 生效 | ⚠️ 需要GBK代码页 | ❌ 差 | ⚠️ |
| ASCII | ✅ 生效 | ❌ 中文乱码 | ✅ 好 | ❌ |

## 验证方法

### 1. 检查文件是否有BOM

```powershell
# 读取文件的前3个字节
$bytes = [System.IO.File]::ReadAllBytes("install.bat")
$bytes[0..2] -join ','

# 如果输出是 "239,187,191" 说明有UTF-8 BOM（需要修复）
# 如果输出是 "64,101,99" (@ec) 说明无BOM（正确）
```

### 2. 重新生成安装包

```batch
cd script
export-offline-package.bat
```

### 3. 测试运行

```batch
cd ..\release\mms-offline-package-YYYYMMDD
initial_install.bat
```

应该只看到 echo 的内容，不会看到命令本身。

## 修复后的效果

### 修复前 ❌
```
E:\...\>setlocal enabledelayedexpansion
E:\...\>REM 切换到脚本所在目录
E:\...\>cd /d "E:\..."
E:\...\>echo ============================================================
============================================================
```

### 修复后 ✅
```
============================================================
MMS制造管理系统 - 全新安装脚本
============================================================

[1/4] 检查Docker环境...
[✓] Docker环境检查通过
```

## 已修复的文件

- ✅ `script/generate-install.ps1` - 修改为使用无BOM的UTF-8编码

## 注意事项

1. **必须重新运行导出脚本**才能生成修复后的安装包
2. 现有的安装包需要重新生成
3. `install-template.bat` 源文件也应该保存为无BOM的UTF-8
4. 如果手动编辑批处理文件，确保编辑器不会添加BOM

## VS Code 设置

在 VS Code 中编辑批处理文件时，可以：

1. 点击右下角的编码显示（如 "UTF-8 with BOM"）
2. 选择 "通过编码保存"
3. 选择 "UTF-8" （不要选 "UTF-8 with BOM"）

或者在 `.vscode/settings.json` 中添加：
```json
{
    "files.encoding": "utf8"
}
```

## 参考

- [UTF-8 BOM 问题详解](https://en.wikipedia.org/wiki/Byte_order_mark)
- Windows批处理文件最佳实践：第一行必须是 `@echo off`，文件编码必须无BOM

