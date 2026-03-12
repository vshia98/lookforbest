#!/bin/sh
set -e

DOMAIN="${DOMAIN:-localhost}"

# 替换配置文件中的 ${DOMAIN} 占位符
for conf in /etc/nginx/conf.d/*.conf; do
    sed -i "s/\${DOMAIN}/$DOMAIN/g" "$conf"
done

# 若 SSL 证书不存在，生成自签名证书用于启动（certbot 后续替换）
CERT_DIR="/etc/letsencrypt/live/${DOMAIN}"
if [ ! -f "${CERT_DIR}/fullchain.pem" ]; then
    echo "SSL cert not found for ${DOMAIN}, generating self-signed cert..."
    mkdir -p "${CERT_DIR}"
    openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
        -keyout "${CERT_DIR}/privkey.pem" \
        -out "${CERT_DIR}/fullchain.pem" \
        -subj "/CN=${DOMAIN}" 2>/dev/null
    cp "${CERT_DIR}/fullchain.pem" "${CERT_DIR}/chain.pem"
fi

exec nginx -g "daemon off;"
