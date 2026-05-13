#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== AMS Build Script ==="
echo "Project dir: $PROJECT_DIR"

# --- Backend: 本地 Maven 构建 ---
echo ""
echo "=== Building Backend (Java21 + Maven) ==="
cd "$PROJECT_DIR/backend"

# 本地执行 mvn package（利用本机 .m2 缓存）
mvn package -DskipTests -B -q

# --- Frontend: 本地 npm 构建 ---
echo ""
echo "=== Building Frontend (Node22 + npm) ==="
cd "$PROJECT_DIR/frontend"

# 本地执行 npm run build（利用本机 node_modules 缓存）
npm run build

# --- 复制构建产物到 docker 目录 ---
echo ""
echo "=== Copying build artifacts to docker/ ==="
mkdir -p "$PROJECT_DIR/docker/backend"
mkdir -p "$PROJECT_DIR/docker/frontend"

# 后端 JAR
cp "$PROJECT_DIR/backend/target"/*.jar "$PROJECT_DIR/docker/backend/app.jar"
echo "Backend JAR: $PROJECT_DIR/docker/backend/app.jar"

# 前端 dist
cp -r "$PROJECT_DIR/frontend/dist" "$PROJECT_DIR/docker/frontend/"
cp "$PROJECT_DIR/frontend/nginx.conf" "$PROJECT_DIR/docker/frontend/nginx.conf"
echo "Frontend dist: $PROJECT_DIR/docker/frontend/dist"

# --- 构建 Docker 镜像（build context = docker/）---
echo ""
echo "=== Building Docker Images ==="
cd "$PROJECT_DIR/docker"

# 构建后端镜像（只复制 app.jar，不下载任何依赖）
docker build -t ams-backend:latest -f Dockerfile.backend .

# 构建前端镜像（只复制 dist，不重新 npm install）
docker build -t ams-frontend:latest -f Dockerfile.frontend .

echo ""
echo "=== Build Complete ==="
docker images | grep ams