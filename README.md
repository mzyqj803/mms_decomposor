# MMS制造管理系统 (Manufacturing Management System)

## 项目简介

MMS制造管理系统是一个专为电梯制造行业设计的工艺分解和管理系统。系统能够根据合同和装箱单数据，自动生成工艺分解表，并提供完整的生产计划、成本估算和投标报价功能。

## 📋 项目状态

- ✅ **项目架构**: 前后端分离架构已完成
- ✅ **后端框架**: Spring Boot 3.2.0 + Java 21
- ✅ **前端框架**: Vue 3 + Element Plus
- ✅ **数据库设计**: 完整的10表数据模型
- ✅ **容器化部署**: Docker + Docker Compose
- ✅ **系统设计文档**: 完整的技术文档
- ✅ **基础CRUD功能**: 合同、零部件、装箱单管理
- ✅ **用户认证系统**: JWT认证框架
- ✅ **缓存系统**: Redis + Redisson分布式锁
- ✅ **文件上传**: Excel装箱单上传和预览
- ✅ **工艺分解算法**: 核心分解逻辑已实现
- 🔄 **生产计划**: 基于工艺工序的生产计划生成
- 🔄 **成本估算**: 零部件和工艺成本计算
- 🔄 **投标报价**: 利润率设置和价格生成
- 🔄 **历史记录**: 数据变更追踪

## 技术栈

### 后端
- **Spring Boot 3.2.0** - 主框架
- **Java 21** - 编程语言
- **Spring Data JPA** - 数据访问层
- **Spring Security** - 安全框架
- **MariaDB** - 主数据库
- **Redis** - 缓存和分布式锁
- **Redisson** - Redis客户端
- **Apache POI** - Excel文件处理
- **iText** - PDF文件生成
- **Lombok** - 代码简化

### 前端
- **Vue 3** - 前端框架
- **Vite** - 构建工具
- **Element Plus** - UI组件库
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Axios** - HTTP客户端
- **ECharts** - 图表库
- **Sass** - CSS预处理器

## 主要功能

### 1. 合同管理 ✅
- ✅ 合同信息录入和管理
- ✅ 合同参数配置
- ✅ 装箱单自动生成
- ✅ 合同状态跟踪
- ✅ 合同搜索和分页

### 2. 零部件管理 ✅
- ✅ 零部件基础信息维护
- ✅ 零部件规格参数管理
- ✅ 零部件关系配置
- ✅ 零部件搜索和分页
- ✅ 零部件缓存优化

### 3. 装箱单管理 ✅
- ✅ Excel装箱单文件上传
- ✅ 装箱单内容预览
- ✅ 装箱单CRUD操作
- ✅ 装箱单搜索和分页
- ✅ 装箱单与合同关联

### 4. 工艺分解 ✅
- ✅ 自动工艺分解算法
- ✅ 单箱包分解功能
- ✅ 合同级批量分解
- ✅ 分解结果查看
- ✅ 分解表导出功能

### 5. 用户认证 ✅
- ✅ JWT Token认证
- ✅ 用户登录/登出
- ✅ 用户信息管理
- ✅ 安全配置

### 6. 缓存系统 ✅
- ✅ Redis分布式缓存
- ✅ Redisson分布式锁
- ✅ 缓存测试接口
- ✅ 性能优化

### 7. 生产计划 🔄
- 🔄 基于工艺工序的最优制造流程
- 🔄 生产计划表生成
- 🔄 计划调整和优化

### 8. 成本估算 🔄
- 🔄 零部件成本计算
- 🔄 工艺成本分析
- 🔄 总成本估算

### 9. 投标报价 🔄
- 🔄 利润率设置
- 🔄 营销成本配置
- 🔄 代理商佣金计算
- 🔄 投标价格生成

### 10. 历史记录 🔄
- 🔄 合同参数修订记录
- 🔄 装箱单修改记录
- 🔄 合并分解表修改记录

### 11. 数据接口 ✅
- ✅ RESTful API
- ✅ 分页和搜索支持
- ✅ 数据验证
- 🔄 外部系统集成
- 🔄 回调更新机制

## 项目结构

