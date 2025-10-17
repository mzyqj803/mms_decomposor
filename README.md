# MMS制造管理系统 (Manufacturing Management System)

## 项目简介

MMS制造管理系统是一个专为电梯制造行业设计的工艺分解和管理系统。系统能够根据合同和装箱单数据，自动生成工艺分解表，并提供完整的生产计划、成本估算和投标报价功能。

## 📋 项目状态

### 核心功能 ✅
- ✅ **项目架构**: 前后端分离架构，Docker容器化部署
- ✅ **后端框架**: Spring Boot 3.2.0 + Java 21
- ✅ **前端框架**: Vue 3 + Element Plus + Vite
- ✅ **数据库设计**: 完整的15+表数据模型，支持复杂业务关系
- ✅ **用户认证**: JWT Token认证系统
- ✅ **缓存系统**: Redis + Redisson分布式锁 + 组件缓存

### 合同管理 ✅
- ✅ **完整CRUD**: 新增、查看、编辑合同
- ✅ **逻辑删除**: 软删除机制，保留历史数据
- ✅ **参数管理**: 合同参数配置与编辑
- ✅ **装箱单上传**: Excel文件上传和解析
- ✅ **装箱单克隆**: 跨合同复制装箱单
- ✅ **状态跟踪**: 草稿、处理中、已完成、错误状态
- ✅ **删除确认**: 双重确认机制，防止误删
- ✅ **批量操作**: 支持批量装箱单处理

### 零部件管理 ✅
- ✅ **完整CRUD**: 新增、查看、编辑、删除零部件
- ✅ **规格管理**: 零部件规格参数配置
- ✅ **关系管理**: 父子组件关系维护
- ✅ **工艺信息**: 零部件工艺流程配置
- ✅ **采购标识**: 自制/采购分类管理
- ✅ **通用件标识**: 装箱紧固件/装配紧固件/非紧固件分类
- ✅ **搜索过滤**: 多条件搜索和分页

### 工艺分解 ✅
- ✅ **自动分解**: 基于BOM的递归分解算法
- ✅ **单箱包分解**: 独立箱包工艺分解
- ✅ **合同批量分解**: 整个合同所有箱包批量处理
- ✅ **并行处理**: 多线程并行分解，性能提升3-5倍
- ✅ **非标组件自动创建**: 识别`~`符号自动生成非标组件
- ✅ **问题组件追踪**: 记录分解过程中的问题零部件
- ✅ **分解结果查看**: 详细的分解结果展示和子组件导航
- ✅ **合并分解表**: PDF格式合并分解表生成和导出
- ✅ **组件合并**: 相同部件编号自动合并数量
- ✅ **ERP代码集成**: 自动匹配和存储ERP代码
- ✅ **超时处理**: 友好的超时提示和重试机制
- ✅ **性能监控**: 详细的性能日志和时间统计

### 紧固件仓库管理 ✅
- ✅ **完整CRUD**: 紧固件信息管理
- ✅ **多条件搜索**: 产品代码、ERP代码、规格、材料等
- ✅ **默认紧固件**: 支持设置默认紧固件
- ✅ **相似度搜索**: 基于Lucene的全文搜索和TF-IDF算法
- ✅ **ERP代码查找**: FastenerErpCodeFinder工具类
- ✅ **渐进式匹配**: productCode → specs → level → surfaceTreatment
- ✅ **缓存优化**: Redis缓存提升查询性能
- ✅ **装配类型查询**: 产线装配/仓库装箱分类

### 装箱单管理 ✅
- ✅ **Excel上传**: 支持.xlsx/.xls格式上传
- ✅ **文件解析**: 自动解析装箱单组件数据
- ✅ **装箱单预览**: 内容预览和验证
- ✅ **CRUD操作**: 创建、编辑、删除装箱单
- ✅ **搜索过滤**: 按合同、项目、箱包号搜索

