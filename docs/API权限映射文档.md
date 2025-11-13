# API权限映射文档

## 概述

本文档定义了系统中所有API端点与权限的映射关系。用户必须拥有相应的权限才能访问对应的API。

## 权限体系

### 权限命名规则

权限代码格式：`资源:操作`

- **资源（Resource）**：CONTRACT（合同）、CONTAINER（装箱单）、COMPONENT（零部件）、USER（用户）、ROLE（角色）、BREAKDOWN（工艺分解）
- **操作（Action）**：CREATE（创建）、UPDATE（编辑）、DELETE（删除）、VIEW（查看）、EXECUTE（执行）

### 权限列表

#### 合同相关权限
- `CONTRACT:CREATE` - 创建合同
- `CONTRACT:UPDATE` - 编辑合同
- `CONTRACT:DELETE` - 删除合同
- `CONTRACT:VIEW` - 查看合同

#### 装箱单相关权限
- `CONTAINER:CREATE` - 创建装箱单
- `CONTAINER:UPDATE` - 编辑装箱单
- `CONTAINER:DELETE` - 删除装箱单
- `CONTAINER:VIEW` - 查看装箱单

#### 工艺分解相关权限
- `BREAKDOWN:EXECUTE` - 执行工艺分解
- `BREAKDOWN:VIEW` - 查看工艺分解

#### 零部件相关权限
- `COMPONENT:CREATE` - 创建零部件
- `COMPONENT:UPDATE` - 编辑零部件
- `COMPONENT:DELETE` - 删除零部件
- `COMPONENT:VIEW` - 查看零部件

#### 用户相关权限
- `USER:CREATE` - 创建用户
- `USER:UPDATE` - 编辑用户
- `USER:DELETE` - 删除用户
- `USER:VIEW` - 查看用户

#### 角色相关权限
- `ROLE:CREATE` - 创建角色
- `ROLE:UPDATE` - 编辑角色
- `ROLE:DELETE` - 删除角色
- `ROLE:VIEW` - 查看角色

## API权限映射表

### 合同管理 API

| API端点 | HTTP方法 | 所需权限 | 说明 |
|---------|---------|---------|------|
| `/api/contracts` | GET | `CONTRACT:VIEW` | 获取合同列表 |
| `/api/contracts/{id}` | GET | `CONTRACT:VIEW` | 获取合同详情 |
| `/api/contracts` | POST | `CONTRACT:CREATE` | 创建合同 |
| `/api/contracts/{id}` | PUT | `CONTRACT:UPDATE` | 更新合同 |
| `/api/contracts/{id}` | DELETE | `CONTRACT:DELETE` | 删除合同 |
| `/api/contracts/search` | GET | `CONTRACT:VIEW` | 搜索合同 |
| `/api/contracts/{id}/generate-container` | POST | `CONTRACT:CREATE` | 生成装箱单 |
| `/api/contracts/{id}/start-breakdown` | POST | `BREAKDOWN:EXECUTE` | 开始工艺分解 |
| `/api/contracts/{id}/breakdown-result` | GET | `BREAKDOWN:VIEW` | 获取工艺分解结果 |
| `/api/contracts/{id}/export` | GET | `BREAKDOWN:VIEW` | 导出分解表 |

### 装箱单管理 API

| API端点 | HTTP方法 | 所需权限 | 说明 |
|---------|---------|---------|------|
| `/api/containers` | GET | `CONTAINER:VIEW` | 获取装箱单列表 |
| `/api/containers/{id}` | GET | `CONTAINER:VIEW` | 获取装箱单详情 |
| `/api/containers` | POST | `CONTAINER:CREATE` | 创建装箱单 |
| `/api/containers/{id}` | PUT | `CONTAINER:UPDATE` | 更新装箱单 |
| `/api/containers/{id}` | DELETE | `CONTAINER:DELETE` | 删除装箱单 |
| `/api/containers/upload` | POST | `CONTAINER:CREATE` | 上传装箱单文件 |

### 零部件管理 API

| API端点 | HTTP方法 | 所需权限 | 说明 |
|---------|---------|---------|------|
| `/api/components` | GET | `COMPONENT:VIEW` | 获取零部件列表 |
| `/api/components/{id}` | GET | `COMPONENT:VIEW` | 获取零部件详情 |
| `/api/components/{id}/detail` | GET | `COMPONENT:VIEW` | 获取零部件完整详情 |
| `/api/components` | POST | `COMPONENT:CREATE` | 创建零部件 |
| `/api/components/{id}` | PUT | `COMPONENT:UPDATE` | 更新零部件 |
| `/api/components/{id}` | DELETE | `COMPONENT:DELETE` | 删除零部件 |
| `/api/components/search` | GET | `COMPONENT:VIEW` | 搜索零部件 |
| `/api/components/categories` | GET | `COMPONENT:VIEW` | 获取零部件分类 |
| `/api/components/specs/{componentCode}` | GET | `COMPONENT:VIEW` | 获取零部件规格 |

