#!/bin/bash
#==============================================================================
# AMS 构建部署脚本
#
# 用法: ./scripts/build-and-deploy.sh [--skip-tests]
#==============================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_DIR/backend"
JAR_NAME="ams-backend-1.0.0-SNAPSHOT.jar"
JAR_PATH="$BACKEND_DIR/target/$JAR_NAME"

SKIP_TESTS=""
if [[ "$1" == "--skip-tests" ]]; then
    SKIP_TESTS="-DskipTests"
fi

echo "==== [1/6] 清理旧容器 ===="
cd "$PROJECT_DIR"
docker compose rm -sf backend 2>/dev/null || true
docker image rm -f ams-backend:latest 2>/dev/null || true

echo "==== [2/6] Maven 编译打包 ===="
cd "$BACKEND_DIR"
mvn clean package $SKIP_TESTS -q

echo "==== [3/6] 验证 JAR 存在 ===="
if [[ ! -f "$JAR_PATH" ]]; then
    echo "ERROR: JAR 文件不存在: $JAR_PATH"
    exit 1
fi
echo "JAR: $(ls -lh "$JAR_PATH" | awk '{print $5, $9}')"

echo "==== [4/6] Docker 构建 ===="
cd "$PROJECT_DIR"
docker compose build --no-cache backend

echo "==== [5/6] 启动服务 ===="
docker compose up -d backend

echo "==== [6/6] 完成 ===="
docker compose ps backend 2>&1 | grep -v Warning