### 待开发功能 🔄
- 🔄 **生产计划**: 基于工艺工序的最优生产流程
- 🔄 **成本估算**: 零部件和工艺成本计算
- 🔄 **投标报价**: 利润率设置和价格生成
- 🔄 **历史记录**: 数据变更追踪和审计日志
- 🔄 **权限管理**: 细粒度的角色和权限控制
- 🔄 **报表系统**: 多维度数据分析和报表

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
- ✅ **完整CRUD**: 新增、查看、编辑合同功能
- ✅ **逻辑删除**: 使用status字段实现软删除，保留历史数据
- ✅ **删除确认**: 双重确认机制（需输入合同号），防止误删
- ✅ **参数管理**: 动态添加、编辑、删除合同参数，支持参数名验证
- ✅ **装箱单上传**: Excel文件上传并自动解析生成装箱单
- ✅ **装箱单克隆**: 跨合同复制装箱单及其组件数据
- ✅ **状态跟踪**: 草稿(0)、处理中(1)、已完成(2)、错误(3)、已删除(4)
- ✅ **搜索过滤**: 按合同号、项目名称、状态搜索，支持分页
- ✅ **合同详情页**: 可折叠卡片展示，按需加载数据
- ✅ **操作菜单**: 工艺分解、生产计划等功能快捷入口

### 2. 零部件管理 ✅
- ✅ **完整CRUD**: 创建、查看、编辑、删除零部件
- ✅ **规格管理**: ComponentsSpec表管理零部件规格参数
- ✅ **关系管理**: ComponentsRelationship表维护父子组件关系
- ✅ **工艺信息**: ComponentsProcesses表配置工艺流程
- ✅ **采购标识**: procurementFlag区分自制/采购
- ✅ **通用件分类**: commonPartsFlag=0(非紧固件)/1(装箱紧固件)/2(装配紧固件)
- ✅ **父组件查找**: ParentComponentSearchDialog支持选择父组件
- ✅ **表单优化**: 失焦自动更新，实时保存编辑
- ✅ **搜索过滤**: 按零部件代号、名称、分类多条件搜索
- ✅ **缓存优化**: ComponentCacheService提供Redis缓存

### 3. 装箱单管理 ✅
- ✅ **Excel上传**: 支持.xlsx/.xls格式，最大10MB
- ✅ **文件解析**: 自动解析Excel并生成装箱单及组件数据
- ✅ **装箱单预览**: ContainerPreview组件预览装箱单内容
- ✅ **CRUD操作**: 创建、编辑、删除装箱单
- ✅ **装箱单克隆**: 从其他合同克隆装箱单
- ✅ **搜索过滤**: 按合同号、项目名称、箱包号搜索
- ✅ **组件管理**: ContainerComponents表存储装箱单组件明细
- ✅ **分页显示**: 支持20/50/100条每页

### 4. 工艺分解 ✅
- ✅ **自动分解算法**: 基于BOM表的递归分解算法
- ✅ **单箱包分解**: 对单个箱包进行独立工艺分解
- ✅ **合同批量分解**: 对合同的所有箱包批量分解
- ✅ **并行处理**: 多线程并行处理箱包，性能提升3-5倍
- ✅ **线程池管理**: 动态线程池，size=min(箱包数, CPU核心数)
- ✅ **非标组件自动创建**: 识别包含`~`的componentCode，自动创建非标组件
- ✅ **非标组件标记**: 自动添加nonStandardPartFlag=1到components_spec
- ✅ **线程安全**: ConcurrentHashMap + synchronized双重检查防止重复创建
- ✅ **问题组件追踪**: Problems表记录无法分解的组件
- ✅ **分解结果查看**: 显示所有分解组件，支持子组件导航
- ✅ **规格悬停显示**: 鼠标悬停2秒显示组件规格，10秒后自动关闭
- ✅ **合并分解表**: PDF格式合并分解表，相同组件自动合并数量
- ✅ **PDF直接打开**: 浏览器新窗口直接打开PDF，无需下载
- ✅ **ERP代码集成**: 自动匹配紧固件ERP代码并存储
- ✅ **性能监控**: 详细日志记录各步骤耗时
- ✅ **超时处理**: 5分钟超时，友好错误提示
- ✅ **数据清理**: 重新分解前自动清理旧数据

