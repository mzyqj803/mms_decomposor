# 构建阶段 - 使用debian基础镜像避免musl问题
FROM node:18-slim as build-stage

# 配置APT源为阿里云镜像
RUN echo "deb http://mirrors.cloud.aliyuncs.com/debian/ bullseye main contrib non-free" > /etc/apt/sources.list && \
    echo "deb-src http://mirrors.cloud.aliyuncs.com/debian/ bullseye main contrib non-free" >> /etc/apt/sources.list && \
    echo "deb http://mirrors.cloud.aliyuncs.com/debian/ bullseye-updates main contrib non-free" >> /etc/apt/sources.list && \
    echo "deb-src http://mirrors.cloud.aliyuncs.com/debian/ bullseye-updates main contrib non-free" >> /etc/apt/sources.list && \
    echo "deb http://mirrors.cloud.aliyuncs.com/debian/ bullseye-backports main contrib non-free" >> /etc/apt/sources.list && \
    echo "deb-src http://mirrors.cloud.aliyuncs.com/debian/ bullseye-backports main contrib non-free" >> /etc/apt/sources.list && \
    echo "deb http://mirrors.cloud.aliyuncs.com/debian-security/ bullseye-security main contrib non-free" >> /etc/apt/sources.list && \
    echo "deb-src http://mirrors.cloud.aliyuncs.com/debian-security/ bullseye-security main contrib non-free" >> /etc/apt/sources.list

# 安装必要的构建工具
RUN apt-get update && apt-get install -y python3 make g++ && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV NODE_ENV=production
ENV NODE_OPTIONS="--openssl-legacy-provider"
ENV npm_config_target_platform=linux
ENV npm_config_target_arch=x64

# 只复制package.json，不复制package-lock.json以避免架构问题
COPY package.json ./

# 完全清理并重新安装依赖以避免平台不匹配
RUN rm -rf node_modules package-lock.json && \
    npm cache clean --force && \
    npm install

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产阶段
FROM nginx:alpine as production-stage

# 复制构建结果到nginx目录
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
