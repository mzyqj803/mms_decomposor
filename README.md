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
- 🔄 **业务功能**: 核心业务逻辑开发中
- 🔄 **工艺分解算法**: 自动分解算法实现中

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

### 1. 合同管理
- 合同信息录入和管理
- 装箱单生成和上传
- 合同状态跟踪

### 2. 零部件管理
- 零部件基础信息维护
- 零部件规格参数管理
- 零部件关系配置

### 3. 工艺分解
- 自动工艺分解算法
- 合并分解表生成
- 分解结果审核和修正

### 4. 生产计划
- 基于工艺工序的最优制造流程
- 生产计划表生成
- 计划调整和优化

### 5. 成本估算
- 零部件成本计算
- 工艺成本分析
- 总成本估算

### 6. 投标报价
- 利润率设置
- 营销成本配置
- 代理商佣金计算
- 投标价格生成

### 7. 历史记录
- 合同参数修订记录
- 装箱单修改记录
- 合并分解表修改记录

### 8. 数据接口
- RESTful API
- 外部系统集成
- 回调更新机制

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
- 前端: http://localhost:3000
- 后端API: http://localhost:8080/api

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
- 前端: http://localhost:3000
- 后端API: http://localhost:8080/api

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
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/me` - 获取当前用户信息

### 合同管理接口
- `GET /api/contracts` - 获取合同列表
- `GET /api/contracts/{id}` - 获取合同详情
- `POST /api/contracts` - 创建合同
- `PUT /api/contracts/{id}` - 更新合同
- `DELETE /api/contracts/{id}` - 删除合同
- `GET /api/contracts/search` - 搜索合同
- `POST /api/contracts/{id}/containers/generate` - 生成装箱单
- `POST /api/contracts/{id}/breakdown/start` - 开始工艺分解
- `GET /api/contracts/{id}/breakdown/result` - 获取分解结果
- `GET /api/contracts/{id}/breakdown/export` - 导出分解表

### 零部件管理接口
- `GET /api/components` - 获取零部件列表
- `GET /api/components/{id}` - 获取零部件详情
- `POST /api/components` - 创建零部件
- `PUT /api/components/{id}` - 更新零部件
- `DELETE /api/components/{id}` - 删除零部件

### 装箱单管理接口
- `GET /api/containers` - 获取装箱单列表
- `GET /api/containers/{id}` - 获取装箱单详情
- `POST /api/containers` - 创建装箱单
- `PUT /api/containers/{id}` - 更新装箱单
- `DELETE /api/containers/{id}` - 删除装箱单
- `POST /api/containers/upload` - 上传装箱单文件
- `GET /api/containers/{id}/preview` - 预览装箱单

## 部署说明

### Docker部署

1. **构建后端镜像**
```bash
mvn clean package
docker build -t mms-backend .
```

2. **构建前端镜像**
```bash
cd frontend
npm run build
docker build -t mms-frontend .
```

3. **使用Docker Compose**
```yaml
version: '3.8'
services:
  mariadb:
    image: mariadb:10.3
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: mms_db
    ports:
      - "3306:3306"
    volumes:
      - mariadb_data:/var/lib/mysql
      - ./src/main/resources/sql/init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:6.0-alpine
    ports:
      - "6379:6379"

  backend:
    image: mms-backend
    ports:
      - "8080:8080"
    depends_on:
      - mariadb
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mariadb://mariadb:3306/mms_db
      SPRING_REDIS_HOST: redis

  frontend:
    image: mms-frontend
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  mariadb_data:
```

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

## 许可证

本项目采用MIT许可证。详情请参阅 [LICENSE](LICENSE) 文件。

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

## 📞 联系方式

如有问题或建议，请联系开发团队。

---

**MMS制造管理系统** - 让电梯制造更智能、更高效！