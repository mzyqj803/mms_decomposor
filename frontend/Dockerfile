# 直接使用nginx镜像，复制已构建的dist目录
FROM nginx:alpine

# 复制构建结果到nginx目录
COPY dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
