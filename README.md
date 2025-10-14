# MMS制造管理系统 (Manufacturing Management System)

## 项目简介

MMS制造管理系统是一个专为电梯制造行业设计的工艺分解和管理系统。系统能够根据合同和装箱单数据，自动生成工艺分解表，并提供完整的生产计划、成本估算和投标报价功能。

## 📋 项目状态

- ✅ **项目架构**: 前后端分离架构已完成
- ✅ **后端框架**: Spring Boot 3.2.0 + Java 21
- ✅ **前端框架**: Vue 3 + Element Plus
- ✅ **数据库设计**: 完整的13表数据模型
- ✅ **容器化部署**: Docker + Docker Compose
- ✅ **系统设计文档**: 完整的技术文档
- ✅ **基础CRUD功能**: 合同、零部件、装箱单、紧固件管理
- ✅ **用户认证系统**: JWT认证框架
- ✅ **缓存系统**: Redis + Redisson分布式锁
- ✅ **文件上传**: Excel装箱单上传和预览
- ✅ **工艺分解算法**: 核心分解逻辑已实现
- ✅ **紧固件仓库**: 紧固件管理和查询功能
- ✅ **合同参数管理**: 合同参数配置和管理
- ✅ **装箱单预览**: Excel文件内容预览功能
- ✅ **缓存测试**: Redis缓存功能测试接口
- ✅ **紧固件ERP代码查找**: FastenerErpCodeFinder工具类
- ✅ **紧固件相似度搜索**: 基于Lucene的全文搜索
- ✅ **紧固件缓存服务**: Redis缓存优化
- ✅ **工艺分解ERP集成**: ERP代码自动匹配
- ✅ **组件紧固件类型查询**: 产线装配/仓库装箱分类
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
- **MariaDB 10.3** - 主数据库
- **Redis 6.0** - 缓存和分布式锁
- **Redisson 3.25.2** - Redis客户端
- **Apache POI 5.2.5** - Excel文件处理
- **iText 8.0.2** - PDF文件生成
- **Apache Lucene 10.2.1** - 全文搜索引擎
- **Lombok** - 代码简化
- **Jackson** - JSON处理
- **Maven 3.11.0** - 构建工具

### 前端
- **Vue 3.3.8** - 前端框架
- **Vite 5.0.0** - 构建工具
- **Element Plus 2.4.4** - UI组件库
- **Vue Router 4.2.5** - 路由管理
- **Pinia 2.1.7** - 状态管理
- **Axios 1.6.2** - HTTP客户端
- **ECharts 5.4.3** - 图表库
- **Vue-ECharts 6.6.1** - ECharts Vue组件
- **Sass 1.69.5** - CSS预处理器
- **Day.js 1.11.10** - 日期处理
- **Lodash-es 4.17.21** - 工具库
- **ESLint 8.53.0** - 代码检查
- **Prettier 3.1.0** - 代码格式化

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

### 11. 紧固件仓库管理 ✅
- ✅ 紧固件基础信息维护
- ✅ 多条件搜索和分页
- ✅ 紧固件规格参数管理
- ✅ 默认紧固件标记
- ✅ 紧固件缓存优化
- ✅ 紧固件相似度搜索（基于Lucene）
- ✅ 全文搜索和TF-IDF算法
- ✅ 索引管理和重建功能
- ✅ 紧固件ERP代码查找工具类
- ✅ Redis缓存服务（缓存key为productCode）
- ✅ 渐进式匹配算法（productCode → specs → level → surfaceTreatment）

### 12. 合同参数管理 ✅
- ✅ 合同参数配置
- ✅ 参数模板管理
- ✅ 参数继承机制
- ✅ 参数验证规则

### 13. 装箱单预览 ✅
- ✅ Excel文件上传
- ✅ 装箱单内容预览
- ✅ 数据格式验证
- ✅ 预览数据导出

### 14. 缓存测试 ✅
- ✅ Redis缓存功能测试
- ✅ 分布式锁测试
- ✅ 缓存性能监控
- ✅ 缓存清理功能

