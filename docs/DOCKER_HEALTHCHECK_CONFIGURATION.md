# Docker服务健康检查配置

## 概述

为了确保服务按正确顺序启动，并且每个服务都在其依赖项完全就绪后才启动，我们为所有Docker服务配置了健康检查（healthcheck）和依赖条件（depends_on with conditions）。

## 服务启动顺序

```
MariaDB (健康检查) → Redis (健康检查) → Backend (健康检查) → Frontend
```

### 启动流程

1. **MariaDB** 启动并执行所有初始化脚本
2. MariaDB 健康检查通过后，**Redis** 启动
3. Redis 健康检查通过后，**Backend** 启动
4. Backend 健康检查通过后，**Frontend** 启动

## 健康检查配置详解

### 1. MariaDB健康检查

```yaml
healthcheck:
  test: ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"]
  interval: 10s       # 每10秒检查一次
  timeout: 5s         # 检查超时时间
  retries: 30         # 最多重试30次
  start_period: 120s  # 启动后等待120秒再开始检查
```

**说明：**
- `healthcheck.sh --connect` 确保MariaDB可以接受连接
- `--innodb_initialized` 确保InnoDB引擎已完全初始化
- `start_period: 120s` 给予充足时间完成所有初始化脚本（约10分钟）
- `retries: 30` 意味着最多等待300秒（10s × 30）

**为什么需要120秒启动期？**
- 数据库初始化脚本包含10个SQL文件
- `02-data_init.sql` 导入数据约需6分钟
- `10-fix_duplicate_specs.sql` 清理重复数据约需3分钟
- 总计约10分钟，120秒的启动期确保在初始化期间不会误判为不健康

### 2. Redis健康检查

```yaml
healthcheck:
  test: ["CMD", "redis-cli", "ping"]
  interval: 5s        # 每5秒检查一次
  timeout: 3s         # 检查超时时间
  retries: 5          # 最多重试5次
  start_period: 5s    # 启动后等待5秒再开始检查
```

**说明：**
- Redis启动速度快，5秒启动期足够
- `redis-cli ping` 返回 `PONG` 表示服务正常

### 3. Backend健康检查

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 10s       # 每10秒检查一次
  timeout: 5s         # 检查超时时间
  retries: 5          # 最多重试5次
  start_period: 60s   # 启动后等待60秒再开始检查
```

**说明：**
- 使用Spring Boot Actuator的健康检查端点
- `start_period: 60s` 给予Spring Boot应用充足的启动时间
- `-f` 参数表示HTTP错误时返回非零退出码

**前提条件：**
- Spring Boot应用需要包含`spring-boot-starter-actuator`依赖
- 健康检查端点需要暴露（默认已暴露）

## 依赖关系配置

### Backend依赖配置

```yaml
depends_on:
  mariadb:
    condition: service_healthy  # 等待MariaDB健康检查通过
  redis:
    condition: service_healthy  # 等待Redis健康检查通过
```

**效果：**
- Backend容器不会启动，直到：
  - MariaDB完全初始化并通过健康检查
  - Redis启动并通过健康检查

### Frontend依赖配置

```yaml
depends_on:
  backend:
    condition: service_healthy  # 等待Backend健康检查通过
```

**效果：**
- Frontend容器不会启动，直到Backend完全启动并通过健康检查

## 健康检查状态

可以使用以下命令查看服务健康状态：

```bash
# 查看所有服务状态
docker-compose ps

# 查看特定服务的健康状态
docker inspect mms-mariadb --format='{{.State.Health.Status}}'
docker inspect mms-redis --format='{{.State.Health.Status}}'
docker inspect mms-backend --format='{{.State.Health.Status}}'

