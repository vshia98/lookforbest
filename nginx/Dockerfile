FROM nginx:1.25-alpine

# 安装 openssl 用于生成临时证书
RUN apk add --no-cache openssl

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf
COPY conf.d/ /etc/nginx/conf.d/

# 创建 certbot webroot 目录
RUN mkdir -p /var/www/certbot

# 创建证书目录（certbot 挂载点）
RUN mkdir -p /etc/letsencrypt

# 启动脚本：替换域名变量后启动 nginx
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 80 443

ENTRYPOINT ["/entrypoint.sh"]