### 15. 紧固件ERP代码查找 ✅
- ✅ FastenerErpCodeFinder工具类
- ✅ 紧固件类型检查（产线装配/仓库装箱）
- ✅ FastenerParser解析器集成
- ✅ 渐进式匹配算法
- ✅ Redis缓存优化
- ✅ 完整的错误处理机制
- ✅ 单元测试覆盖

### 16. 工艺分解ERP集成 ✅
- ✅ ERP代码自动匹配
- ✅ 工艺分解结果ERP代码存储
- ✅ ERP代码记录管理
- ✅ 组件紧固件类型查询
- ✅ 产线装配/仓库装箱分类

### 17. 数据接口 ✅
- ✅ RESTful API
- ✅ 分页和搜索支持
- ✅ 数据验证
- ✅ 统一异常处理
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
│   ├── controller/                     # 控制器层
│   │   ├── AuthController.java         # 认证控制器
│   │   ├── ContractsController.java    # 合同管理控制器
│   │   ├── ComponentsController.java   # 零部件管理控制器
│   │   ├── ContainersController.java   # 装箱单管理控制器
│   │   ├── BreakdownController.java   # 工艺分解控制器
│   │   ├── FastenerWarehouseController.java # 紧固件仓库控制器
│   │   ├── FastenerSimilarityController.java # 紧固件相似度搜索控制器
│   │   ├── ContractParametersController.java # 合同参数控制器
│   │   ├── ContainerUploadController.java # 装箱单上传控制器
│   │   ├── ContainerPreviewController.java # 装箱单预览控制器
│   │   ├── CacheTestController.java    # 缓存测试控制器
│   │   ├── FastenerCacheController.java # 紧固件缓存管理控制器
│   │   ├── ComponentFastenerController.java # 组件紧固件控制器
│   │   └── ContainerComponentsBreakdownErpController.java # 工艺分解ERP控制器
│   ├── dto/                           # 数据传输对象
│   │   ├── ContainerDTO.java          # 装箱单DTO
│   │   ├── FastenerSimilarityResult.java # 紧固件相似度结果DTO
│   │   └── FastenerParseResult.java   # 紧固件解析结果DTO
│   ├── entity/                         # 实体类
│   │   ├── BaseEntity.java            # 基础实体类
│   │   ├── Components.java            # 零部件实体
│   │   ├── Contracts.java            # 合同实体
│   │   ├── Containers.java           # 装箱单实体
│   │   ├── FastenerWarehouse.java    # 紧固件仓库实体
│   │   ├── ContainerComponentsBreakdownErp.java # 工艺分解ERP实体
│   │   ├── ComponentFastenerType.java # 组件紧固件类型实体
│   │   └── ...                       # 其他实体类
│   ├── repository/                     # 数据访问层
│   │   ├── ComponentsRepository.java
│   │   ├── ContractsRepository.java
│   │   ├── ContainersRepository.java
│   │   ├── FastenerWarehouseRepository.java
│   │   └── ...                       # 其他Repository
│   └── service/                        # 服务层
│       ├── CacheService.java          # 缓存服务
│       ├── DistributedLockService.java # 分布式锁服务
│       ├── ContractsService.java      # 合同服务
│       ├── ComponentsService.java    # 零部件服务
│       ├── ContainersService.java     # 装箱单服务
│       ├── BreakdownService.java      # 工艺分解服务
│       ├── FastenerWarehouseService.java # 紧固件仓库服务
│       ├── FastenerLuceneIndexService.java # 紧固件Lucene索引服务
│       ├── ComponentCacheService.java # 零部件缓存服务
│       ├── ContainerUploadService.java # 装箱单上传服务
│       ├── FastenerWarehouseCacheService.java # 紧固件仓库缓存服务
│       ├── FastenerErpCodeService.java # 紧固件ERP代码查找服务
│       └── ContainerComponentsBreakdownErpService.java # 工艺分解ERP服务
│       └── impl/                      # 服务实现
│           ├── ContractsServiceImpl.java
│           ├── ComponentsServiceImpl.java
│           ├── ContainersServiceImpl.java
│           ├── BreakdownServiceImpl.java
│           ├── FastenerWarehouseServiceImpl.java
│           ├── ComponentCacheServiceImpl.java
│           ├── ContainerUploadServiceImpl.java
│           ├── FastenerWarehouseCacheServiceImpl.java # 紧固件仓库缓存服务实现
│           └── ContainerComponentsBreakdownErpServiceImpl.java # 工艺分解ERP服务实现
│   ├── utils/                          # 工具类
│   │   ├── FastenerParser.java         # 紧固件解析器
│   │   ├── FastenerErpCodeFinder.java  # 紧固件ERP代码查找工具类
│   │   └── FastenerErpCodeDemo.java   # 紧固件ERP代码查找演示程序
├── src/main/resources/
│   ├── application.yml                 # 应用配置
│   └── sql/
│       ├── data_init/                  # 数据初始化脚本
│       │   ├── schema.sql             # 数据库结构
│       │   ├── data_init.sql          # 初始数据
│       │   ├── fastener_warehouse_init.sql # 紧固件仓库数据
│       │   └── cleanup_duplicate_components_with_fk.sql # 清理脚本
│       └── init.sql                    # 数据库初始化脚本
├── frontend/                           # 前端项目
│   ├── src/
│   │   ├── api/                        # API接口
│   │   │   ├── index.js              # API入口
│   │   │   ├── contracts.js          # 合同API
│   │   │   ├── components.js         # 零部件API
│   │   │   ├── containers.js         # 装箱单API
│   │   │   ├── breakdown.js          # 工艺分解API
│   │   │   └── fastenerWarehouse.js  # 紧固件仓库API
│   │   ├── components/                 # Vue组件
│   │   │   └── CreateContainerDialog.vue # 创建装箱单对话框
│   │   ├── layouts/                    # 布局组件
│   │   │   └── MainLayout.vue        # 主布局
│   │   ├── router/                     # 路由配置
│   │   │   └── index.js              # 路由定义
│   │   ├── stores/                     # 状态管理
│   │   │   └── user.js               # 用户状态
│   │   ├── styles/                     # 样式文件
│   │   │   ├── index.scss            # 主样式
│   │   │   └── problem-components.css # 问题组件样式
│   │   ├── views/                      # 页面组件
│   │   │   ├── Login.vue             # 登录页面
│   │   │   ├── Dashboard.vue         # 仪表盘
│   │   │   ├── Contracts.vue        # 合同管理
│   │   │   ├── ContractDetail.vue   # 合同详情
│   │   │   ├── Components.vue        # 零部件管理
│   │   │   ├── Containers.vue       # 装箱单管理
│   │   │   ├── Breakdown.vue        # 工艺分解
│   │   │   ├── FastenerWarehouse.vue # 紧固件仓库
│   │   │   ├── ProductionPlan.vue    # 生产计划
│   │   │   ├── CostEstimation.vue   # 成本估算
│   │   │   ├── Bidding.vue          # 投标报价
│   │   │   ├── History.vue          # 历史记录
│   │   │   ├── Settings.vue          # 设置
│   │   │   └── NotFound.vue          # 404页面
│   │   ├── App.vue                    # 根组件
│   │   └── main.js                    # 入口文件
│   ├── public/                         # 静态资源
│   │   ├── logo.svg                   # 主Logo
│   │   └── logo-mini.svg              # 小Logo
│   ├── dist/                           # 构建输出
│   ├── package.json                    # 前端依赖配置
│   ├── vite.config.js                  # Vite配置
│   ├── Dockerfile                      # 前端Dockerfile
│   └── nginx.conf                      # Nginx配置
├── docs/                               # 文档目录
│   ├── 系统设计文档.md                  # 系统设计文档
│   ├── Spec.md                         # 需求规格说明
│   ├── Data Modeling.md                # 数据模型设计
│   ├── 工艺分解.md                      # 工艺分解说明
│   ├── 新建装箱单功能说明.md             # 装箱单功能说明
│   ├── DBER.drawio                     # 数据库ER图
│   ├── 装箱单_sample.xlsx              # 装箱单示例文件
│   ├── env/                            # 环境配置文件
│   │   ├── docker/daemon.json          # Docker配置
│   │   ├── maven/settings.xml          # Maven配置
│   │   └── node/.npmrc                 # Node.js配置
│   └── data_init/                      # 数据初始化脚本
│       ├── data_init.sql
│       ├── 上梁箱包工艺分解_*.sql       # 上梁箱包数据
│       ├── 井道材料工艺分解_*.sql       # 井道材料数据
│       ├── 侧梁箱包工艺分解_*.sql       # 侧梁箱包数据
│       ├── 对重架箱包工艺分解_*.sql     # 对重架箱包数据
│       ├── 层门工艺分解_*.sql          # 层门数据
│       ├── 承重梁箱包工艺分解_*.sql     # 承重梁箱包数据
│       ├── 轿壁箱包工艺分解_*.sql       # 轿壁箱包数据
│       └── 轿底箱包工艺分解_*.sql       # 轿底箱包数据
├── data/                               # 数据文件
│   └── data_init.sql                   # 数据初始化
├── logs/                               # 日志目录
│   └── mms-decomposor.log              # 应用日志
├── docker-compose.yml                  # Docker编排配置
├── Dockerfile.backend                  # 后端Dockerfile
├── pom.xml                            # Maven配置
├── README.md                          # 项目说明
├── PROJECT_SUMMARY.md                 # 项目总结
├── UPGRADE_GUIDE.md                   # 升级指南
├── WINDOWS_SETUP.md                   # Windows环境配置
├── REDIS_CACHE_IMPLEMENTATION.md      # Redis缓存实现说明
├── start.bat                          # Windows启动脚本
├── start.sh                           # Linux启动脚本
├── dev-start.bat                      # Windows开发启动脚本
├── stop.bat                           # Windows停止脚本
├── stop.sh                            # Linux停止脚本
├── restart.bat                        # Windows重启脚本
├── clean.bat                          # Windows清理脚本
├── check-env.bat                      # Windows环境检查脚本
├── status.bat                         # Windows状态检查脚本
├── logs.bat                           # Windows日志查看脚本
├── test-commands.bat                  # Windows测试命令脚本
└── test-redis-cache.bat               # Windows Redis缓存测试脚本
```

## 快速开始

### 环境要求
- **JDK 21+** (推荐使用 OpenJDK 21)
- Maven 3.8.6+
- Node.js 16+
- Docker & Docker Compose
- Git

### 环境优化配置（可选）

为了提升国内网络环境下的构建速度，建议使用项目提供的配置文件：

```bash
# 复制环境配置文件
# Windows
copy docs\env\docker\daemon.json %USERPROFILE%\.docker\daemon.json
copy docs\env\maven\settings.xml %USERPROFILE%\.m2\settings.xml
copy docs\env\node\.npmrc %USERPROFILE%\.npmrc