### 工艺分解 API

| API端点 | HTTP方法 | 所需权限 | 说明 |
|---------|---------|---------|------|
| `/api/breakdown/{contractId}` | GET | `BREAKDOWN:VIEW` | 获取工艺分解结果 |
| `/api/breakdown/{contractId}/execute` | POST | `BREAKDOWN:EXECUTE` | 执行工艺分解 |
| `/api/breakdown/{contractId}/export` | GET | `BREAKDOWN:VIEW` | 导出分解结果 |

### 用户管理 API

| API端点 | HTTP方法 | 所需权限 | 说明 |
|---------|---------|---------|------|
| `/api/users` | GET | `USER:VIEW` | 获取用户列表 |
| `/api/users/{id}` | GET | `USER:VIEW` | 获取用户详情 |
| `/api/users` | POST | `USER:CREATE` | 创建用户 |
| `/api/users/{id}` | PUT | `USER:UPDATE` | 更新用户 |
| `/api/users/{id}` | DELETE | `USER:DELETE` | 删除用户 |
| `/api/users/{id}/toggle-enabled` | PUT | `USER:UPDATE` | 启用/禁用用户 |
| `/api/users/{id}/change-password` | PUT | `USER:UPDATE` | 修改密码 |
| `/api/users/{id}/reset-password` | PUT | `USER:UPDATE` | 重置密码 |

### 角色管理 API

| API端点 | HTTP方法 | 所需权限 | 说明 |
|---------|---------|---------|------|
| `/api/roles` | GET | `ROLE:VIEW` | 获取角色列表 |
| `/api/roles/{id}` | GET | `ROLE:VIEW` | 获取角色详情 |
| `/api/roles` | POST | `ROLE:CREATE` | 创建角色 |
| `/api/roles/{id}` | PUT | `ROLE:UPDATE` | 更新角色 |
| `/api/roles/{id}` | DELETE | `ROLE:DELETE` | 删除角色 |
| `/api/roles/{id}/permissions` | GET | `ROLE:VIEW` | 获取角色权限 |
| `/api/roles/{id}/permissions` | PUT | `ROLE:UPDATE` | 更新角色权限 |

### 认证相关 API（无需权限）

| API端点 | HTTP方法 | 所需权限 | 说明 |
|---------|---------|---------|------|
| `/api/auth/login` | POST | 无（公开） | 用户登录 |
| `/api/auth/logout` | POST | 无（需登录） | 用户登出 |
| `/api/auth/me` | GET | 无（需登录） | 获取当前用户信息 |

## 权限检查机制

### 实现方式

1. **方法级权限控制**
   - 使用 `@PreAuthorize` 注解在Controller方法上
   - 示例：`@PreAuthorize("hasAuthority('CONTRACT:CREATE')")`

2. **全局权限拦截**
   - 在Security配置中定义URL模式与权限的映射
   - 使用 `hasAuthority()` 或 `hasAnyAuthority()` 进行权限检查

3. **动态权限验证**
   - 在Service层进行业务逻辑权限验证
   - 根据用户角色和权限动态判断

### 权限验证流程

```
用户请求 → JWT验证 → 获取用户角色 → 获取角色权限 → 检查权限 → 允许/拒绝
```

## 默认角色权限配置

### 管理员角色（ADMIN）
拥有所有权限：
- ✅ 所有 `CONTRACT:*` 权限
- ✅ 所有 `CONTAINER:*` 权限
- ✅ 所有 `BREAKDOWN:*` 权限
- ✅ 所有 `COMPONENT:*` 权限
- ✅ 所有 `USER:*` 权限
- ✅ 所有 `ROLE:*` 权限

### 普通用户角色（USER）
基础权限：
- ✅ `CONTRACT:VIEW` - 查看合同
- ✅ `CONTAINER:VIEW` - 查看装箱单
- ✅ `BREAKDOWN:VIEW` - 查看工艺分解
- ✅ `COMPONENT:VIEW` - 查看零部件
- ❌ 无创建、编辑、删除权限
- ❌ 无用户和角色管理权限

## 权限继承规则

1. **角色继承**：用户通过角色获得权限
2. **权限叠加**：用户拥有多个角色时，权限会叠加
3. **权限优先级**：拒绝权限优先于允许权限

## 注意事项

1. **API版本控制**：权限代码与API版本无关，保持向后兼容
2. **权限粒度**：权限控制到操作级别，不控制到数据级别
3. **性能考虑**：权限检查应在认证后立即进行，避免不必要的数据库查询
4. **错误处理**：权限不足时返回403 Forbidden，不泄露权限信息

## 更新日志

- 2024-11-13: 初始版本，定义基础权限和API映射


