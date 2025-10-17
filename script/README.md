# MMS 制造管理系统 - Windows 批处理脚本

本目录包含了所有用于管理和运行 MMS 制造管理系统的 Windows 批处理脚本。

## 📋 脚本列表

### 启动和停止脚本

| 脚本名称 | 功能描述 | 使用场景 |
|---------|---------|---------|
| `start.bat` | 一键启动生产环境 | 正式部署使用 |
| `stop.bat` | 停止所有服务 | 停止系统 |
| `restart.bat` | 重启系统 | 重启服务 |
| `dev-start.bat` | 开发环境启动 | 开发调试使用 |
| `stop-dev.bat` | 停止开发环境 | 停止开发服务 |
| `start-debug.bat` | 调试模式启动 | 调试和问题排查 |

### 构建和部署脚本

| 脚本名称 | 功能描述 |
|---------|---------|
| `rebuild-deploy.bat` | 重新编译并部署 |
| `rebuild-deploy-complete.bat` | 完整重新构建并部署（删除旧镜像） |

### 管理和维护脚本

| 脚本名称 | 功能描述 |
|---------|---------|
| `status.bat` | 检查系统状态 |
| `logs.bat` | 查看服务日志 |
| `clean.bat` | 清理系统资源 |
| `check-env.bat` | 检查环境依赖 |

### 测试脚本

| 脚本名称 | 功能描述 |
|---------|---------|
| `test-commands.bat` | 运行测试命令 |
| `test-redis-cache.bat` | 测试Redis缓存功能 |

## 🚀 快速开始

### 方式一：从项目根目录运行（推荐）

```cmd
# 在项目根目录下执行
script\start.bat
script\stop.bat
script\restart.bat
```

### 方式二：双击运行

直接双击 `script` 目录下的脚本文件即可运行。脚本会自动切换到项目根目录。

### 方式三：在 PowerShell 中运行

```powershell
# 在项目根目录下执行
.\script\start.bat

# 或者先进入script目录
cd script
.\start.bat
cd ..
```

## 💡 常用命令示例

### 启动系统

```cmd
# 生产环境启动
script\start.bat

# 开发环境启动（支持热重载）
script\dev-start.bat

# 调试模式启动
script\start-debug.bat
```

### 停止和重启

```cmd
# 停止系统
script\stop.bat

# 重启系统
script\restart.bat
```

### 检查和维护

```cmd
# 检查环境依赖
script\check-env.bat

# 查看服务状态
script\status.bat

# 查看日志
script\logs.bat

# 清理系统
script\clean.bat
```

### 重新部署

```cmd
# 重新编译并部署
script\rebuild-deploy.bat

# 完整重新构建（删除旧镜像）
script\rebuild-deploy-complete.bat
```

## ⚙️ 脚本功能详解

### start.bat - 生产环境启动

**功能：**
- ✅ 自动检查 Java、Maven、Node.js、Docker 环境
- ✅ 自动构建后端应用 (Maven clean package)
- ✅ 自动安装前端依赖 (npm install)
- ✅ 自动构建前端应用 (npm run build)
- ✅ 自动启动 Docker 容器
- ✅ 自动打开浏览器访问应用

**使用场景：** 首次启动或生产环境部署

### dev-start.bat - 开发环境启动

**功能：**
- ✅ 启动数据库和Redis容器
- ✅ 启动后端开发服务器（支持热重载）
- ✅ 启动前端开发服务器（支持热重载）

**使用场景：** 开发调试

**优势：**
- 代码修改后自动重新加载
- 不需要重新构建Docker镜像
- 开发效率更高

### start-debug.bat - 调试模式启动

**功能：**
- ✅ 详细的输出信息
- ✅ 支持 `--verbose` 参数显示更多信息
- ✅ 支持 `--skip-build` 参数跳过构建

**使用示例：**
```cmd
# 基本调试
script\start-debug.bat

# 详细输出
script\start-debug.bat --verbose

# 跳过构建直接启动
script\start-debug.bat --skip-build
```

### rebuild-deploy.bat vs rebuild-deploy-complete.bat

| 功能 | rebuild-deploy.bat | rebuild-deploy-complete.bat |
|-----|-------------------|---------------------------|
| 停止服务 | ✅ | ✅ |
| 删除旧镜像 | ❌ | ✅ |
| 重新构建后端 | ✅ | ✅ |
| 重新构建前端 | ✅ | ✅ |
| 启动服务 | ✅ | ✅ |
| 清理Docker系统 | 部分 | 完全 |
| 执行时间 | 较快 | 较慢 |

**使用建议：**
- 普通更新：使用 `rebuild-deploy.bat`
- 重大更新或问题排查：使用 `rebuild-deploy-complete.bat`

### clean.bat - 清理系统

提供三种清理级别：

1. **轻度清理** - 仅删除容器
2. **中度清理** - 删除容器和镜像
3. **重度清理** - 删除所有数据（⚠️ 谨慎使用）

## 📝 技术说明

### 脚本内部机制

所有脚本都会在开头执行以下代码：

```batch
REM 切换到项目根目录
cd /d "%~dp0.."
```

这确保了无论从哪里运行脚本，都能正确找到项目文件。

### 路径说明

- `%~dp0` - 获取脚本所在的驱动器和路径
- `%~dp0..` - 获取脚本所在目录的父目录（项目根目录）
- `cd /d` - 切换驱动器和目录

## 🔧 故障排查

### 脚本无法运行

1. **检查执行策略（PowerShell）**
   ```powershell
   # 查看当前策略
   Get-ExecutionPolicy
   
   # 设置为允许本地脚本
   Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

2. **使用管理员权限**
   - 右键点击脚本 → "以管理员身份运行"

3. **检查文件编码**
   - 确保脚本文件使用 UTF-8 或 GBK 编码
   - 避免使用 UTF-8 BOM

### 环境检查失败

运行环境检查脚本：
```cmd
script\check-env.bat
```

根据提示安装缺失的依赖。

### Docker相关错误

1. **确保 Docker Desktop 正在运行**
2. **检查 Docker 服务状态**
   ```cmd
   docker --version
   docker-compose --version
   ```

## 🔗 相关文档

- [项目README](../README.md) - 项目整体说明
- [Windows环境配置指南](../docs/WINDOWS_SETUP.md) - Windows环境配置
- [Windows服务器部署指南](../docs/Windows_Server_Deployment_Guide.md) - 服务器部署
- [脚本目录重组总结](../docs/SCRIPT_DIRECTORY_REORGANIZATION_SUMMARY.md) - 本次重组详情

## 📞 支持

如有问题，请参考以上文档或联系系统管理员。

---

**最后更新**: 2025年10月17日  
**维护者**: MMS开发团队