# Linux/Mac
cp docs/env/docker/daemon.json ~/.docker/daemon.json
cp docs/env/maven/settings.xml ~/.m2/settings.xml
cp docs/env/node/.npmrc ~/.npmrc
```

**配置效果：**
- Docker镜像拉取速度提升3-5倍
- Maven依赖下载速度提升5-10倍
- NPM包安装速度提升3-8倍

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
- 数据库: localhost:3307
- Redis: localhost:6379

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
- 数据库: localhost:3307
- Redis: localhost:6379

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

### 实用脚本

项目提供了多个实用脚本，方便开发和运维：

#### Windows脚本
- `start.bat` - 一键启动所有服务
- `dev-start.bat` - 开发模式启动（支持热重载）
- `stop.bat` - 停止所有服务
- `restart.bat` - 重启所有服务
- `clean.bat` - 清理构建文件和日志
- `check-env.bat` - 检查环境依赖
- `status.bat` - 检查服务状态
- `logs.bat` - 查看应用日志
- `test-commands.bat` - 运行测试命令
- `test-redis-cache.bat` - 测试Redis缓存功能

#### Linux/Mac脚本
- `start.sh` - 一键启动所有服务
- `stop.sh` - 停止所有服务

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
- `GET /contract-parameters/{id}` - 获取合同参数详情
- `POST /contract-parameters` - 创建合同参数
- `PUT /contract-parameters/{id}` - 更新合同参数
- `DELETE /contract-parameters/{id}` - 删除合同参数

### 紧固件仓库管理接口
- `GET /fastener-warehouse` - 获取紧固件列表（支持多条件搜索和分页）
- `GET /fastener-warehouse/{id}` - 获取紧固件详情
- `POST /fastener-warehouse` - 创建紧固件
- `PUT /fastener-warehouse/{id}` - 更新紧固件
- `DELETE /fastener-warehouse/{id}` - 删除紧固件

### 紧固件相似度搜索接口
- `GET /fastener-similarity/search` - 根据查询文本搜索相似紧固件
- `GET /fastener-similarity/search-by-id/{id}` - 根据紧固件ID搜索相似紧固件
- `POST /fastener-similarity/rebuild-index` - 重新构建Lucene索引
- `GET /fastener-similarity/index-status` - 获取索引状态信息

### 紧固件缓存管理接口
- `GET /api/fastener-cache/status` - 检查紧固件缓存状态
- `POST /api/fastener-cache/initialize` - 初始化紧固件缓存
- `DELETE /api/fastener-cache/clear` - 清空紧固件缓存
- `POST /api/fastener-cache/reload/{productCode}` - 重新加载指定产品代码缓存
- `GET /api/fastener-cache/test/{productCode}` - 测试从缓存获取紧固件
- `GET /api/fastener-cache/stats` - 获取缓存统计信息

### 组件紧固件类型查询接口
- `GET /api/component-fastener/type/{componentId}` - 查询组件紧固件类型（产线装配/仓库装箱）

### 工艺分解ERP集成接口
- `GET /api/breakdown-erp` - 获取工艺分解ERP代码记录列表
- `GET /api/breakdown-erp/{id}` - 获取指定ERP代码记录
- `POST /api/breakdown-erp` - 创建ERP代码记录
- `PUT /api/breakdown-erp/{id}` - 更新ERP代码记录
- `DELETE /api/breakdown-erp/{id}` - 删除ERP代码记录

### 装箱单上传和预览接口
- `POST /containers/upload` - 上传Excel装箱单文件
- `GET /containers/{id}/preview` - 预览装箱单内容
- `GET /containers/{id}/export` - 导出装箱单数据

### 缓存测试接口
- `GET /cache/test` - 缓存功能测试
- `POST /cache/test` - 缓存写入测试
- `DELETE /cache/test` - 缓存清理测试

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
  - 镜像: `mariadb:10.3`
  - 数据库: `mms_db`
  - 用户: `mms_user`
  - 密码: `mms_password`
  - 字符集: `utf8mb4`
  - 自动初始化数据库结构和数据
  - 数据持久化到 `mariadb_data` 卷

- **Redis缓存** (端口: 6379)
  - 镜像: `redis:6.0-alpine`
  - 用于分布式缓存和锁
  - 数据持久化到 `redis_data` 卷

- **后端服务** (端口: 8080)
  - 镜像: `mms-backend:latest`
  - Spring Boot 3.2.0 + Java 21应用
  - 自动连接数据库和Redis
  - 支持热重载开发模式
  - 依赖MariaDB和Redis服务

- **前端服务** (端口: 9000)
  - 镜像: `mms-frontend:latest`
  - Vue 3 + Vite应用
  - Nginx反向代理
  - 静态资源优化
  - 依赖后端服务

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
- 数据库: localhost:3307 (用户名: mms_user, 密码: mms_password)
- Redis: localhost:6379

## 📚 文档说明

### 技术文档
- **[系统设计文档](docs/系统设计文档.md)** - 完整的技术架构和设计说明
- **[数据模型设计](docs/Data%20Modeling.md)** - 数据库设计和实体关系
- **[需求规格说明](docs/Spec.md)** - 业务需求和功能规格
- **[升级指南](UPGRADE_GUIDE.md)** - Spring Boot 3.2.0 升级说明
- **[紧固件ERP代码查找工具类文档](docs/FastenerErpCodeFinder.md)** - 工具类使用说明
- **[紧固件仓库Redis缓存服务文档](docs/FastenerWarehouseCacheService_Implementation_Summary.md)** - 缓存服务实现说明

### 环境配置文档
- **[Docker配置](docs/env/docker/daemon.json)** - Docker镜像源和构建器配置
- **[Maven配置](docs/env/maven/settings.xml)** - Maven镜像源和仓库配置
- **[Node.js配置](docs/env/node/.npmrc)** - NPM镜像源配置

### 部署文档
- **[Windows环境配置](WINDOWS_SETUP.md)** - Windows环境详细配置说明
- **[Windows Server部署指南](docs/Windows_Server_Deployment_Guide.md)** - Windows Server完整部署指南
- **Docker Compose配置** - 容器化部署配置

## 🔧 环境配置

### 构建工具配置

项目提供了完整的构建工具配置文件，位于 `docs/env/` 目录下，用于优化国内网络环境下的构建速度。

#### Docker配置 (`docs/env/docker/daemon.json`)
```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.xuanyuan.me",
    "https://docker.1ms.run",
    "https://dislabaiot.xyz"
  ]
}
```

**配置说明：**
- **镜像源**: 配置了多个国内Docker镜像源，加速镜像拉取
- **构建器**: 启用垃圾回收，设置存储空间为20GB
- **实验性功能**: 关闭实验性功能，确保稳定性

#### Maven配置 (`docs/env/maven/settings.xml`)
```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <mirrorOf>*</mirrorOf>
        <name>Aliyun Public Maven</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

