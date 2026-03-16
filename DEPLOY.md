# LookForBest 服务器部署指南

## 服务器要求

- **系统**: Ubuntu 22.04+ / CentOS 8+
- **配置**: 4核 8GB 内存 50GB 硬盘（最低 2核4G）
- **软件**: Docker 24+ / Docker Compose v2+
- **域名**: 已解析到服务器 IP，用于 HTTPS 证书

## 一、安装 Docker

```bash
# Ubuntu
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# 重新登录使 docker 组生效
```

## 二、拉取代码

```bash
git clone https://github.com/你的仓库/lookforbest.git
cd lookforbest
```

## 三、配置环境变量

```bash
cp .env.example .env
nano .env
```

**必须修改的关键配置：**

```env
# 你的域名
DOMAIN=yourdomain.com
SSL_EMAIL=admin@yourdomain.com

# 数据库密码（必须改强密码）
DB_PASSWORD=你的强密码_16位以上
DB_ROOT_PASSWORD=你的root强密码

# JWT 密钥（必须改）
JWT_SECRET=$(openssl rand -base64 48)

# Redis 密码
REDIS_PASSWORD=你的redis密码

# MinIO 密码
MINIO_ACCESS_KEY=你的minio用户名
MINIO_SECRET_KEY=你的minio密码_16位以上

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# 站点 URL
SITE_BASE_URL=https://yourdomain.com
```

## 四、首次启动

```bash
# 启动所有服务（首次会自动构建镜像，约 5-10 分钟）
docker compose up -d

# 查看启动日志
docker compose logs -f

# 等待所有服务变 healthy
docker compose ps
```

**服务启动顺序**: MySQL → Redis → MinIO → Elasticsearch → Backend → Frontend → Crawler → Nginx

## 五、初始化数据库

Backend 首次启动会通过 Flyway 自动建表。如果 Flyway 迁移文件缺失，手动导入：

```bash
docker compose exec mysql mysql -u lookforbest -p你的密码 lookforbest < backend/src/main/resources/db/init_schema.sql
```

## 六、创建管理员账号

```bash
# 进入后端容器执行，或通过 API 注册后在数据库中改角色
docker compose exec mysql mysql -ulookforbest -p你的密码 lookforbest -e "
  INSERT INTO users (username, email, password_hash, role, email_verified)
  VALUES ('admin', 'admin@yourdomain.com', '\$2b\$12\$这里填bcrypt哈希', 'superadmin', 1);
"
```

或者先通过网页注册，再改角色：
```bash
docker compose exec mysql mysql -ulookforbest -p你的密码 lookforbest -e "
  UPDATE users SET role='superadmin' WHERE username='admin';
"
```

## 七、申请 SSL 证书

```bash
# 首次申请（确保域名已解析到服务器 + 80 端口可访问）
docker compose exec certbot certbot certonly \
  --webroot --webroot-path=/var/www/certbot \
  -d yourdomain.com \
  --email admin@yourdomain.com \
  --agree-tos --no-eff-email

# 重载 nginx 使证书生效
docker compose exec nginx nginx -s reload
```

证书会自动续期（certbot 容器每 12 小时检查一次）。

## 八、爬取数据

```bash
# 全量爬取 320+ 机器人（含图片下载到 MinIO + 厂商 logo）
docker compose exec crawler python scripts/crawl_aixzd.py --pages 6

# 约 15-20 分钟完成
# 查看进度
docker compose logs -f crawler
```

爬虫会自动：
1. 爬取 6 页列表（~320 机器人）
2. 下载封面图 + 厂商 logo → 上传 MinIO
3. 写入数据库（机器人 + 厂商 + 分类 + 标签）
4. 补抓缺失的厂商 logo（从官网 favicon）

## 九、验证部署

```bash
# 检查所有服务状态
docker compose ps

# 检查后端健康
curl -s http://localhost:8080/actuator/health

# 检查数据
docker compose exec mysql mysql -ulookforbest -p你的密码 lookforbest -e "
  SELECT '机器人' AS item, COUNT(*) AS cnt FROM robots
  UNION SELECT '厂商', COUNT(*) FROM manufacturers
  UNION SELECT '分类', COUNT(*) FROM robot_categories;
"

# 访问网站
# https://yourdomain.com
```

## 常用运维命令

```bash
# 查看日志
docker compose logs -f backend
docker compose logs -f crawler

# 重启单个服务
docker compose restart backend

# 更新代码后重新构建
git pull
docker compose up -d --build

# 备份数据库
docker compose exec mysql mysqldump -ulookforbest -p你的密码 lookforbest > backup_$(date +%Y%m%d).sql

# 备份 MinIO 数据
docker compose exec minio mc alias set local http://localhost:9000 你的key 你的secret
docker compose exec minio mc mirror local/lookforbest /tmp/minio-backup/

# 进入容器调试
docker compose exec backend sh
docker compose exec crawler bash
```

## 架构图

```
用户浏览器
    ↓
Nginx (80/443)
    ├── /api/*      → Backend (8080) → MySQL + Redis
    ├── /lookforbest/* → MinIO (9000)  图片/文件
    └── /*          → Frontend (静态 HTML/JS/CSS)

Crawler (8002) → MySQL + MinIO（定时爬取）
Elasticsearch (9200)（全文搜索，可选）
```

## 资源占用参考

| 服务 | 内存限制 | 说明 |
|------|---------|------|
| MySQL | 1GB | 数据库 |
| Redis | 256MB | 缓存 |
| MinIO | 512MB | 文件存储 |
| Elasticsearch | 1.5GB | 全文搜索（可选） |
| Backend | 1GB | Spring Boot |
| Frontend | 128MB | Nginx 静态 |
| Crawler | 256MB | Python 爬虫 |
| **总计** | **~4.5GB** | 不含 ES 约 3GB |

> 如果服务器内存不足，可以在 `.env` 中设置 `ELASTICSEARCH_ENABLED=false` 关闭 ES，搜索会自动降级为 MySQL LIKE 查询。
