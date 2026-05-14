# AMS MVP PRD — 资产管理系统

> **For Hermes:** 按照 superpowers `writing-plans` 工作流执行，先 PRD 再实现。

---

## 1. 概述

### 1.1 项目背景

企业 IT 资产管理是 IT 运维的基础工作。现状：
- 资产台账靠 Excel，维护成本高、易出错
- 资产领用/归还流程不清晰，责任难以追溯
- 设备维修/保养记录分散，信息不集中
- 资产统计靠人工汇总，效率低

### 1.2 目标

构建一个 **IT 资产管理系统（AMS）**，实现：
- IT 资产的**全生命周期**管理（采购 → 入库 → 领用 → 维修 → 报废）
- 清晰的**责任归属**（谁在用、哪个部门）
- 结构化的**操作日志**（所有操作可审计）
- 实时**资产视图**（数量、状态、分布）

### 1.3 MVP 范围

**包含：**
- 资产 CRUD（增删改查）
- 资产分类（硬件设备、网络设备、配件耗材、软件许可证）
- 资产状态（库存、已领用、维修中、已报废）
- 资产领用/归还
- 维修记录
- 仪表盘（统计总览）
- 操作日志（结构化，记录到 Elasticsearch，按日期建索引）

**不包含（v1）：**
- 多租户/权限管理（所有人可写）
- 采购审批流程
- 财务/折旧计算
- 条码/二维码扫描
- 通知提醒

---

## 2. 功能规格

### 2.1 资产数据模型

```
Asset（资产）
├── id            Long, PK, 自增
├── assetCode     String, 唯一编码（如 "PC-2024-0001"）
├── name          String, 资产名称（如 "ThinkPad X1 Carbon"）
├── category      Enum, 分类（ HARDWARE / NETWORK / PERIPHERAL / SOFTWARE_LICENSE ）
├── status        Enum, 状态（ IN_STOCK / IN_USE / MAINTENANCE / RETIRED ）
├── spec          String, 规格描述（如 "16GB RAM, 512GB SSD"）
├── purchaseDate  Date, 采购日期
├── purchasePrice Decimal, 采购价格（元）
├── warrantyEnd   Date, 保修到期
├── supplier      String, 供应商
├── location      String, 当前存放/使用地点
├── assigneeId    Long, FK → Employee, 领用人（可为空）
├── createdAt     DateTime, 创建时间
├── updatedAt     DateTime, 更新时间
└── deleted       Boolean, 软删除标记

Employee（员工/使用人）
├── id        Long, PK, 自增
├── name      String, 姓名
├── dept      String, 部门
├── email     String, 邮箱
├── phone     String, 电话
├── createdAt DateTime
└── updatedAt DateTime

MaintenanceRecord（维修记录）
├── id         Long, PK, 自增
├── assetId    Long, FK → Asset
├── type       Enum, 维修类型（ REPAIR / MAINTENANCE / INSPECTION ）
├── description String, 描述
├── cost       Decimal, 费用（元）
├── startDate  Date, 开始日期
├── endDate    Date, 完成日期（可为空表示未完成）
├── vendor     String, 维修商
├── createdAt  DateTime
└── updatedAt  DateTime

AssetLog（资产操作日志）
├── id         Long, PK, 自增
├── assetId    Long, FK → Asset
├── action     Enum, 操作类型（ CREATE / UPDATE / ASSIGN / UNASSIGN / MAINTENANCE / RETIRE / RESTORE ）
├── operator   String, 操作人
├── detail     String, 操作详情（JSON 字符串）
├── createdAt  DateTime, 日志时间
└── indexName  String, ES 索引名（如 "ams-logs-2026-05-13"）
```

### 2.2 API 设计

**资产管理**

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/assets | 分页查询资产列表（支持 category, status, keyword 过滤） |
| GET | /api/assets/{id} | 获取单个资产详情 |
| POST | /api/assets | 创建资产 |
| PUT | /api/assets/{id} | 更新资产 |
| DELETE | /api/assets/{id} | 软删除资产 |
| POST | /api/assets/{id}/assign | 领用资产（assigneeId） |
| POST | /api/assets/{id}/unassign | 归还资产 |
| POST | /api/assets/{id}/retire | 报废资产 |

**员工管理**

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/employees | 查询员工列表 |
| GET | /api/employees/{id} | 获取员工详情 |
| POST | /api/employees | 创建员工 |
| PUT | /api/employees/{id} | 更新员工 |
| DELETE | /api/employees/{id} | 删除员工 |

**维修记录**

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/assets/{assetId}/maintenance-records | 获取资产的维修记录 |
| POST | /api/assets/{assetId}/maintenance-records | 创建维修记录 |
| PUT | /api/maintenance-records/{id} | 更新维修记录（标记完成等） |

**仪表盘**

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/dashboard/stats | 资产统计（总数、各状态数量、各分类数量） |
| GET | /api/dashboard/recent-logs | 最近操作日志（最近20条） |

### 2.3 前端页面