**配置说明：**
- **镜像源**: 使用阿里云Maven镜像，加速依赖下载
- **仓库配置**: 配置中央仓库和插件仓库
- **快照策略**: 禁用快照版本，确保构建稳定性

#### Node.js配置 (`docs/env/node/.npmrc`)
```
registry=https://registry.npmmirror.com
```

**配置说明：**
- **NPM镜像**: 使用淘宝NPM镜像源，加速前端依赖安装
- **全局配置**: 适用于所有Node.js项目

### 配置使用方法

#### Windows环境
1. **Docker配置**:
   ```cmd
   # 复制配置文件到Docker配置目录
   copy docs\env\docker\daemon.json %USERPROFILE%\.docker\daemon.json
   # 重启Docker Desktop
   ```

2. **Maven配置**:
   ```cmd
   # 复制配置文件到Maven配置目录
   copy docs\env\maven\settings.xml %USERPROFILE%\.m2\settings.xml
   ```

3. **Node.js配置**:
   ```cmd
   # 复制配置文件到用户目录
   copy docs\env\node\.npmrc %USERPROFILE%\.npmrc
   ```

#### Linux/Mac环境
1. **Docker配置**:
   ```bash
   # 复制配置文件到Docker配置目录
   cp docs/env/docker/daemon.json ~/.docker/daemon.json
   # 重启Docker服务
   sudo systemctl restart docker
   ```

