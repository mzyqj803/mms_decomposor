# Spring Boot 3.x + Java 21 升级指南

## 🎉 升级完成

项目已成功从 Spring Boot 2.7.18 + Java 11 升级到 **Spring Boot 3.2.0 + Java 21**！

## 📋 升级内容

### 1. Java 版本升级
- **从**: Java 11
- **到**: Java 21
- **变更**: 更新了 `pom.xml` 中的 `maven.compiler.source` 和 `maven.compiler.target`

### 2. Spring Boot 版本升级
- **从**: Spring Boot 2.7.18
- **到**: Spring Boot 3.2.0
- **变更**: 更新了 `spring-boot.version` 属性

### 3. 依赖版本更新
- **MariaDB**: 3.3.2 → 3.3.3
- **Redisson**: 3.24.3 → 3.25.2
- **Apache POI**: 5.2.4 → 5.2.5
- **iText**: 9.3.0 → 8.0.2 (修复了重定位警告)
- **Maven Compiler Plugin**: 3.1 → 3.11.0 (支持Java 21)

### 4. Jakarta EE 迁移
所有 `javax.*` 包已迁移到 `jakarta.*`：
- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.validation.*` → `jakarta.validation.*`

### 5. Spring Security 配置更新
- **从**: `WebSecurityConfigurerAdapter` (已废弃)
- **到**: `SecurityFilterChain` Bean 配置
- **变更**: 使用新的 Lambda DSL 配置方式

## 🔧 环境要求

### Java 环境
- **JDK**: Java 21+
- **JAVA_HOME**: 需要设置为 Java 21 路径

### Maven 环境
- **Maven**: 3.8.6+
- **编译器插件**: 3.11.0+ (支持Java 21)

## 🚀 启动方式

### Windows 环境
```bash
# 设置Java 21环境变量
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# 启动项目
script\start.bat
```

### Linux/Mac 环境
```bash
# 设置Java 21环境变量
export JAVA_HOME=/path/to/java21

# 启动项目
./start.sh
```

## ⚠️ 注意事项

### 1. Java 版本兼容性
- 确保系统安装的是 Java 21
- Maven 必须使用 Java 21 进行编译

### 2. 数据库兼容性
- MariaDB 3.3.3 完全兼容
- 现有数据库结构无需修改

### 3. Redis 兼容性
- Redisson 3.25.2 完全兼容
- 现有缓存数据无需清理

### 4. 前端兼容性
- Vue 3 前端无需修改
- API 接口保持兼容

## 🐛 常见问题

### Q: 编译时提示"无效的目标发行版: 21"
**A**: 检查 JAVA_HOME 环境变量是否设置为 Java 21 路径

### Q: Maven 使用错误的 Java 版本
**A**: 设置环境变量：
```bash
# Windows
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Linux/Mac
export JAVA_HOME=/path/to/java21
```

### Q: Spring Security 配置错误
**A**: 已更新为 Spring Boot 3.x 兼容的配置方式

## 📊 性能提升

### Java 21 新特性
- **虚拟线程**: 提高并发性能
- **模式匹配**: 简化代码逻辑
- **记录类**: 减少样板代码
- **性能优化**: 更快的启动时间和更低的内存占用

### Spring Boot 3.x 改进
- **原生镜像支持**: 更快的启动时间
- **改进的监控**: 更好的可观测性
- **性能优化**: 整体性能提升

## 🔄 回滚方案

如果需要回滚到旧版本：

1. 恢复 `pom.xml` 中的版本配置
2. 将所有 `jakarta.*` 改回 `javax.*`
3. 恢复 `SecurityConfig` 为 `WebSecurityConfigurerAdapter` 方式
4. 使用 Java 11 重新编译

## 📝 升级检查清单

- [x] Java 版本升级到 21
- [x] Spring Boot 升级到 3.2.0
- [x] 所有依赖版本更新
- [x] Jakarta EE 迁移完成
- [x] Spring Security 配置更新
- [x] 编译测试通过
- [x] 打包测试通过
- [x] 环境变量配置正确

## 🎯 下一步

1. **测试运行**: 启动应用程序并测试所有功能
2. **性能测试**: 验证性能提升效果
3. **监控配置**: 配置新的监控和日志系统
4. **文档更新**: 更新开发文档和部署指南

---

**升级完成时间**: 2025-09-22  
**升级版本**: Spring Boot 3.2.0 + Java 21  
**状态**: ✅ 成功

