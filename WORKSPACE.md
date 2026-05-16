# AMS 工作空间

## 项目信息

| 项目 | 值 |
|------|---|
| 项目名称 | AMS 资产管理系统 |
| 类型 | Spring Boot + Vue3 全栈应用 |
| 工作目录 | `/home/yuhang/.hermes/profiles/independent-developer/workspace/ams` |

## 关键文件路径

| 用途 | 路径 |
|------|------|
| 看板 | `kanban.md` |
| PRD 文档 | `prds/` 目录 |
| Kanban 脚本 | `/home/yuhang/.hermes/profiles/independent-developer/scripts/kanban_lifecycle.py` |

## 看板状态流转

```
todo → in_progress → completed → done（已验收）
```

| 状态 | 操作者 | 含义 |
|------|--------|------|
| `todo` | PM（新增） | 新任务已识别 |
| `in_progress` | Developer | 开发者开始工作 |
| `completed` | Developer | 代码+测试+git push完成 |
| `done（已验收）` | QA | 通过验收 |

## Kanban 操作命令

```bash
# 脚本位置
KANBAN_SCRIPT="/home/yuhang/.hermes/profiles/independent-developer/scripts/kanban_lifecycle.py"
KANBAN_FILE="/path/to/workspace/ams/kanban.md"  # 相对路径或绝对路径

# Dev: 移动任务
ROLE=dev python "$KANBAN_SCRIPT" "$KANBAN_FILE" todo in_progress "<任务名>"
ROLE=dev python "$KANBAN_SCRIPT" "$KANBAN_FILE" in_progress completed "<任务名>"

# PM: 新增 todo
ROLE=pm python "$KANBAN_SCRIPT" "$KANBAN_FILE" add-todo "<任务描述>" --prd "prds/YYYY-MM-DD-功能名称.md"

# QA: 验收或打回
ROLE=qa python "$KANBAN_SCRIPT" "$KANBAN_FILE" completed "done（已验收）" "<任务名>"
ROLE=qa python "$KANBAN_SCRIPT" "$KANBAN_FILE" completed todo "<任务名>" --note "<打回原因>"
```

## 看板角色选择规则

| 条件 | 选择角色 |
|------|---------|
| 有 `completed`（待验收） | QA |
| 有 `todo`（待处理） | Developer |
| 既无 `completed` 也无 `todo` | PM（兜底） |

## 技术栈

### 后端
- Java 21 + Spring Boot
- JPA / Hibernate
- MySQL
- Flyway（数据库迁移）
- Maven

### 前端
- Vue 3 + TypeScript
- Vite
- Ant Design Vue
- HashRouter（`/#/` 路径）

### DevOps
- Docker + Docker Compose
- nginx（前端容器）

## 项目结构

```
ams/
├── backend/
│   └── src/main/java/com/ams/
│       ├── entity/      # JPA 实体
│       ├── repository/  # 数据访问
│       ├── service/     # 业务逻辑
│       ├── controller/  # REST API
│       ├── dto/         # 数据传输对象
│       ├── enums/       # 枚举类
│       └── config/      # 配置类
├── frontend/
│   └── src/
│       ├── pages/       # 页面组件
│       ├── api/         # API 客户端
│       └── App.tsx      # 路由配置
└── docker-compose.yml
```

## 常用开发命令

```bash
# 后端构建
cd /home/yuhang/.hermes/profiles/independent-developer/workspace/ams
JAVA_HOME=/home/yuhang/.local/java \
  /home/yuhang/.local/maven/bin/mvn \
  -f backend/pom.xml clean package -DskipTests -q

# 前端构建
cd frontend && npm run build

# Docker 重建
docker compose build --no-cache backend frontend
docker rm -sf ams-backend ams-frontend
docker compose up -d backend frontend

# 后端健康检查
curl -s http://localhost:8080/actuator/health
```
