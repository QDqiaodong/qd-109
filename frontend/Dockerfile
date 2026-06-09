# ==========================================
# 前端 Dockerfile - Vue3 + Vite + Nginx
# 分层缓存策略：依赖层独立，源代码修改不触发 node_modules 重安装
# ==========================================

ARG DOCKER_REGISTRY

# ---------- 构建阶段 ----------
FROM ${DOCKER_REGISTRY}/library/node:18-alpine AS builder

WORKDIR /app

# 1. 先设置 npm 国内镜像源（配置层）
RUN npm config set registry https://registry.npmmirror.com

# 2. 复制 package.json 单独安装依赖（依赖层，package.json 不变更则缓存复用）
COPY package.json ./
COPY package-lock.json* ./
RUN npm install

# 3. 最后复制源代码（业务层，频繁变更，仅触发构建）
COPY . .
RUN npm run build

# ---------- 运行阶段 ----------
FROM ${DOCKER_REGISTRY}/library/nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