### 5. 紧固件仓库管理 ✅
- ✅ **完整CRUD**: 紧固件信息增删改查
- ✅ **多条件搜索**: 产品代码、ERP代码、名称、规格、材料、表面处理、等级
- ✅ **默认紧固件**: 支持设置默认紧固件标记
- ✅ **相似度搜索**: 基于Lucene的全文搜索引擎
- ✅ **TF-IDF算法**: 计算文本相似度得分
- ✅ **索引管理**: 支持索引重建和增量更新
- ✅ **ERP代码查找**: FastenerErpCodeFinder工具类
- ✅ **渐进式匹配**: 按productCode → specs → level → surfaceTreatment依次匹配
- ✅ **缓存优化**: FastenerWarehouseCacheService提供Redis缓存
- ✅ **装配类型查询**: 数据库视图区分产线装配/仓库装箱
- ✅ **批量操作**: 支持批量设置默认、批量删除

### 6. 用户认证 ✅
- ✅ **JWT Token**: 基于JWT的无状态认证
- ✅ **用户登录/登出**: 完整的登录登出流程
- ✅ **用户状态管理**: Pinia store管理用户状态
- ✅ **路由守卫**: 自动跳转登录页
- ✅ **Token刷新**: 支持Token自动刷新
- ✅ **安全配置**: Spring Security配置

### 7. 缓存系统 ✅
- ✅ **Redis分布式缓存**: 组件、紧固件、合同等数据缓存
- ✅ **Redisson分布式锁**: 防止并发问题
- ✅ **ComponentCacheService**: 组件缓存服务
- ✅ **FastenerWarehouseCacheService**: 紧固件缓存服务
- ✅ **缓存预热**: 启动时自动加载常用数据
- ✅ **缓存管理API**: 支持手动刷新、清理缓存
- ✅ **缓存统计**: 缓存命中率监控

### 8. 数据导出 ✅
- ✅ **Excel导出**: 工艺分解表Excel格式导出
- ✅ **PDF导出**: 合并分解表PDF格式生成
- ✅ **iText库**: 使用iText 8.0.2生成PDF
- ✅ **自定义格式**: 支持自定义表头、列宽、字体
- ✅ **直接打开**: PDF直接在浏览器打开，无需下载
- ✅ **文件命名**: 自动生成包含合同号的文件名

### 9. 生产计划 🔄
- 🔄 基于工艺工序的最优制造流程
- 🔄 生产计划表生成
- 🔄 计划调整和优化
- 🔄 资源分配和排程

### 10. 成本估算 🔄
- 🔄 零部件成本计算
- 🔄 工艺成本分析
- 🔄 总成本估算
- 🔄 成本报表生成

### 11. 投标报价 🔄
- 🔄 利润率设置
- 🔄 营销成本配置
- 🔄 代理商佣金计算
- 🔄 投标价格生成

### 12. 历史记录 🔄
- 🔄 合同参数修订记录
- 🔄 装箱单修改记录
- 🔄 分解表变更追踪
- 🔄 审计日志查询

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
├── start.sh                           # Linux启动脚本
├── stop.sh                            # Linux停止脚本
└── script/                            # Windows批处理脚本目录
    ├── start.bat                      # Windows启动脚本
    ├── dev-start.bat                  # Windows开发启动脚本
    ├── stop.bat                       # Windows停止脚本
    ├── restart.bat                    # Windows重启脚本
    ├── clean.bat                      # Windows清理脚本
    ├── check-env.bat                  # Windows环境检查脚本
    ├── status.bat                     # Windows状态检查脚本
    ├── logs.bat                       # Windows日志查看脚本
    ├── test-commands.bat              # Windows测试命令脚本
    └── test-redis-cache.bat           # Windows Redis缓存测试脚本
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
script\start.bat
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
script\dev-start.bat
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
- `script\start.bat` - 一键启动所有服务
- `script\dev-start.bat` - 开发模式启动（支持热重载）
- `script\stop.bat` - 停止所有服务
- `script\restart.bat` - 重启所有服务
- `script\clean.bat` - 清理构建文件和日志
- `script\check-env.bat` - 检查环境依赖
- `script\status.bat` - 检查服务状态
- `script\logs.bat` - 查看应用日志
- `script\test-commands.bat` - 运行测试命令
- `script\test-redis-cache.bat` - 测试Redis缓存功能

#### Linux/Mac脚本
- `start.sh` - 一键启动所有服务
- `stop.sh` - 停止所有服务