# 查看健康检查日志
docker inspect mms-mariadb --format='{{json .State.Health}}' | jq
```

**健康状态值：**
- `starting` - 正在启动，尚未进行健康检查
- `healthy` - 健康检查通过
- `unhealthy` - 健康检查失败

## 启动时间估算

### 全新部署（无现有数据卷）

| 服务 | 启动时间 | 说明 |
|------|---------|------|
| MariaDB | ~10分钟 | 包含数据初始化脚本执行时间 |
| Redis | ~5秒 | Redis启动速度很快 |
| Backend | ~30-60秒 | Spring Boot应用启动时间 |
| Frontend | ~5秒 | Nginx静态资源服务 |
| **总计** | **~12分钟** | 从`docker-compose up -d`到所有服务就绪 |

### 重启（数据卷已存在）

| 服务 | 启动时间 | 说明 |
|------|---------|------|
| MariaDB | ~10秒 | 无需执行初始化脚本 |
| Redis | ~5秒 | - |
| Backend | ~30-60秒 | - |
| Frontend | ~5秒 | - |
| **总计** | **~1-2分钟** | 快速重启 |

## 优势

### 1. 避免连接错误

**之前：**
```
Backend启动 → 尝试连接MariaDB → 失败（数据库还在初始化）
→ Backend重启 → 重试连接 → 可能再次失败
→ 多次重启后才成功
```

**现在：**
```
MariaDB完全初始化 → 健康检查通过 → Backend启动 → 成功连接
```

### 2. 确保数据完整性

- Backend只在所有数据库初始化脚本执行完成后才启动
- 包括重复数据清理、约束添加等关键步骤
- 避免Backend在数据不完整时启动

### 3. 减少不必要的重启

- 服务不会在依赖项未就绪时启动
- 减少因连接失败导致的容器重启
- 提高系统稳定性

### 4. 更清晰的启动状态

- 可以通过`docker-compose ps`清楚地看到每个服务的健康状态
- 更容易诊断启动问题

## 监控启动过程

### 实时查看服务状态

```bash
# 持续监控服务状态（每2秒刷新）
watch -n 2 'docker-compose ps'
```

### 查看MariaDB初始化日志

```bash
# 查看实时日志
docker logs -f mms-mariadb

# 查看初始化脚本执行情况
docker logs mms-mariadb 2>&1 | grep "docker-entrypoint-initdb.d"

# 查看清理脚本结果
docker logs mms-mariadb 2>&1 | grep "清理完成"
```

### 查看Backend启动日志

```bash
# 查看实时日志
docker logs -f mms-backend

# 查看数据库连接状态
docker logs mms-backend 2>&1 | grep "HikariPool"
```

## 故障排查

### MariaDB健康检查失败

**问题：** MariaDB一直处于`starting`或`unhealthy`状态

**排查步骤：**
1. 查看MariaDB日志
   ```bash
   docker logs mms-mariadb
   ```

2. 检查初始化脚本是否有错误
   ```bash
   docker logs mms-mariadb 2>&1 | grep -i error
   ```

3. 手动测试健康检查命令
   ```bash
   docker exec mms-mariadb healthcheck.sh --connect --innodb_initialized
   ```

### Backend健康检查失败

**问题：** Backend一直处于`starting`或`unhealthy`状态

**排查步骤：**
1. 查看Backend日志
   ```bash
   docker logs mms-backend
   ```

2. 检查Actuator端点是否可访问
   ```bash
   docker exec mms-backend curl -f http://localhost:8080/actuator/health
   ```

3. 确认Backend镜像包含`curl`命令
   ```bash
   docker exec mms-backend which curl
   ```

   如果没有curl，可以修改健康检查为：
   ```yaml
   test: ["CMD-SHELL", "wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1"]
   ```

### 服务启动时间过长

**问题：** MariaDB初始化时间超过预期

**解决方案：**
1. 增加`start_period`
   ```yaml
   start_period: 180s  # 增加到3分钟
   ```

2. 增加`retries`
   ```yaml
   retries: 50  # 增加重试次数
   ```

## 配置调优

### 开发环境

如果数据量小，可以减少启动等待时间：

```yaml
mariadb:
  healthcheck:
    start_period: 30s  # 减少到30秒
    retries: 20

backend:
  healthcheck:
    start_period: 30s
    retries: 3
```

### 生产环境

如果数据量大，建议增加等待时间：

```yaml
mariadb:
  healthcheck:
    start_period: 300s  # 增加到5分钟
    retries: 60         # 增加重试次数
    interval: 15s       # 增加检查间隔
```

## 最佳实践

1. **合理设置start_period**
   - 应该略大于服务正常启动所需时间
   - 避免在启动期间误判为不健康

2. **适当的检查间隔**
   - 数据库：10-15秒
   - 缓存：5秒
   - 应用：10秒

3. **监控健康状态**
   - 在生产环境中集成到监控系统
   - 设置告警通知

4. **日志记录**
   - 记录健康检查失败的原因
   - 便于后续分析和优化

## 参考资料

- [Docker Compose健康检查文档](https://docs.docker.com/compose/compose-file/#healthcheck)
- [MariaDB健康检查脚本](https://github.com/MariaDB/mariadb-docker/blob/master/healthcheck.sh)
- [Spring Boot Actuator文档](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 更新记录

- **2025-10-21**: 初始版本，添加所有服务的健康检查配置
  - MariaDB: 确保数据库完全初始化后才启动Backend
  - Redis: 确保缓存服务就绪
  - Backend: 确保应用完全启动后才启动Frontend
  - Frontend: 依赖Backend健康检查