```
mms_decomposor/
├── src/main/java/com/mms/
│   ├── MmsDecomposorApplication.java    # 主应用类
│   ├── config/                         # 配置类
│   │   ├── JpaConfig.java
│   │   ├── RedisConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/                     # 控制器
│   │   ├── AuthController.java
│   │   └── ContractsController.java
│   ├── entity/                         # 实体类
│   │   ├── BaseEntity.java
│   │   ├── Components.java
│   │   ├── Contracts.java
│   │   └── ...
│   ├── repository/                     # 数据访问层
│   │   ├── ComponentsRepository.java
│   │   └── ContractsRepository.java
│   └── service/                        # 服务层
│       ├── CacheService.java
│       ├── DistributedLockService.java
│       ├── ContractsService.java
│       └── impl/
│           └── ContractsServiceImpl.java
├── src/main/resources/
│   ├── application.yml                 # 应用配置
│   └── sql/
│       └── init.sql                    # 数据库初始化脚本
├── frontend/                           # 前端项目
│   ├── src/
│   │   ├── api/                        # API接口
│   │   ├── components/                 # Vue组件
│   │   ├── layouts/                    # 布局组件
│   │   ├── router/                     # 路由配置
│   │   ├── stores/                     # 状态管理
│   │   ├── styles/                     # 样式文件
│   │   └── views/                      # 页面组件
│   ├── package.json
│   └── vite.config.js
├── docs/                               # 文档
│   ├── 系统设计文档.md                  # 系统设计文档
│   ├── Spec.md                         # 需求规格说明
│   ├── Data Modeling.md                # 数据模型设计
│   ├── DBER.drawio                     # 数据库ER图
│   └── data_init/                      # 数据初始化脚本
└── pom.xml                            # Maven配置
```

## 快速开始

### 环境要求
- **JDK 21+** (推荐使用 OpenJDK 21)
- Maven 3.8.6+
- Node.js 16+
- Docker & Docker Compose
- Git

### Windows环境启动 (推荐)

1. **克隆项目**
```cmd
git clone <repository-url>
cd mms_decomposor
```

2. **一键启动**
```cmd
# 双击运行或在命令行执行
start.bat
```

3. **访问应用**
- 前端: http://localhost:9000
- 后端API: http://localhost:8080

### Linux/Mac环境启动

1. **克隆项目**
```bash
git clone <repository-url>
cd mms_decomposor
```

2. **一键启动**
```bash
chmod +x start.sh
./start.sh
```

3. **访问应用**
- 前端: http://localhost:9000
- 后端API: http://localhost:8080

### 开发环境启动

#### Windows开发环境
```cmd
# 开发模式启动，支持热重载
dev-start.bat
```

#### Linux/Mac开发环境
```bash
# 启动基础服务
docker-compose up -d mariadb redis

# 启动后端开发服务器
mvn spring-boot:run

# 启动前端开发服务器 (新终端)
cd frontend
npm run dev
```

### 手动启动 (不推荐)

如果需要手动启动各个组件，请参考 [WINDOWS_SETUP.md](WINDOWS_SETUP.md) 详细说明。

## API文档

### 认证接口
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出
- `GET /auth/me` - 获取当前用户信息

### 合同管理接口
- `GET /contracts` - 获取合同列表（支持分页和搜索）
- `GET /contracts/{id}` - 获取合同详情
- `POST /contracts` - 创建合同
- `PUT /contracts/{id}` - 更新合同
- `DELETE /contracts/{id}` - 删除合同

### 零部件管理接口
- `GET /components` - 获取零部件列表（支持分页和搜索）
- `GET /components/{id}` - 获取零部件详情
- `POST /components` - 创建零部件
- `PUT /components/{id}` - 更新零部件
- `DELETE /components/{id}` - 删除零部件

### 装箱单管理接口
- `GET /containers` - 获取装箱单列表（支持分页和搜索）
- `GET /containers/{id}` - 获取装箱单详情
- `POST /containers` - 创建装箱单
- `PUT /containers/{id}` - 更新装箱单
- `DELETE /containers/{id}` - 删除装箱单
- `POST /containers/upload` - 上传Excel装箱单文件
- `GET /containers/{id}/preview` - 预览装箱单内容

### 工艺分解接口
- `POST /breakdown/container/{containerId}` - 对单个箱包进行工艺分解
- `POST /breakdown/contract/{contractId}` - 对合同的所有箱包进行工艺分解
- `GET /breakdown/result/{contractId}` - 获取工艺分解结果
- `GET /breakdown/export/{contractId}` - 导出工艺分解表

### 合同参数管理接口
- `GET /contract-parameters` - 获取合同参数列表
- `POST /contract-parameters` - 创建合同参数
- `PUT /contract-parameters/{id}` - 更新合同参数
- `DELETE /contract-parameters/{id}` - 删除合同参数

### 缓存测试接口
- `GET /cache/test` - 缓存功能测试
- `POST /cache/test` - 缓存写入测试

## 部署说明

### Docker部署

#### 一键部署（推荐）
```bash
# 使用Docker Compose一键启动所有服务
docker-compose up -d
```

#### 服务配置
当前Docker Compose配置包含以下服务：

- **MariaDB数据库** (端口: 3307)
  - 数据库: `mms_db`
  - 用户: `mms_user`
  - 密码: `mms_password`
  - 自动初始化数据库结构和数据

- **Redis缓存** (端口: 6379)
  - 用于分布式缓存和锁
  - 数据持久化支持

- **后端服务** (端口: 8080)
  - Spring Boot应用
  - 自动连接数据库和Redis
  - 支持热重载开发模式