2. **Maven配置**:
   ```bash
   # 复制配置文件到Maven配置目录
   cp docs/env/maven/settings.xml ~/.m2/settings.xml
   ```

3. **Node.js配置**:
   ```bash
   # 复制配置文件到用户目录
   cp docs/env/node/.npmrc ~/.npmrc
   ```

### 性能优化效果

使用这些配置文件后，构建性能将显著提升：

- **Docker镜像拉取**: 速度提升3-5倍
- **Maven依赖下载**: 速度提升5-10倍
- **NPM包安装**: 速度提升3-8倍
- **整体构建时间**: 减少60-80%

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
- **紧固件仓库**: 紧固件管理和查询功能
- **合同参数**: 合同参数配置和管理
- **装箱单预览**: Excel文件内容预览功能
- **缓存测试**: Redis缓存功能测试接口
- **紧固件ERP代码查找**: FastenerErpCodeFinder工具类
- **紧固件缓存服务**: Redis缓存优化（缓存key为productCode）
- **渐进式匹配算法**: productCode → specs → level → surfaceTreatment
- **紧固件相似度搜索**: 基于Lucene的全文搜索和TF-IDF算法
- **工艺分解ERP集成**: ERP代码自动匹配和存储
- **组件紧固件类型查询**: 产线装配/仓库装箱分类
- **实用脚本**: 完整的开发和运维脚本

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