| 页面 | 路由 | 功能 |
|------|------|------|
| 仪表盘 | / | 统计卡片 + 最近操作列表 |
| 资产列表 | /assets | 资产表格（分页、筛选、搜索） |
| 资产详情 | /assets/:id | 资产详情 + 维修记录 |
| 新建/编辑资产 | /assets/new, /assets/:id/edit | 资产表单 |
| 员工列表 | /employees | 员工表格 |
| 员工详情 | /employees/:id | 员工详情 + 名下资产 |

### 2.4 日志规范

所有资产操作写入结构化日志，同时落 MySQL 和 Elasticsearch：

**Elasticsearch 索引命名：** `ams-logs-YYYY-MM-DD`（按天滚动）

**日志文档结构：**
```json
{
  "id": 123,
  "assetId": 1,
  "assetCode": "PC-2024-0001",
  "action": "ASSIGN",
  "operator": "admin",
  "detail": "{\"assigneeId\": 5, \"assigneeName\": \"张三\"}",
  "createdAt": "2026-05-13T15:30:00Z",
  "@timestamp": "2026-05-13T15:30:00Z"
}
```

---

## 3. 技术架构

### 3.1 目录结构

```
ams/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/ams/
│   │       ├── Am sApplication.java
│   │       ├── config/         # 配置类
│   │       ├── controller/     # REST Controller
│   │       ├── service/        # Service
│   │       ├── repository/      # JPA Repository
│   │       ├── entity/         # JPA Entity
│   │       ├── dto/            # Request/Response DTO
│   │       ├── enums/          # 枚举类
│   │       └── elasticsearch/   # ES 相关
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/       # Flyway 迁移脚本
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                  # React 前端
│   ├── src/
│   │   ├── api/               # API 调用封装
│   │   ├── pages/             # 页面组件
│   │   ├── components/        # 公共组件
│   │   ├── types/            # TypeScript 类型
│   │   └── App.tsx
│   ├── package.json
│   ├── vite.config.ts
│   └── Dockerfile
│
├── docker-compose.yml         # 整体部署
├── .env.example               # 环境变量示例
└── README.md
```

### 3.2 后端依赖（Java 21 + Spring Boot 3）

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.x | 框架 |
| Spring Data JPA | - | ORM |
| MySQL Driver | 8.x | 数据库 |
| Flyway | - | 数据库迁移 |
| Spring Data Elasticsearch | - | ES 集成 |
| Lombok | - | 简化代码 |
| MapStruct | - | DTO 映射 |
| validation | - | 参数校验 |
| springdoc-openapi | 2.x | API 文档 |

### 3.3 前端依赖

| 依赖 | 用途 |
|------|------|
| React 18 | 框架 |
| TypeScript | 类型安全 |
| Vite | 构建工具 |
| Ant Design 5 | UI 组件库 |
| React Router 6 | 路由 |
| Axios | HTTP 客户端 |
| @tanstack/react-query | 数据请求/缓存 |

### 3.4 数据库

- **MySQL 8.x**，InnoDB 引擎
- Flyway 管理迁移脚本，初始版本建所有表
- 数据库名：`ams_db`，字符集 `utf8mb4`

### 3.5 Elasticsearch

- **Elasticsearch 8.x**
- 每日索引：`ams-logs-YYYY-MM-DD`
- 连接走 `spring.elasticsearch.uris` 配置

### 3.6 Docker 部署

```
docker-compose.yml 包含：
- MySQL 8
- Elasticsearch 8
- backend (Spring Boot JAR)
- frontend (Nginx 静态托管)
```

---

## 4. 验收标准

### 4.1 功能验收

| # | 场景 | 预期结果 |
|---|------|---------|
| F1 | 新建资产 | 资产入库，status=IN_STOCK，日志写入 ES |
| F2 | 领用资产 | status→IN_USE，日志记录 assigneeId |
| F3 | 归还资产 | status→IN_STOCK，assigneeId 清空 |
| F4 | 维修登记 | 创建 MaintenanceRecord，status→MAINTENANCE |
| F5 | 报废资产 | status→RETIRED，不可再领用 |
| F6 | 删除资产 | 软删除（deleted=true），日志记录 |
| F7 | 仪表盘 | 显示总数/各状态/各分类数量 |
| F8 | ES 日志查询 | 日志按日期索引，可查询 |

### 4.2 技术验收

| # | 检查项 | 验收方式 |
|---|--------|---------|
| T1 | 后端启动无报错 | `docker-compose up -d backend` 后 health check 通过 |
| T2 | 前端构建成功 | `npm run build` 无错误 |
| T3 | 数据库迁移执行 | Flyway 迁移日志无错误 |
| T4 | ES 索引自动创建 | 写入一条日志后索引存在 |
| T5 | Docker 部署 | `docker-compose up -d` 全部容器 running |
| T6 | API 文档可访问 | `GET /swagger-ui.html` 返回 200 |

---

## 5. MVP 实现优先级

**Phase 1（核心回路）：**
1. 项目骨架 + Docker Compose（MySQL + backend + frontend + ES）
2. Asset / Employee CRUD API + 页面
3. 领用/归还 API + 页面
4. 仪表盘
5. 操作日志（MySQL + ES 双写）

