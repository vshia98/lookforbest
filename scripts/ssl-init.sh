#!/bin/bash
# SSL 证书初始化脚本（首次部署使用）
# 用法：DOMAIN=yourdomain.com EMAIL=you@example.com ./scripts/ssl-init.sh

set -e

DOMAIN="${DOMAIN:-example.com}"
EMAIL="${EMAIL:-admin@example.com}"
COMPOSE_FILE="$(dirname "$0")/../docker-compose.yml"

echo "==> 初始化 SSL 证书：${DOMAIN}"

# 第一步：启动 nginx（使用自签名证书，仅开放 80 端口）
echo "==> 启动 nginx 服务..."
docker compose -f "$COMPOSE_FILE" up -d nginx

# 等待 nginx 就绪
sleep 3

# 第二步：申请 Let's Encrypt 证书
echo "==> 申请 Let's Encrypt 证书..."
docker compose -f "$COMPOSE_FILE" run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    -d "$DOMAIN"

# 第三步：重载 nginx 加载真实证书
echo "==> 重载 nginx..."
docker compose -f "$COMPOSE_FILE" exec nginx nginx -s reload

echo "==> SSL 证书申请成功！"
echo "==> 证书路径：/etc/letsencrypt/live/${DOMAIN}/"
echo "==> 证书将每 90 天自动续期（由 certbot 定时任务处理）"
