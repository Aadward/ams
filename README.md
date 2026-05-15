# AMS — Asset Management System

企业 IT 资产管理系统（功能完备版）

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Java 21 + Spring Boot 3.4 + MySQL 8 + Elasticsearch 8 |
| 前端 | React 18 + TypeScript + Vite + Ant Design 5 |
| 部署 | Docker + Docker Compose |

## 快速启动

### 1. 环境要求

- Docker & Docker Compose
- JDK 21 (本地开发)
- Node.js 22 (本地开发)

### 2. Docker 部署（推荐）

```bash
cp .env.example .env
docker-compose up -d
```

访问：
- 前端：http://localhost
- 后端 API：http://localhost:8080
- Swagger UI：http://localhost:8080/swagger-ui.html
- Elasticsearch：http://localhost:9200
- Kibana：http://localhost:5601

### 3. 本地开发

**后端：**
```bash
cd backend
./mvnw spring-boot:run
```

**前端：**
```bash
cd frontend
npm install
npm run dev
```

## 项目结构

```
ams/
├── backend/          # Spring Boot 后端（Java 21）
├── frontend/         # React 前端（TypeScript + Vite）
├── docker/           # Dockerfile 和 nginx 配置
├── scripts/          # 运维脚本
├── prds/             # 产品需求文档
├── docs/             # 技术决策文档
├── docker-compose.yml
├── .env.example
└── README.md
```

## 主要功能

### 核心资产管理

- **资产管理**：CRUD + 领用/归还/报废/转移调拨
- **资产借用**：独立借用申请/审批流程，支持计划归还日期，超期未还自动通知
- **批量操作**：批量领用/归还/报废/转移
- **标签打印**：资产详情页生成二维码，支持浏览器打印
- **扫码盘点**：ScanController + H5 扫码页面，支持领用/归还/盘点

### 资产运维

- **维修记录**：维修申请→审批→执行→归档完整流程
- **定期盘点**：按部门/分类制定盘点计划，生成任务，支持移动端扫码确认，输出差异报告
- **折旧计算**：直线法折旧，台账/分类汇总/部门汇总报表，对接报废流程
- **维保到期提醒**：每日 8 点定时扫描，仪表盘卡片支持 7/15/30/60 天筛选

### 低值易耗品

- 分类管理 / 入库 / 出库 / 实时库存 / 库存预警 / 消耗报表

### 供应链

- **供应商管理**：增删改查 + 评级 + 联系方式
- **资产保险管理**：保险单 CRUD + 与资产关联 + 到期提醒 + 理赔记录 + 仪表盘视图
- **采购申请审批**（PRD 已完成）

### 组织架构

- **部门管理**：树形结构 + 部门资产统计
- **员工管理**：含角色分配

### 审批与通知

- **审批流程**：领用审批、维修审批、报废审批、借用审批
- **WebSocket 通知推送**：实时通知铃铛组件
- **通知页面**：查看所有通知

### 数据与分析

- **仪表盘**：资产状态分布、部门统计、维修费用汇总、维保到期、折旧汇总、保险概览
- **报表**：分类/状态/部门图表 + 维修费用汇总
- **操作日志**：MySQL + Elasticsearch 双写，按日索引
- **ES 日志查询页面**：支持按时间/操作类型/操作人筛选
- **数据备份**：备份管理页面 + backup-db.sh 脚本

### 系统安全

- **RBAC 权限控制**：UserRole、Employee.role、RoleController、secured APIs
- **JWT 认证**：登录认证 + Swagger 文档

## 运维脚本

```bash
# 数据库备份
./scripts/backup-db.sh

# 一键构建并部署（后端 jar + 前端 dist + Docker 镜像）
./scripts/build-and-deploy.sh

# 仅构建镜像
./scripts/build.sh
```

## API

API 文档见 Swagger UI：http://localhost:8080/swagger-ui.html
