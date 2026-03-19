#!/bin/bash
# LookForBest 一键生产部署脚本
# 用法：./scripts/deploy.sh [--skip-build] [--skip-ssl]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SKIP_BUILD=false
SKIP_SSL=false

for arg in "$@"; do
  case "$arg" in
    --skip-build) SKIP_BUILD=true ;;
    --skip-ssl)   SKIP_SSL=true  ;;
  esac
done

cd "$PROJECT_DIR"

# ---- 颜色输出 ----
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

# ---- 前置检查 ----
info "检查前置条件..."
command -v docker  >/dev/null 2>&1 || error "需要安装 docker"
command -v docker  >/dev/null 2>&1 && docker compose version >/dev/null 2>&1 || error "需要安装 docker compose v2"

[ -f ".env" ] || error ".env 文件不存在，请先执行: cp .env.example .env 并填写实际值"

# 检查必填环境变量
source .env
[ -z "${DOMAIN:-}" ]          && error "DOMAIN 未设置"
[ -z "${DB_PASSWORD:-}" ]     && error "DB_PASSWORD 未设置"
[ -z "${JWT_SECRET:-}" ]      && error "JWT_SECRET 未设置"
[ -z "${MINIO_ACCESS_KEY:-}" ] && error "MINIO_ACCESS_KEY 未设置"
[ -z "${MINIO_SECRET_KEY:-}" ] && error "MINIO_SECRET_KEY 未设置"

# 安全检查：确保没有使用默认值
[[ "${DB_PASSWORD}" == *"CHANGE_ME"* ]]     && error "请修改 DB_PASSWORD 的默认值"
[[ "${JWT_SECRET}" == *"CHANGE_ME"* ]]      && error "请修改 JWT_SECRET 的默认值"
[[ "${MINIO_ACCESS_KEY}" == *"CHANGE_ME"* ]] && error "请修改 MINIO_ACCESS_KEY 的默认值"
[[ "${MINIO_SECRET_KEY}" == *"CHANGE_ME"* ]] && error "请修改 MINIO_SECRET_KEY 的默认值"

info "环境变量检查通过 (DOMAIN=${DOMAIN})"

# ---- 构建镜像 ----
if [ "$SKIP_BUILD" = false ]; then
  info "构建 Docker 镜像..."
  docker compose build --no-cache backend frontend recommend elasticsearch
else
  warn "跳过构建（--skip-build）"
fi

# ---- 启动基础服务 ----
info "启动基础服务（MySQL / Redis / MinIO）..."
docker compose up -d mysql redis minio

info "等待基础服务就绪..."
for svc in mysql redis; do
  echo -n "  等待 $svc..."
  for i in $(seq 1 30); do
    status=$(docker inspect --format='{{.State.Health.Status}}' "lookforbest-${svc}" 2>/dev/null || echo "unknown")
    if [ "$status" = "healthy" ]; then
      echo " OK"
      break
    fi
    if [ $i -eq 30 ]; then
      echo " TIMEOUT"
      error "$svc 健康检查超时，请检查日志：docker compose logs $svc"
    fi
    sleep 3
  done
done

echo -n "  等待 minio..."
for i in $(seq 1 40); do
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:9000/minio/health/live" || echo "000")
  if [ "$HTTP_CODE" = "200" ]; then
    echo " OK"
    break
  fi
  if [ $i -eq 40 ]; then
    echo " TIMEOUT"
    error "minio 健康检查超时，请检查日志：docker compose logs minio"
  fi
  sleep 3
done

# ---- 启动后端与推荐服务 ----
info "启动后端与推荐服务..."
docker compose up -d backend recommend

echo -n "  等待 backend..."
for i in $(seq 1 40); do
  status=$(docker inspect --format='{{.State.Health.Status}}' "lookforbest-backend" 2>/dev/null || echo "unknown")
  if [ "$status" = "healthy" ]; then
    echo " OK"
    break
  fi
  if [ $i -eq 40 ]; then
    echo " TIMEOUT"
    error "backend 启动超时，请检查日志：docker compose logs backend"
  fi
  sleep 5
done

# ---- 启动前端（由宝塔 Nginx 反向代理访问）----
info "启动前端..."
docker compose up -d frontend

sleep 5

# ---- SSL 证书 ----
# 说明：当前服务器通过宝塔 Nginx 管理 SSL 证书，本脚本不再自动申请/续期证书。
warn "SSL 证书请在宝塔面板中为域名 ${DOMAIN} 配置（本脚本不再调用 ssl-init.sh）"

# ---- 验证部署 ----
info "验证服务状态..."
docker compose ps

info "测试 API 可达性..."
PROTOCOL="http"
[ "${DOMAIN}" != "localhost" ] && PROTOCOL="https"
BASE_URL="${PROTOCOL}://${DOMAIN}"

sleep 3
HTTP_CODE=$(curl -sk -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/robots?page=0&size=1" || echo "000")
if [[ "$HTTP_CODE" =~ ^(200|206)$ ]]; then
  info "API 正常响应 (HTTP ${HTTP_CODE})"
else
  warn "API 响应码 ${HTTP_CODE}（可能是 SSL/域名问题，请手动验证）"
fi

info "====================================================="
info " LookForBest 部署完成！"
info " 访问地址：${BASE_URL}"
info " MinIO 控制台：http://$(hostname -I | awk '{print $1}'):9001"
info " 查看日志：docker compose logs -f"
info "====================================================="