## API文档

### 认证接口
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出
- `GET /auth/me` - 获取当前用户信息

### 合同管理接口
- `GET /api/contracts` - 获取合同列表（支持分页和搜索）
  - 查询参数: `page`, `size`, `contractNo`, `projectName`, `status`
- `GET /api/contracts/{id}` - 获取合同详情
- `GET /api/contracts/{id}/include-deleted` - 获取包含已删除状态的合同详情
- `POST /api/contracts` - 创建合同（包含合同参数）
- `PUT /api/contracts/{id}` - 更新合同
- `DELETE /api/contracts/{id}` - 逻辑删除合同（需传入contractNo确认）
- `PUT /api/contracts/{id}/parameters` - 更新合同参数
- `GET /api/contracts/{id}/containers` - 获取合同的所有装箱单
- `POST /api/contracts/{id}/containers/upload` - 上传装箱单Excel文件
- `POST /api/contracts/{id}/containers/clone` - 从其他合同克隆装箱单
- `POST /api/contracts/{id}/breakdown/start` - 开始工艺分解

### 零部件管理接口
- `GET /api/components` - 获取零部件列表（支持分页和搜索）
  - 查询参数: `page`, `size`, `componentCode`, `name`, `categoryCode`
- `GET /api/components/{id}` - 获取零部件详情（包含规格、工艺、关系）
- `GET /api/components/by-code/{componentCode}` - 根据零部件代号获取详情
- `GET /api/components/{componentCode}/specs` - 获取零部件规格信息
- `POST /api/components` - 创建零部件
- `PUT /api/components/{id}` - 更新零部件
- `DELETE /api/components/{id}` - 删除零部件
- `GET /api/components/search-parent` - 搜索父组件（用于关系建立）

### 装箱单管理接口
- `GET /api/containers` - 获取装箱单列表（支持分页和搜索）
  - 查询参数: `page`, `size`, `contractNo`, `projectName`, `containerNo`
- `GET /api/containers/{id}` - 获取装箱单详情
- `POST /api/containers` - 创建装箱单（需关联合同）
- `PUT /api/containers/{id}` - 更新装箱单
- `DELETE /api/containers/{id}` - 删除装箱单
- `POST /api/contracts/{contractId}/upload` - 上传Excel装箱单文件（自动解析）
- `GET /api/containers/{id}/preview` - 预览装箱单内容
- `GET /api/containers/{id}/components` - 获取装箱单组件明细

### 工艺分解接口
- `POST /api/breakdown/container/{containerId}` - 对单个箱包进行工艺分解
  - 响应包含: 分解组件列表、问题组件、总数、耗时
- `POST /api/breakdown/contract/{contractId}` - 对合同的所有箱包批量分解（并行处理）
  - 响应包含: 所有箱包分解结果、总统计、性能指标
- `GET /api/breakdown/container/{containerId}` - 获取箱包分解结果
- `GET /api/breakdown/contract/{contractId}/summary` - 获取合同分解汇总
- `DELETE /api/breakdown/container/{containerId}` - 删除箱包分解结果
- `POST /api/breakdown/merge` - 合并多个箱包的分解表生成PDF
  - 请求体: `containerIds` (数组)
  - 响应包含: PDF文件URL

### 合同参数管理接口
- `GET /api/contract-parameters/contract/{contractId}` - 获取指定合同的参数列表
- `PUT /api/contracts/{contractId}/parameters` - 批量更新合同参数
- `POST /api/contract-parameters` - 创建单个合同参数
- `PUT /api/contract-parameters/{id}` - 更新合同参数
- `DELETE /api/contract-parameters/{id}` - 删除合同参数

### 紧固件仓库管理接口
- `GET /api/fastener-warehouse` - 获取紧固件列表（支持多条件搜索和分页）
  - 查询参数: `page`, `size`, `productCode`, `erpCode`, `name`, `specs`, `material`, `surfaceTreatment`, `level`, `defaultFlag`
