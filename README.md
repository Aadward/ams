# AMS — Asset Management System

企业 IT 资产管理系统（Alpha 版本）

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
├── backend/          # Spring Boot 后端
├── frontend/         # React 前端
├── docker-compose.yml
├── .env.example
└── docs/prd/         # PRD 文档
```

## 主要功能

- 资产管理（CRUD + 领用/归还/报废）
- 员工管理
- 维修记录
- 仪表盘统计
- 操作日志（写入 MySQL + Elasticsearch，按日索引）

## API

API 文档见 Swagger UI：http://localhost:8080/swagger-ui.html
