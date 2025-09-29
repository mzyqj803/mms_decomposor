# 工件装配类型查询API文档

## 概述

本模块提供查询工件装配类型的API接口，通过查询 `components_spec` 表中 `spec_code='programCode'` 的规格数据，来判断工件是产线装配紧固件还是仓库装箱紧固件。

## 数据库视图

系统创建了两个视图来优化查询性能：

1. **components_fastener_assembled** - 产线装配紧固件视图
   - 包含 `component_id` 列
   - 筛选条件：`spec_code='programCode' AND spec_value='产线装配'`

2. **components_fastener_unassembled** - 仓库装箱紧固件视图  
   - 包含 `component_id` 列
   - 筛选条件：`spec_code='programCode' AND spec_value='仓库装箱'`

## API接口

### 1. 获取工件的装配类型

**接口地址：** `GET /api/components/fastener/type/{componentId}`

**参数：**
- `componentId` (Long) - 工件ID

**响应示例：**
```json
{
    "componentId": 123,
    "assemblyType": "产线装配",
    "componentInfo": {
        "componentCode": "FT001",
        "name": "紧固件A",
        "categoryCode": "FASTENER"
    }
}
```

**装配类型值：**
- `"产线装配"` - 产线装配紧固件
- `"仓库装箱"` - 仓库装箱紧固件  
- `null` - 无装配信息

### 2. 获取所有紧固件装配类型分类

**接口地址：** `GET /api/components/fastener/types`

**响应示例：**
```json
{
    "assembledFasteners": [
        {
            "componentId": 123,
            "componentCode": "FT001",
            "name": "紧固件A",
            "assemblyType": "产线装配"
        }
    ],
    "unassembledFasteners": [
        {
            "componentId": 124,
            "componentCode": "FT002", 
            "name": "紧固件B",
            "assemblyType": "仓库装箱"
        }
    ],
    "assembledCount": 1,
    "unassembledCount": 1
}
```

### 3. 检查工件是否为产线装配紧固件

**接口地址：** `GET /api/components/fastener/assembled/{componentId}`

**参数：**
- `componentId` (Long) - 工件ID

**响应：** `true` 或 `false`

### 4. 检查工件是否为仓库装箱紧固件

**接口地址：** `GET /api/components/fastener/unassembled/{componentId}`

**参数：**
- `componentId` (Long) - 工件ID

**响应：** `true` 或 `false`

## 使用示例

### JavaScript客户端示例

```javascript
// 检查工件装配类型
async function getComponentAssemblyType(componentId) {
    try {
        const response = await fetch(`/api/components/fastener/type/${componentId}`);
        const data = await response.json();
        console.log(`工件${componentId}装配类型:`, data.assemblyType);
        return data;
    } catch (error) {
        console.error('查询失败:', error);
    }
}

// 获取所有紧固件分类
async function getAllFastenerTypes() {
    try {
        const response = await fetch('/api/components/fastener/types');
        const data = await response.json();
        console.log('产线装配紧固件:', data.assembledCount, '个');
        console.log('仓库装箱紧固件:', data.unassembledCount, '个');
        return data;
    } catch (error) {
        console.error('查询失败:', error);
    }
}
```

## 错误处理

- **404 Not Found** - 工件不存在
- **500 Internal Server Error** - 服务器内部错误

## 缓存机制

API使用了缓存机制来提高查询性能：

- 装配类型查询：缓存15分钟
- 紧固件列表：缓存10分钟

## 注意事项

1. 如果工件在 `components_spec` 表中不存在 `spec_code='programCode'` 的记录，`assemblyType` 将返回 `null`
2. 同一个工件ID不能在产线装配和仓库装箱中同时存在
3. API支持缓存，提高查询性能