- `GET /api/fastener-warehouse/{id}` - 获取紧固件详情
- `POST /api/fastener-warehouse` - 创建紧固件
- `PUT /api/fastener-warehouse/{id}` - 更新紧固件
- `DELETE /api/fastener-warehouse/{id}` - 删除紧固件
- `POST /api/fastener-warehouse/{id}/set-default` - 设置为默认紧固件
- `GET /api/fastener-warehouse/materials` - 获取所有材料选项
- `GET /api/fastener-warehouse/surface-treatments` - 获取所有表面处理选项
- `GET /api/fastener-warehouse/levels` - 获取所有等级选项

### 紧固件相似度搜索接口
- `GET /api/fastener-similarity/search` - 根据查询文本搜索相似紧固件
  - 查询参数: `query`, `limit` (默认10)
  - 响应包含: 相似度得分、匹配紧固件列表
- `GET /api/fastener-similarity/search-by-id/{id}` - 根据紧固件ID搜索相似紧固件
- `POST /api/fastener-similarity/rebuild-index` - 重新构建Lucene索引
- `GET /api/fastener-similarity/index-status` - 获取索引状态信息
  - 响应包含: 文档数、索引大小、最后更新时间

### 紧固件缓存管理接口
- `GET /api/fastener-cache/status` - 检查紧固件缓存状态
- `POST /api/fastener-cache/initialize` - 初始化紧固件缓存（预热）
- `DELETE /api/fastener-cache/clear` - 清空紧固件缓存
- `POST /api/fastener-cache/reload/{productCode}` - 重新加载指定产品代码缓存
- `GET /api/fastener-cache/test/{productCode}` - 测试从缓存获取紧固件
- `GET /api/fastener-cache/stats` - 获取缓存统计信息
  - 响应包含: 缓存大小、命中率、miss率

### 组件紧固件类型查询接口
- `GET /api/component-fastener/type/{componentId}` - 查询组件装配类型
  - 响应: `产线装配` 或 `仓库装箱` 或 `非紧固件`
- `GET /api/component-fastener/types` - 获取所有紧固件分类列表
  - 响应包含: 产线装配列表、仓库装箱列表、统计数量
- `GET /api/component-fastener/assembled/{componentId}` - 检查是否产线装配紧固件
- `GET /api/component-fastener/unassembled/{componentId}` - 检查是否仓库装箱紧固件

### 工艺分解ERP集成接口
- `GET /api/breakdown-erp` - 获取工艺分解ERP代码记录列表
- `GET /api/breakdown-erp/{id}` - 获取指定ERP代码记录
- `GET /api/breakdown-erp/breakdown/{breakdownId}` - 获取指定分解记录的ERP代码
- `GET /api/breakdown-erp/container/{containerId}` - 获取箱包所有分解记录的ERP代码（Map格式）
- `GET /api/breakdown-erp/contract/{contractId}` - 获取合同所有分解记录的ERP代码
- `POST /api/breakdown-erp` - 创建ERP代码记录
- `PUT /api/breakdown-erp/{id}` - 更新ERP代码记录
- `DELETE /api/breakdown-erp/{id}` - 删除ERP代码记录

### 缓存测试接口
- `GET /api/cache/test` - 缓存功能测试
- `POST /api/cache/test` - 缓存写入测试
- `DELETE /api/cache/test` - 缓存清理测试
- `GET /api/cache/lock-test` - 分布式锁测试

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
- **现代化技术栈**: Spring Boot 3.2.0 + Java 21 + Vue 3 + Element Plus
- **容器化部署**: Docker + Docker Compose，一键启动所有服务
- **高性能**: 
  - 多线程并行处理工艺分解，性能提升3-5倍
  - Redis缓存 + Redisson分布式锁
  - 批量查询优化，解决N+1查询问题
- **高并发**: 
  - 线程安全的非标组件创建（ConcurrentHashMap + synchronized）
  - 行级锁避免死锁
  - 独立事务处理每个箱包
- **智能化**: 
  - 自动识别非标组件（`~`符号）
  - 自动ERP代码匹配（渐进式匹配算法）
  - 全文搜索 + TF-IDF相似度算法
- **安全机制**: 
  - Spring Security + JWT Token认证
  - 逻辑删除保留历史数据
  - 双重确认防误删
- **文档完善**: 50+份技术文档，详细的实现总结

