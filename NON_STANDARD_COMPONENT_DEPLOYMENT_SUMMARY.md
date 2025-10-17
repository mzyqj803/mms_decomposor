# 非标组件自动生成功能 - Docker 部署总结

## 部署信息

**部署时间**: 2025-10-17 10:53  
**部署方式**: Docker Compose  
**部署状态**: ✅ 成功

## 功能概述

在工艺分解过程中，当遇到包含 `~` 符号的组件代码（如 `TTA0E104002~AA79375`）时，系统会自动：
1. 检测非标组件代码
2. 提取基础组件代码（`TTA0E104002`）
3. 基于基础组件自动创建非标组件
4. 复制所有属性、规格和关系
5. 添加非标标记（`nonStandardPartFlag=1`）

## 部署流程

### 1. 停止并清理现有服务
```bash
docker-compose down
docker rmi mms-backend:latest mms-frontend:latest
docker system prune -f
```

### 2. 重新构建应用
- **前端**: `npm install` → `npm run build`
- **后端**: `mvn clean package -DskipTests`

### 3. 构建 Docker 镜像
- **后端镜像**: `docker build -f Dockerfile.backend -t mms-backend:latest .`
- **前端镜像**: `docker build -f frontend/Dockerfile -t mms-frontend:latest frontend/`

### 4. 启动所有服务
```bash
docker-compose up -d
```

## 服务状态

### 运行中的容器

| 容器名称 | 镜像 | 端口映射 | 状态 |
|---------|------|----------|------|
| mms-frontend | mms-frontend:latest | 9000:80 | ✅ Running |
| mms-backend | mms-backend:latest | 8080:8080 | ✅ Running |
| mms-mariadb | mariadb:11 | 3307:3306 | ✅ Running |
| mms-redis | redis:6.0-alpine | 6379:6379 | ✅ Running |

### 服务地址

- **前端应用**: http://localhost:9000
- **后端API**: http://localhost:8080/api
- **数据库**: localhost:3307
  - 用户名: mms_user
  - 密码: mms_password
  - 数据库: mms_db
- **Redis缓存**: localhost:6379

## 后端服务启动日志

```
✅ 零部件缓存初始化成功，共缓存 10741 个零部件
✅ 紧固件缓存初始化成功，共缓存 1161 个紧固件状态
✅ 所有缓存初始化完成
```

## 代码修改

### 修改的文件
1. `src/main/java/com/mms/service/impl/BreakdownServiceImpl.java`
   - 添加 `ComponentsSpecRepository` 依赖
   - 修改 `getComponentByCode()` 方法，检测 `~` 符号
   - 新增 `getOrCreateNonStandardComponent()` 方法

### 新增的文件
1. `NON_STANDARD_COMPONENT_AUTO_CREATION_FEATURE_SUMMARY.md` - 功能实现总结
2. `NON_STANDARD_COMPONENT_TESTING_GUIDE.md` - 测试指南
3. `NON_STANDARD_COMPONENT_DEPLOYMENT_SUMMARY.md` - 部署总结（本文件）

## 测试建议

### 1. 功能测试

在数据库中插入测试数据：
```sql
INSERT INTO container_components 
(Container_ID, Component_No, Component_Name, Quantity, Entry_User, Last_Update_User)
VALUES 
(1, 'TTA0E104002~AA79375', 'Test Non-Standard Component', 5, 'TEST_USER', 'TEST_USER');
```

执行工艺分解：
```bash
curl -X POST http://localhost:8080/api/breakdown/container/1
```

验证结果：
```sql
-- 检查非标组件是否创建
SELECT * FROM components WHERE component_code = 'TTA0E104002~AA79375';

-- 检查非标标记
SELECT * FROM components_spec 
WHERE component_id = (SELECT id FROM components WHERE component_code = 'TTA0E104002~AA79375')
AND spec_code = 'nonStandardPartFlag';
```

### 2. 日志验证

查看后端日志，应包含以下关键信息：
```bash
docker-compose logs -f backend | grep "非标组件"
```

