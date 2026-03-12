# LookForBest — 全球机器人展示与对比平台

一站式机器人产品库：浏览、搜索、对比全球工业机器人与服务机器人，支持多端访问。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 API | Spring Boot 3.2 · Java 17 · JPA/Hibernate · MySQL 8 |
| 缓存 | Redis 7 |
| 对象存储 | MinIO |
| 管理后台 | Vue3 · Vite · TypeScript · Tailwind CSS |
| Flutter App | Flutter 3.27 · hooks_riverpod |
| 微信小程序 | Uni-app · Vue3 · TypeScript |
| 反向代理 | Nginx 1.25 + Let's Encrypt SSL |
| 容器化 | Docker · Docker Compose |
| CI/CD | GitHub Actions |

## 模块说明

```
lookforbest/
├── backend/              Spring Boot API 服务
├── frontend/             Vue3 管理后台 + 前台展示
├── lookforbest_app/      Flutter 多端 App（iOS/Android）
├── lookforbest_miniapp/  Uniapp 微信小程序
├── nginx/                Nginx 反向代理 + SSL 配置
├── scripts/              部署脚本
└── docs/                 文档
```

## 本地开发启动

### 依赖环境

- Java 17+
- Node.js 20+
- Flutter 3.27+
- Docker + Docker Compose

### 1. 启动基础服务（MySQL / Redis / MinIO）

```bash
docker compose up -d mysql redis minio
```

### 2. 启动后端

```bash
cd backend
./mvnw spring-boot:run
# 默认监听 http://localhost:8080
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
# 默认监听 http://localhost:5173
```

### 4. 运行 Flutter App

```bash
cd lookforbest_app
flutter pub get
flutter run
```

### 5. 运行微信小程序

```bash
cd lookforbest_miniapp
npm install
npm run dev:mp-weixin
# 使用微信开发者工具打开 dist/dev/mp-weixin/
```

## 部署方式

详见 [docs/deployment.md](docs/deployment.md)

## API 文档

启动后端后访问：`http://localhost:8080/swagger-ui.html`

### 主要端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/robots` | 机器人列表（支持多维筛选） |
| GET | `/api/v1/robots/{id}` | 机器人详情 |
| GET | `/api/v1/robots/slug/{slug}` | 按 Slug 获取机器人 |
| GET | `/api/v1/robots/{id}/similar` | 相似机器人 |
| POST | `/api/v1/compare` | 创建对比会话 |
| GET | `/api/v1/manufacturers` | 厂商列表 |
| GET | `/api/v1/categories` | 分类树 |
| POST | `/api/v1/auth/register` | 用户注册 |
| POST | `/api/v1/auth/login` | 用户登录 |
| GET | `/api/v1/users/me/favorites` | 收藏列表 |
| POST | `/api/v1/admin/stats` | 管理仪表盘统计（Admin） |
| DELETE | `/api/v1/admin/robots/batch` | 批量删除机器人（Admin） |
| PATCH | `/api/v1/admin/robots/batch/status` | 批量修改状态（Admin） |
| POST | `/api/v1/files/upload/admin` | 文件上传（Admin） |
