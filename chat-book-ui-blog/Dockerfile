# Production stage
FROM nginx:alpine

# 作者
LABEL maintainer="chat-book-cloud"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制 dist 目录到 nginx 默认目录
COPY dist/ /usr/share/nginx/html/

# 复制 Nginx 模板，容器启动时会用 envsubst 渲染
COPY nginx.conf /etc/nginx/templates/default.conf.template

# 暴露端口
EXPOSE 80

# 启动命令
CMD ["nginx", "-g", "daemon off;"]
