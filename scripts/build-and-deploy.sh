#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Find mvn - try PATH first, then common locations
if command -v mvn &>/dev/null; then
    MVN="mvn"
elif [ -x "/home/yuhang/.hermes/profiles/independent-developer/home/.local/maven/bin/mvn" ]; then
    MVN="/home/yuhang/.hermes/profiles/independent-developer/home/.local/maven/bin/mvn"
else
    echo "ERROR: mvn not found"
    exit 1
fi

# Set JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/home/yuhang/.hermes/profiles/independent-developer/home/.local/java" ]; then
        export JAVA_HOME="/home/yuhang/.hermes/profiles/independent-developer/home/.local/java"
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

echo "=== AMS Build and Deploy Script ==="
echo "Project dir: $PROJECT_DIR"

# --- 1. Backend: Maven build ---
echo ""
echo "=== Building Backend (mvn package -DskipTests) ==="
cd "$PROJECT_DIR/backend"
$MVN package -DskipTests -B -q

# --- 2. Frontend: npm build ---
echo ""
echo "=== Building Frontend (npm run build) ==="
cd "$PROJECT_DIR/frontend"
npm run build

# --- 3. Copy artifacts to backend/ and frontend/ (docker build context = project root) ---
echo ""
echo "=== Copying artifacts to backend/ and frontend/ (context=.) ==="
mkdir -p "$PROJECT_DIR/backend"
mkdir -p "$PROJECT_DIR/frontend"

# Backend JAR
cp "$PROJECT_DIR/backend/target"/*.jar "$PROJECT_DIR/backend/app.jar"
echo "Backend JAR: $PROJECT_DIR/backend/app.jar"

# Frontend dist - already at frontend/dist, no need to copy
echo "Frontend dist: $PROJECT_DIR/frontend/dist"

# --- 4. Build Docker images ---
echo ""
echo "=== Building Docker Images (context=.) ==="

# Backend image (context=project root, dockerfile=docker/Dockerfile.backend)
docker build -t ams-backend:latest -f docker/Dockerfile.backend .
echo "Built: ams-backend:latest"

# Frontend image (context=project root, dockerfile=docker/Dockerfile.frontend)
docker build -t ams-frontend:latest -f docker/Dockerfile.frontend .
echo "Built: ams-frontend:latest"

# --- 5. Deploy with docker compose ---
echo ""
echo "=== Deploying with docker compose ==="
cd "$PROJECT_DIR"

# Stop existing containers
docker compose down

# Start containers (rebuild if needed)
docker compose up -d --build
echo "Containers started."

# --- 6. Wait for health check ---
echo ""
echo "=== Waiting for backend health check ==="
MAX_RETRIES=40
RETRY_INTERVAL=10
retries=0

while [ $retries -lt $MAX_RETRIES ]; do
    if docker compose exec -T backend curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "Backend is healthy!"
        break
    fi
    retries=$((retries + 1))
    echo "Waiting for backend to be healthy... ($retries/$MAX_RETRIES)"
    sleep $RETRY_INTERVAL
done

if [ $retries -eq $MAX_RETRIES ]; then
    echo "ERROR: Backend health check failed after $MAX_RETRIES retries."
    exit 1
fi

echo ""
echo "=== Build and Deploy Complete ==="
docker compose ps
docker images | grep ams
