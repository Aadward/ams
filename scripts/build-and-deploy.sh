#!/bin/bash
#==============================================================================
# AMS 构建部署脚本 — 严谨版本
#
# 每次构建前先清理旧容器/镜像，确保 JAR 与源码完全一致
#
# 用法: ./scripts/build-and-deploy.sh [--skip-tests]
#==============================================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_DIR/backend"
JAR_NAME="ams-backend-1.0.0-SNAPSHOT.jar"
JAR_PATH="$BACKEND_DIR/target/$JAR_NAME"
DOCKERFILE="$PROJECT_DIR/docker/Dockerfile.backend"

SKIP_TESTS=""
if [[ "$1" == "--skip-tests" ]]; then
    SKIP_TESTS="-DskipTests"
fi

echo "==== [1/7] 清理旧容器 ===="
cd "$PROJECT_DIR"
docker compose rm -sf backend 2>/dev/null || true
docker image rm -f ams-backend:latest 2>/dev/null || true

echo "==== [2/7] Maven 编译打包 ===="
cd "$BACKEND_DIR"
mvn clean package $SKIP_TESTS -q

echo "==== [3/7] 验证 JAR 存在 ===="
if [[ ! -f "$JAR_PATH" ]]; then
    echo "ERROR: JAR 文件不存在: $JAR_PATH"
    exit 1
fi
echo "JAR: $(ls -lh "$JAR_PATH" | awk '{print $5, $9}')"

echo "==== [4/7] 验证 Migration 文件 CRC ===="
python3 "$SCRIPT_DIR/verify-migrations.py" "$JAR_PATH" "$BACKEND_DIR/src/main/resources/db/migration"

echo "==== [5/7] Docker 构建（no-cache） ===="
cd "$PROJECT_DIR"
docker compose build --no-cache backend

echo "==== [6/7] 验证 Docker 镜像 CRC ===="
# 从新构建的镜像中提取 JAR 并验证 V5 migration CRC
IMG_CRC=$(docker run --rm --entrypoint sh ams-backend:latest -c 'cat /app/app.jar' 2>/dev/null | \
    python3 -c "
import sys, zipfile, io, zlib
jar_data = sys.stdin.buffer.read()
with zipfile.ZipFile(io.BytesIO(jar_data)) as z:
    for name in z.namelist():
        if 'V5__add_approval_id_to_maintenance_record' in name:
            data = z.read(name)
            crc = zlib.crc32(data) & 0xffffffff
            print(crc)
")
echo "  V5 CRC in container: $IMG_CRC"
if [[ "$IMG_CRC" != "2539394677" ]]; then
    echo "  ERROR: Expected 2539394677"
    exit 1
fi
echo "  CRC 验证通过"

echo "==== [7/7] 启动服务 ===="
docker compose up -d backend
echo ""
echo "==== 完成 ===="
docker compose ps backend 2>&1 | grep -v Warning