- **前端服务** (端口: 9000)
  - Vue 3应用
  - Nginx反向代理
  - 静态资源优化

#### 手动构建部署
```bash
# 1. 构建后端镜像
mvn clean package
docker build -f Dockerfile.backend -t mms-backend .

# 2. 构建前端镜像
cd frontend
npm run build
docker build -t mms-frontend .

# 3. 启动服务
docker-compose up -d
```

#### 访问地址
- 前端应用: http://localhost:9000
- 后端API: http://localhost:8080
- 数据库: localhost:3307
- Redis: localhost:6379

## 📚 文档说明

### 技术文档
- **[系统设计文档](docs/系统设计文档.md)** - 完整的技术架构和设计说明
- **[数据模型设计](docs/Data%20Modeling.md)** - 数据库设计和实体关系
- **[需求规格说明](docs/Spec.md)** - 业务需求和功能规格
- **[升级指南](UPGRADE_GUIDE.md)** - Spring Boot 3.2.0 升级说明

### 部署文档
- **[Windows环境配置](WINDOWS_SETUP.md)** - Windows环境详细配置说明
- **Docker Compose配置** - 容器化部署配置

## 开发指南

### 代码规范
- 后端遵循Java编码规范
- 前端遵循Vue.js最佳实践
- 使用ESLint和Prettier进行代码格式化

### 测试
```bash
# 后端测试
mvn test

# 前端测试
cd frontend
npm run test
```

### 贡献指南
1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 🚀 升级说明

项目已升级到 **Spring Boot 3.2.0 + Java 21**！

📖 详细升级指南请参考: [UPGRADE_GUIDE.md](./UPGRADE_GUIDE.md)

### 主要升级内容
- ✅ Java 11 → Java 21
- ✅ Spring Boot 2.7.18 → 3.2.0
- ✅ Jakarta EE 迁移 (javax → jakarta)
- ✅ Spring Security 配置更新
- ✅ 所有依赖版本更新

## 🎯 项目特色

### 技术特色
- **现代化技术栈**: Spring Boot 3.2.0 + Vue 3 + Java 21
- **容器化部署**: Docker + Docker Compose 一键部署
- **分布式架构**: Redis缓存 + 分布式锁支持
- **安全机制**: Spring Security + JWT认证
- **文档完善**: 完整的技术文档和API文档

### 业务特色
- **专业领域**: 专为电梯制造行业设计
- **自动化处理**: 自动工艺分解算法
- **完整流程**: 从合同到生产计划的全流程管理
- **数据可视化**: ECharts图表展示
- **文件处理**: Excel/PDF导入导出

## 🚀 开发状态与下一步计划

### 当前开发状态
项目已完成核心架构和基础功能开发，主要模块包括：

#### 已完成功能 ✅
- **基础架构**: Spring Boot 3.2.0 + Vue 3 前后端分离架构
- **数据管理**: 完整的CRUD操作，支持分页和搜索
- **用户认证**: JWT Token认证系统
- **文件处理**: Excel装箱单上传和预览
- **工艺分解**: 核心分解算法实现
- **缓存系统**: Redis分布式缓存和锁
- **容器化**: Docker + Docker Compose部署

#### 开发中功能 🔄
- **生产计划**: 基于工艺工序的生产计划生成算法
- **成本估算**: 零部件和工艺成本计算逻辑
- **投标报价**: 利润率设置和价格生成功能
- **历史记录**: 数据变更追踪和审计日志

### 下一步开发计划

#### 短期目标（1-2周）
1. **完善生产计划模块**
   - 实现基于工艺工序的最优制造流程算法
   - 添加生产计划表生成功能
   - 支持计划调整和优化

2. **开发成本估算功能**
   - 零部件成本计算
   - 工艺成本分析
   - 总成本估算和报表

3. **增强用户体验**
   - 添加更多交互反馈
   - 优化页面加载性能
   - 完善错误处理机制

#### 中期目标（1个月）
1. **投标报价系统**
   - 利润率设置界面
   - 营销成本配置
   - 代理商佣金计算
   - 投标价格生成和导出

2. **历史记录追踪**
   - 合同参数修订记录
   - 装箱单修改历史
   - 分解表变更追踪

3. **系统优化**
   - 性能监控和优化
   - 数据库查询优化
   - 缓存策略优化

#### 长期目标（2-3个月）
1. **功能扩展**
   - 移动端适配
   - 多语言支持
   - 高级报表功能
   - 数据导入导出

2. **技术升级**
   - 微服务架构改造
   - 容器编排优化
   - 监控和日志系统
   - CI/CD流水线

### 技术债务
- [ ] 完善单元测试覆盖率
- [ ] 添加集成测试
- [ ] 完善API文档
- [ ] 代码质量检查工具集成

## 📞 联系方式

如有问题或建议，请联系开发团队。

---

**MMS制造管理系统** - 让电梯制造更智能、更高效！