# ===== 构建阶段：Node 16 + Vue CLI 4 =====
# 使用国内镜像加速源拉取基础镜像（网络可直连 Docker Hub 时可去掉 dockerproxy.net/library/ 前缀）
FROM dockerproxy.net/library/node:16-alpine AS builder

WORKDIR /app

# 使用国内 npmmirror 加速依赖安装
RUN npm config set registry https://registry.npmmirror.com

# 先复制 package.json 安装依赖，充分利用镜像构建缓存
COPY package.json ./
RUN npm install

# 复制源码并构建生产版本
COPY . .
RUN npm run build

# ===== 运行阶段：nginx 托管静态资源并反向代理后端 =====
FROM dockerproxy.net/library/nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
