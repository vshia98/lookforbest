# 部署文档

## 生产环境部署（Docker Compose）

### 前置条件

- 服务器：Linux，2核4GB以上
- 已安装：Docker 24+、Docker Compose v2
- 域名已解析到服务器 IP

### 步骤 1：克隆代码

```bash
git clone <your-repo-url> lookforbest
cd lookforbest
```

### 步骤 2：配置环境变量

```bash
cp .env.example .env
```

编辑 `.env`，必填项：

```env
# 域名
DOMAIN=yourdomain.com
SSL_EMAIL=admin@yourdomain.com

# 数据库密码（修改默认值）
DB_PASSWORD=your_strong_db_password

# JWT 密钥（至少 32 字符，随机字符串）
JWT_SECRET=your_random_32_chars_secret_key_here

# MinIO 密钥（修改默认值）
MINIO_ACCESS_KEY=your_minio_key
MINIO_SECRET_KEY=your_minio_secret

# CORS 允许域名
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### 步骤 3：构建并启动服务

```bash
# 首次部署：构建所有镜像
docker compose build

# 启动所有服务（后台运行）
docker compose up -d

# 查看日志
docker compose logs -f
```

### 步骤 4：申请 SSL 证书

首次部署后执行（需要域名已正确解析）：

```bash
DOMAIN=yourdomain.com EMAIL=admin@yourdomain.com ./scripts/ssl-init.sh
```

脚本会自动：
1. 通过 Let's Encrypt 申请证书
2. 重载 Nginx 加载真实证书
3. HTTP → HTTPS 跳转自动生效

### 步骤 5：数据库初始化

MySQL 容器启动时自动执行 `backend/src/main/resources/db/init.sql`，无需手动操作。

### 验证部署

```bash
# 检查所有容器状态
docker compose ps

# 测试 API
curl https://yourdomain.com/api/v1/robots

# 检查后端健康
curl https://yourdomain.com/actuator/health
```

---

## 环境变量完整说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DOMAIN` | 生产域名 | `localhost` |
| `SSL_EMAIL` | Let's Encrypt 通知邮箱 | — |
| `DB_USERNAME` | MySQL 用户名 | `lookforbest` |
| `DB_PASSWORD` | MySQL 密码 | `lookforbest`（**必须修改**） |
| `JWT_SECRET` | JWT 签名密钥 | 内置测试值（**必须修改**） |
| `MINIO_ACCESS_KEY` | MinIO Access Key | `minioadmin`（**必须修改**） |
| `MINIO_SECRET_KEY` | MinIO Secret Key | `minioadmin`（**必须修改**） |
| `CORS_ALLOWED_ORIGINS` | CORS 允许来源（逗号分隔） | `http://localhost:5173,http://localhost:80` |
| `RATE_LIMIT_ENABLED` | 是否启用限流 | `true` |
| `RATE_LIMIT_DEFAULT` | 默认限流（次/分钟/IP） | `100` |
| `RATE_LIMIT_AUTH` | 认证接口限流（次/分钟/IP） | `20` |

---

## 更新部署

```bash
git pull
docker compose build backend frontend
docker compose up -d --no-deps backend frontend
```

## 备份数据库

```bash
docker compose exec mysql mysqldump -u lookforbest -p lookforbest > backup_$(date +%Y%m%d).sql
```

## 服务端口说明

| 服务 | 内部端口 | 对外暴露 |
|------|----------|----------|
| Nginx | 80, 443 | 80, 443 |
| Backend | 8080 | 仅内部（通过 Nginx） |
| Frontend | 80 | 仅内部（通过 Nginx） |
| MySQL | 3306 | 3306（可关闭） |
| Redis | 6379 | 6379（可关闭） |
| MinIO | 9000, 9001 | 9000, 9001 |