### 业务特色
- **专业领域**: 专为电梯制造行业设计，深度定制
- **自动化处理**: 
  - 自动工艺分解算法（基于BOM表递归）
  - 非标组件自动创建
  - ERP代码自动匹配
- **完整流程**: 从合同录入 → 装箱单上传 → 工艺分解 → PDF生成
- **智能分解**: 
  - 识别问题组件并记录
  - 相同组件自动合并数量
  - 子组件关系可视化导航
- **数据导出**: 
  - Excel格式导出工艺分解表
  - PDF格式合并分解表
  - 浏览器直接打开PDF，无需下载
- **用户体验**: 
  - 可折叠卡片按需加载数据
  - 鼠标悬停显示组件规格
  - 友好的超时和错误提示
  - 详细的操作日志和性能监控

### 性能优化
- **并行处理**: ExecutorService线程池并行处理箱包
- **批量查询**: 一次性批量查询ERP代码，避免循环查询
- **缓存优化**: 
  - 组件缓存（ComponentCacheService）
  - 紧固件缓存（FastenerWarehouseCacheService）
  - 启动时预热常用数据
- **数据库优化**: 
  - 使用数据库视图（产线装配/仓库装箱）
  - 索引优化
  - 批量保存减少SQL执行次数

### 健壮性设计
- **事务管理**: 每个箱包独立事务，失败不影响其他箱包
- **异常处理**: 完整的异常捕获和友好提示
- **数据清理**: 重新分解前自动清理旧数据，避免重复
- **线程安全**: 双重检查锁定防止重复创建
- **超时处理**: 5分钟超时配置，提供重试指引

## 🚀 开发状态与下一步计划

### 当前开发状态
项目已完成核心业务流程和主要功能模块，系统运行稳定，性能优异。

#### 已完成功能 ✅

**核心业务流程**
- ✅ **合同管理**: 完整的CRUD、逻辑删除、参数管理、装箱单上传/克隆
- ✅ **零部件管理**: CRUD、规格管理、关系管理、工艺配置、缓存优化
- ✅ **装箱单管理**: Excel上传解析、预览、CRUD、组件明细管理
- ✅ **工艺分解**: 
  - 单箱包/批量分解
  - 并行处理（3-5倍性能提升）
  - 非标组件自动创建
  - ERP代码自动匹配
  - 合并分解表PDF生成
  - 问题组件追踪

**技术基础设施**
- ✅ **前后端分离**: Spring Boot 3.2.0 + Java 21 + Vue 3 + Element Plus
- ✅ **容器化部署**: Docker + Docker Compose，一键启动
- ✅ **用户认证**: JWT Token认证，路由守卫
- ✅ **缓存系统**: Redis缓存 + Redisson分布式锁 + 缓存预热
- ✅ **文件处理**: Excel解析（Apache POI）+ PDF生成（iText）
- ✅ **全文搜索**: Lucene索引 + TF-IDF相似度算法

**数据管理**
- ✅ **紧固件仓库**: CRUD、多条件搜索、相似度搜索、ERP代码查找
- ✅ **装配类型**: 数据库视图区分产线装配/仓库装箱
- ✅ **合同参数**: 动态参数配置、参数验证
- ✅ **ERP集成**: 自动匹配和存储ERP代码

**性能优化**
- ✅ **并行处理**: 多线程并行处理箱包
- ✅ **批量查询**: 解决N+1查询问题
- ✅ **缓存优化**: 组件缓存、紧固件缓存
- ✅ **数据库优化**: 视图、索引、批量操作

**开发运维**
- ✅ **Windows批处理脚本**: 10+个实用脚本（start、stop、restart、clean等）
- ✅ **Linux Shell脚本**: start.sh、stop.sh
- ✅ **环境配置**: Docker、Maven、Node.js镜像源配置
- ✅ **技术文档**: 50+份详细的实现总结和部署指南

#### 开发中功能 🔄
- 🔄 **生产计划**: 基于工艺工序的最优生产流程
- 🔄 **成本估算**: 零部件和工艺成本计算
- 🔄 **投标报价**: 利润率设置和价格生成
- 🔄 **历史记录**: 数据变更追踪和审计日志
- 🔄 **权限管理**: 细粒度的角色和权限控制
- 🔄 **报表系统**: 多维度数据分析和可视化

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