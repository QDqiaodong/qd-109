# 数码配件经验交流社区

为数码爱好者搭建的交流圈子，用户分享数码配件使用体验、搭配方案、避坑心得，可发布使用问题寻求解答。

> **仅做经验交流，不涉及商品交易、比价、线上售后等业务**

---

## 🚀 快速开始

### 方式一：一键启动脚本（推荐）

```bash
./start.sh
```

### 方式二：Docker Compose 手动启动

```bash
# 构建并启动所有服务
docker compose up --build -d

# 查看运行状态
docker compose ps
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3009 |
| 后端 API | http://127.0.0.1:8089/api |
| MySQL | 127.0.0.1:3312 |
| Redis | 127.0.0.1:6381 |

### 测试账号

| 用户名 | 密码 |
|--------|------|
| test001 | 123456 |
| test002 | 123456 |
| test003 | 123456 |

---

## 📐 端口配置（统一管理）

所有端口配置在 `.env` 文件中，可根据需要修改：

| 变量名 | 端口 | 说明 | 避开的默认端口 |
|--------|------|------|----------------|
| `FRONTEND_PORT` | 3009 | 前端 Nginx | 80, 8080 |
| `BACKEND_PORT` | 8089 | 后端 SpringBoot | 8080, 9090 |
| `MYSQL_PORT` | 3312 | MySQL 数据库 | 3306 |
| `REDIS_PORT` | 6381 | Redis 缓存 | 6379 |

> **重要约束**：所有服务仅绑定 `127.0.0.1`，不对外网暴露。

---

## 🛠️ 技术栈

### 前端
- **框架**: Vue 3.4 + Vite 5
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **UI 组件**: Element Plus
- **HTTP 客户端**: Axios

### 后端
- **框架**: Spring Boot 3.3
- **JDK**: 17
- **ORM**: MyBatis Plus 3.5.5
- **缓存**: Redis
- **数据库**: MySQL 8.0
- **工具库**: Hutool 5.8

### 部署
- **容器化**: Docker + Docker Compose
- **前端服务器**: Nginx (Alpine)
- **构建优化**: 分层缓存 + 国内镜像源

---

## 📦 核心功能模块

### 1. 体验帖子发布
- 撰写数码配件使用感受、搭配方案
- 上传实拍图片（最多 9 张）
- 选择对应设备分类

### 2. 问题求助答疑
- 描述配件故障、适配问题
- 发布求助内容，等待其他用户解答

### 3. 帖子互动留言
- 在公开帖子下方留言讨论
- 支持多层级评论回复
- 交流使用技巧与解决方案

### 4. 内容分类筛选
- 8 大分类：手机配件、电脑配件、影音设备、智能穿戴、摄影器材、游戏外设、网络设备、存储设备
- 按类目快速筛选对应交流内容
- 区分「体验分享」和「问题求助」标签

---

## 🏗️ 项目结构

```
qd-109/
├── .env                         # 全局环境变量（端口、镜像仓库等）
├── docker-compose.yml           # Docker 编排配置
├── start.sh                     # 一键启动脚本
├── README.md                    # 项目说明文档
├── backend/                     # 后端 SpringBoot
│   ├── Dockerfile               # 后端镜像构建（分层缓存）
│   ├── pom.xml                  # Maven 依赖
│   ├── settings.xml             # 阿里云 Maven 镜像源
│   └── src/main/
│       ├── java/com/digital/community/
│       │   ├── CommunityApplication.java
│       │   ├── common/          # 通用响应类
│       │   ├── config/          # MyBatisPlus、Redis、CORS 配置
│       │   ├── controller/      # REST API 接口
│       │   ├── service/         # 业务逻辑层
│       │   ├── mapper/          # 数据访问层
│       │   ├── entity/          # 数据实体
│       │   ├── dto/             # 请求参数 DTO
│       │   └── vo/              # 响应视图 VO
│       └── resources/
│           ├── application.yml          # 主配置
│           ├── application-dev.yml      # 开发环境
│           ├── application-prod.yml     # 生产环境
│           ├── schema.sql               # 数据库初始化脚本
│           └── mapper/*.xml             # MyBatis XML 映射
└── frontend/                    # 前端 Vue3
    ├── Dockerfile               # 前端镜像构建（分层缓存）
    ├── nginx.conf               # Nginx 配置（含 Gzip 压缩）
    ├── package.json             # npm 依赖
    ├── vite.config.js           # Vite 构建配置
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/                 # Axios 封装 + 接口定义
        ├── components/          # 公共组件（Header、Footer、PostCard）
        ├── views/               # 页面组件
        │   ├── Home.vue         # 首页
        │   ├── PostDetail.vue   # 帖子详情
        │   ├── CreatePost.vue   # 发布帖子
        │   └── Category.vue     # 分类页
        ├── router/              # 路由配置
        ├── store/               # Pinia 状态管理
        └── assets/              # 全局样式
```

---

## 🔧 Docker 构建优化说明

### 1. 分层缓存机制
**首次构建**：全量下载依赖
**后续构建**：
- `pom.xml` / `package.json` 无变更 → 复用依赖缓存，不重新下载
- 仅修改源代码 → 仅重新编译，跳过依赖下载步骤

### 2. 国内镜像源
- **Maven**: 阿里云公共仓库 (`settings.xml`)
- **npm**: 淘宝镜像 (`registry.npmmirror.com`)
- **基础镜像**: DaoCloud 镜像仓库 (`docker.m.daocloud.io`)

### 3. 镜像仓库统一
所有基础镜像通过 `DOCKER_REGISTRY` 环境变量统一前缀，避免部分镜像直连 DockerHub：

```
DOCKER_REGISTRY=docker.m.daocloud.io
```

涉及的基础镜像：
- `maven:3.9.6-eclipse-temurin-17` → 后端构建
- `eclipse-temurin:17-jre` → 后端运行
- `node:18-alpine` → 前端构建
- `nginx:alpine` → 前端运行
- `mysql:8.0` → 数据库
- `redis:7-alpine` → 缓存

---

## 📝 开发指南

### 本地开发模式

#### 后端
```bash
cd backend
mvn spring-boot:run
```

#### 前端
```bash
cd frontend
npm install
npm run dev
```

> 前端开发服务器运行在 `http://127.0.0.1:3009`，API 请求自动代理到后端 `http://127.0.0.1:8089`

### 常用命令

```bash
# 查看所有服务日志
docker compose logs -f

# 查看单个服务日志
docker compose logs -f backend

# 停止所有服务
docker compose down

# 重启服务
docker compose restart

# 进入容器
docker compose exec backend bash
docker compose exec mysql mysql -uroot -proot123456
```

---

## 🔒 安全约束

1. **端口绑定**: 所有 Docker 端口仅绑定 `127.0.0.1`，禁止外网访问
2. **端口固定**: 不使用自动端口分配，避免端口冲突
3. **严格端口**: Vite `strictPort = true`，端口被占用时直接报错
4. **容器名称**: 所有容器名称使用 `digital-community-` 前缀，便于识别

---

## ✅ 自检清单

启动后请执行以下检查确保项目正常运行：

```bash
# 检查端口监听
lsof -nP -iTCP:3009 -sTCP:LISTEN
lsof -nP -iTCP:8089 -sTCP:LISTEN

# 验证前端访问
curl -sS http://127.0.0.1:3009 | head -20
curl -sS http://localhost:3009 | head -20

# 验证后端 API
curl -sS http://127.0.0.1:8089/api/categories
```

---

## 📄 License

MIT