**Phase 2（完善）：**
6. 维修记录
7. 报废功能
8. ES 日志查询页面

**Phase 3（企业级功能）：**
9. 权限管理（RBAC）— 角色、权限、菜单权限控制 ⬜ 进行中
10. ~~部门管理 — 部门树形结构、部门资产统计~~ ✅ 已完成
11. ~~批量操作 — 批量领用/归还/报废/转移~~ ✅ 已完成
12. ~~导入/导出 Excel — 资产批量导入、报表导出~~ ✅ 已完成
13. 标签打印 — 条码/二维码生成与打印 ⬜ 待开发
14. 审批流程 — 领用审批、维修审批、报废审批 ⬜ 待开发
15. 消息通知 — 审批结果通知、维保到期提醒 ⬜ 待开发
16. 数据备份 — 数据库定期备份策略 ⬜ 待开发
17. 高级报表 — 资产履历、成本分析、分布统计 ⬜ 待开发

---

## 6.1 RBAC 权限模型设计（详细）

### 6.1.1 数据模型

```
Role（角色）
├── id              Long, PK
├── code            String, 唯一编码（如 "ADMIN", "ASSET_MANAGER", "VIEWER"）
├── name            String, 显示名称（如 "管理员", "资产管理员"）
├── description     String, 描述
├── createdAt       DateTime
└── updatedAt       DateTime

Permission（权限）
├── id              Long, PK
├── code            String, 唯一编码（如 "ASSET_CREATE", "ASSET_DELETE"）
├── name            String, 显示名称
├── resource        String, 资源类型（如 "ASSET", "EMPLOYEE"）
├── action          String, 操作（如 "CREATE", "READ", "UPDATE", "DELETE"）
└── description     String

RolePermission（角色-权限关联）
├── id              Long, PK
├── roleId          Long, FK → Role
└── permissionId    Long, FK → Permission

UserRole（用户-角色关联）
├── id              Long, PK
├── userId          Long, FK → Employee（复用 Employee 表作为用户）
└── roleId          Long, FK → Role
```

### 6.1.2 预定义角色

| 角色 | 代码 | 权限描述 |
|------|------|---------|
| 管理员 | ADMIN | 全部权限 |
| 资产管理员 | ASSET_MANAGER | 资产管理（CRUD）、批量操作、导入导出 |
| 维修员 | MAINTENANCE_STAFF | 维修记录管理、资产查看 |
| 普通员工 | EMPLOYEE | 查看自己名下资产、申请领用 |
| 只读用户 | VIEWER | 全部只读 |

### 6.1.3 权限清单

| 权限代码 | 资源 | 操作 |
|----------|------|------|
| ASSET_CREATE | ASSET | CREATE |
| ASSET_READ | ASSET | READ |
| ASSET_UPDATE | ASSET | UPDATE |
| ASSET_DELETE | ASSET | DELETE |
| ASSET_ASSIGN | ASSET | ASSIGN |
| ASSET_IMPORT_EXPORT | ASSET | IMPORT_EXPORT |
| EMPLOYEE_CREATE | EMPLOYEE | CREATE |
| EMPLOYEE_READ | EMPLOYEE | READ |
| EMPLOYEE_UPDATE | EMPLOYEE | UPDATE |
| EMPLOYEE_DELETE | EMPLOYEE | DELETE |
| DEPARTMENT_CREATE | DEPARTMENT | CREATE |
| DEPARTMENT_READ | DEPARTMENT | READ |
| DEPARTMENT_UPDATE | DEPARTMENT | UPDATE |
| DEPARTMENT_DELETE | DEPARTMENT | DELETE |
| MAINTENANCE_CREATE | MAINTENANCE | CREATE |
| MAINTENANCE_READ | MAINTENANCE | READ |
| MAINTENANCE_UPDATE | MAINTENANCE | UPDATE |
| MAINTENANCE_APPROVE | MAINTENANCE | APPROVE |
| DASHBOARD_VIEW | DASHBOARD | READ |

### 6.1.4 API 层权限控制

- JWT Token 认证（登录后获取）
- 后端使用 Spring Security + Method Security
- 注解方式：`@PreAuthorize("hasAuthority('ASSET_CREATE')")`
- 前端路由守卫：根据用户角色显示/隐藏菜单项

### 6.1.5 实施计划

Phase 3.1（RBAC 基础）：
1. 创建 Role、Permission、RolePermission 实体和表
2. 实现登录认证 API（JWT）
3. 实现角色管理 CRUD
4. 在现有 API 上添加权限注解

Phase 3.2（前端权限）：
5. 前端登录页面
6. 路由守卫
7. 菜单动态显示
8. 按钮级别权限控制

---

## 6. 风险与边界

| 风险 | 缓解 |
|------|------|
| ES 版本兼容性 | 使用 Spring Data Elasticsearch 兼容的 ES 8.x |
| 前端状态管理复杂度 | MVP 用 React Query + localStorage，不过渡设计 |
| 权限控制 | MVP 不做，所有人可写；后续加 RBAC |
