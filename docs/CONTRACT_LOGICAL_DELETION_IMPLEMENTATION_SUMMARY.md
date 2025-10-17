# 合同逻辑删除功能实施总结

## 概述
根据用户需求，已将合同删除功能从物理删除改为逻辑删除（软删除），使用现有的 `status` 字段来表示删除状态，避免外键约束冲突，同时保留数据历史记录。

## 修改内容

### 1. 实体类修改

#### 1.1 Contracts实体类
- **文件**: `src/main/java/com/mms/entity/Contracts.java`
- **修改内容**:
  - 添加 `DELETED = 4` 状态常量
  - 更新 `getStatusText()` 和 `getStatusType()` 方法支持已删除状态
  - **注意**: 使用现有的 `status` 字段，不添加新的 `deleted` 字段

### 2. 数据库修改

#### 2.1 表结构
- **文件**: `src/main/resources/sql/data_init/schema.sql`
- **说明**: 使用现有的 `contracts` 表结构，无需添加新字段
- **状态值**: `status = 4` 表示已删除状态

### 3. Repository层修改

#### 3.1 ContractsRepository
- **文件**: `src/main/java/com/mms/repository/ContractsRepository.java`
- **修改内容**: 更新所有查询方法，添加 `AND c.status != 4` 条件
- **影响的查询方法**:
  - `findByContractNoContaining`
  - `findByProjectNameContaining`
  - `findByContractNoAndProjectNameContaining`
  - `findByContractNoOrProjectNameContaining`
  - `findByStatus`
  - `findByContractNoContainingAndStatus`
  - `findByProjectNameContainingAndStatus`
  - `findByContractNoAndProjectNameContainingAndStatus`

### 4. Service层修改

#### 4.1 ContractsService接口
- **文件**: `src/main/java/com/mms/service/ContractsService.java`
- **修改**: 将 `getContracts` 方法的 `status` 参数类型从 `Contracts.ContractStatus` 改为 `Integer`
- **新增**: `getContractByIdIncludeDeleted` 方法用于获取已删除的合同

#### 4.2 ContractsServiceImpl实现
- **文件**: `src/main/java/com/mms/service/impl/ContractsServiceImpl.java`
- **修改内容**:
  - 更新 `getContracts` 方法签名
  - 重写 `deleteContract` 方法为逻辑删除：
    ```java
    // 逻辑删除：将状态设置为已删除
    contract.setStatus(Contracts.ContractStatus.DELETED);
    contractsRepository.save(contract);
    ```
  - 更新 `getContractById` 方法检查删除状态
  - 新增 `getContractByIdIncludeDeleted` 方法

### 5. Controller层修改

#### 5.1 ContractsController
- **文件**: `src/main/java/com/mms/controller/ContractsController.java`
- **修改**: 将 `getContracts` 方法的 `status` 参数类型从 `Contracts.ContractStatus` 改为 `Integer`
- **新增**: `GET /contracts/{id}/include-deleted` 端点用于获取已删除的合同

## 功能特性

### 1. 逻辑删除机制
- 删除合同时不物理删除记录，而是将 `status` 设置为 `DELETED(4)`
- 保留所有相关数据的历史记录
- 避免外键约束冲突问题

### 2. 查询过滤
- 所有查询方法默认排除已删除的合同（`status != 4`）
- 已删除的合同不会在合同列表页面显示
- 搜索功能也不会返回已删除的合同

### 3. 操作拦截
- 所有对已删除合同的操作都会失败
- `getContractById` 方法会检查合同状态，已删除的合同抛出异常
- 提供 `getContractByIdIncludeDeleted` 方法用于显示已删除的合同

### 4. 状态管理
- 新增 `DELETED` 状态，状态文本为"已删除"，样式为 `danger`
- 处理中的合同仍然不能删除（保持原有业务逻辑）

## 验证结果

### 1. 编译验证
- ✅ 后端Java代码编译成功
- ✅ 无语法错误
- ⚠️ 存在3个类型安全警告（非本次修改引起）

### 2. 服务启动
- ✅ 后端服务重新启动成功
- ✅ 所有缓存初始化完成

## 注意事项

### 1. 数据库兼容性
- 使用现有的 `status` 字段，无需数据库迁移
- 现有数据库完全兼容

### 2. 前端兼容性
- 前端代码无需修改，API接口保持兼容
- 状态参数现在接受整数值（0-4）而不是枚举

### 3. 数据恢复
- 如需恢复已删除的合同，可以手动将 `status` 字段设置为适当的状态（0-3）

## 总结
通过实施基于 `status` 字段的逻辑删除功能，成功解决了合同删除时的外键约束冲突问题，同时保留了数据的历史记录。所有查询功能都会自动过滤已删除的合同，确保用户界面的清洁性。使用现有字段的设计更加简洁和兼容。