预期日志：
```
INFO  检测到非标组件代码: TTA0E104002~AA79375
INFO  提取基础组件代码: TTA0E104002
INFO  找到基础组件: TTA0E104002, name=...
INFO  创建非标组件成功: id=..., componentCode=TTA0E104002~AA79375
INFO  复制基础组件规格完成: 共X条
INFO  添加非标组件标记成功
INFO  复制基础组件关系完成: 共X条
INFO  非标组件创建完成: componentCode=TTA0E104002~AA79375, baseComponentCode=TTA0E104002
```

## 管理命令

### 查看服务状态
```bash
docker-compose ps
```

### 查看服务日志
```bash
# 所有服务
docker-compose logs -f

# 特定服务
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mariadb
docker-compose logs -f redis
```

### 重启服务
```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend
docker-compose restart frontend
```

### 停止服务
```bash
docker-compose down
```

### 重新部署（不重新构建镜像）
```bash
docker-compose down
docker-compose up -d
```

### 完整重新构建和部署
```bash
.\rebuild-deploy-complete.bat
```

## 数据库表结构影响

### components 表
- 自动创建的非标组件记录

### components_spec 表
- 复制的规格记录
- 新增的非标标记记录：`nonStandardPartFlag=1`

### components_relationship 表
- 复制的子组件关系记录

## 性能指标

- **容器启动时间**: < 10秒
- **缓存初始化**: 
  - 零部件缓存: 10741 个
  - 紧固件缓存: 1161 个
- **镜像大小**:
  - 后端镜像: ~400MB
  - 前端镜像: ~20MB

## 故障排查

### 1. 服务无法启动

检查 Docker Desktop 是否运行：
```bash
docker --version
docker-compose --version
```

检查端口占用：
```bash
# Windows
netstat -ano | findstr "8080"
netstat -ano | findstr "9000"
netstat -ano | findstr "3307"
netstat -ano | findstr "6379"
```

### 2. 数据库连接失败

查看数据库日志：
```bash
docker-compose logs mariadb
```

检查数据库是否就绪：
```bash
docker-compose exec mariadb mysql -umms_user -pmms_password -e "SELECT 1"
```

### 3. Redis 连接失败

查看 Redis 日志：
```bash
docker-compose logs redis
```

测试 Redis 连接：
```bash
docker-compose exec redis redis-cli ping
```

### 4. 后端启动失败

查看完整的后端日志：
```bash
docker-compose logs backend --tail=200
```

进入容器查看：
```bash
docker-compose exec backend sh
```

## 回滚方案

如果新功能出现问题，可以回滚到之前的版本：

```bash
# 1. 停止服务
docker-compose down

# 2. 从 Git 恢复代码
git checkout <previous-commit>

# 3. 重新构建和部署
.\rebuild-deploy-complete.bat
```

## 监控和维护

### 日志管理

日志位置：
- 后端日志: 容器内的标准输出
- 前端日志: Nginx 访问日志和错误日志

持续监控：
```bash
docker-compose logs -f --tail=100
```

### 数据备份

定期备份数据库：
```bash
docker-compose exec mariadb mysqldump -umms_user -pmms_password mms_db > backup_$(date +%Y%m%d).sql
```

### 容器健康检查

```bash
# 查看容器资源使用情况
docker stats

# 查看容器详细信息
docker-compose exec backend sh -c "ps aux"
```

## 后续优化建议

1. **性能优化**
   - 添加健康检查配置
   - 优化 JVM 内存参数
   - 使用多阶段构建减小镜像大小

2. **监控增强**
   - 集成 Prometheus + Grafana
   - 添加应用性能监控（APM）
   - 配置告警规则

3. **安全加固**
   - 使用 secrets 管理敏感信息
   - 配置网络隔离
   - 定期更新基础镜像

4. **CI/CD 集成**
   - 自动化构建流程
   - 自动化测试
   - 自动化部署

## 联系信息

如有问题，请查看：
- 功能文档: `NON_STANDARD_COMPONENT_AUTO_CREATION_FEATURE_SUMMARY.md`
- 测试指南: `NON_STANDARD_COMPONENT_TESTING_GUIDE.md`
- 项目文档: `PROJECT_SUMMARY.md`

## 版本信息

- **功能版本**: 1.0
- **部署日期**: 2025-10-17
- **部署人**: 系统管理员
- **Git Commit**: [待填写]

