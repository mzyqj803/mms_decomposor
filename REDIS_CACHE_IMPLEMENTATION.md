# Redis缓存实现说明

## 功能概述

本项目已实现零部件Redis缓存功能，在项目启动时将所有零部件列表加载到Redis缓存中，工艺分解时优先从缓存中查找零部件信息，提高查询性能。

## 实现的功能

### 1. 零部件缓存服务 (`ComponentCacheService`)

- **初始化缓存**: 项目启动时将数据库中所有零部件加载到Redis缓存
- **缓存查询**: 根据零部件编号从缓存中获取零部件信息
- **缓存存储**: 将零部件信息存储到Redis缓存
- **连接检查**: 检查Redis服务是否可用
- **缓存管理**: 清空缓存、获取缓存大小等管理功能

### 2. 项目启动时自动加载缓存

修改了 `MmsDecomposorApplication.java`，实现 `CommandLineRunner` 接口：
- 应用启动完成后自动初始化零部件缓存
- 如果Redis可用，将所有零部件加载到缓存
- 记录缓存加载的详细日志

### 3. 工艺分解服务优化

修改了 `BreakdownServiceImpl.java`：
- 新增 `getComponentByCode()` 方法，优先从Redis缓存获取零部件
- 如果缓存中没有，则从数据库获取并存储到缓存
- 更新所有零部件查询逻辑，使用缓存优先策略

### 4. 缓存测试接口

新增 `CacheTestController.java` 提供测试接口：
- `GET /api/cache/status` - 检查Redis连接状态和缓存大小
- `POST /api/cache/reload` - 重新加载缓存
- `DELETE /api/cache/clear` - 清空缓存
- `GET /api/cache/component/{componentCode}` - 测试从缓存获取零部件

## 技术实现细节

### 缓存键格式
```
component:{componentCode}
```

### 缓存值格式
零部件实体的JSON序列化字符串，包含所有字段信息。

### 缓存策略
- **永不过期**: 零部件信息相对稳定，设置为永不过期
- **优先缓存**: 查询时优先从缓存获取，缓存未命中时从数据库获取并更新缓存
- **容错处理**: Redis不可用时自动降级到数据库查询

### 序列化配置
使用Jackson ObjectMapper进行JSON序列化，支持Java 8时间类型。

## 使用方法

### 1. 启动项目
项目启动时会自动检测Redis连接并初始化缓存：
```bash
./start.bat
```

### 2. 测试缓存功能
使用提供的测试脚本：
```bash
./test-redis-cache.bat
```

### 3. 手动管理缓存
通过REST API接口：
```bash
# 检查缓存状态
curl -X GET "http://localhost:8080/api/cache/status"

# 重新加载缓存
curl -X POST "http://localhost:8080/api/cache/reload"

# 清空缓存
curl -X DELETE "http://localhost:8080/api/cache/clear"
```

## 性能优化效果

1. **查询性能提升**: 零部件查询从数据库查询优化为Redis内存查询
2. **减少数据库压力**: 减少对数据库的频繁查询
3. **提高响应速度**: 缓存查询响应时间显著降低
4. **容错机制**: Redis不可用时自动降级，不影响业务功能

## 注意事项

1. **Redis依赖**: 需要确保Redis服务正常运行
2. **内存使用**: 缓存所有零部件会占用一定Redis内存
3. **数据一致性**: 如果零部件数据有更新，需要重新加载缓存
4. **日志监控**: 关注缓存加载和查询的日志信息

## 配置要求

确保 `application.yml` 中Redis配置正确：
```yaml
spring:
  redis:
    host: redis
    port: 6379
    password: 
    database: 0

redisson:
  address: redis://redis:6379
  password: 
  database: 0
```
