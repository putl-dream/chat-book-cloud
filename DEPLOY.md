# CI/CD 部署文档 (本地构建模式)

本项目使用 **GitHub Actions** + **Docker Compose** 实现自动化部署。
本方案采用**本地构建模式**：在 GitHub Actions 构建 Jar 包和前端资源，传输到服务器后直接构建 Docker 镜像并运行，无需依赖外部镜像仓库。

## 1. 准备工作

### 1.1 服务器准备
- 一台安装了 Docker 和 Docker Compose 的 Linux 服务器（推荐 Ubuntu/CentOS）。
- 确保服务器开放了相关端口（80, 8080, 8848 等，视安全策略而定）。
- 确保 SSH 服务开启，且可以使用密钥登录。

### 1.2 GitHub Secrets 配置

在 GitHub 仓库的 `Settings` -> `Secrets and variables` -> `Actions` 中添加以下 Secrets：

| Secret Name | 说明 | 示例 |
|---|---|---|
| `SERVER_HOST` | 服务器 IP 地址 | `1.2.3.4` |
| `SERVER_USER` | 服务器用户名 | `root` |
| `SERVER_SSH_KEY` | 服务器 SSH 私钥 | `-----BEGIN OPENSSH PRIVATE KEY-----...` |

> **注意**：不再需要 `REGISTRY_URL`, `REGISTRY_USERNAME` 等镜像仓库相关的 Secrets。

## 2. 部署流程

1. **代码推送**：
   - 当代码推送到 `main` 分支时，GitHub Actions 会自动触发。
2. **CI 构建**：
   - **Java 后端**：Maven 打包生成 Jar 包。
   - **Vue 前端**：Node.js 构建生成 `dist` 静态资源目录。
3. **文件传输**：
   - 将构建产物（Jar, dist）、Dockerfile 和 docker-compose.yml 打包传输到服务器。
4. **服务部署**：
   - 在服务器上执行 `docker-compose up -d --build`。
   - Docker 根据传入的 Jar 和 Dockerfile 现场构建镜像并启动容器。

## 3. 本地开发与测试

### 3.1 启动所有服务
在项目根目录运行（需确保本地已执行 Maven 打包和前端构建）：
```bash
# 1. 后端打包
mvn clean package -DskipTests

# 2. 前端构建 (进入 chat-book-ui-blog 目录)
npm install && npm run build

# 3. 启动
docker-compose up -d --build
```

### 3.2 访问服务
- **Nacos**: http://localhost:8848/nacos
- **Gateway**: http://localhost:8080
- **Frontend**: http://localhost:80

## 4. 常见问题

- **构建慢**：GitHub Actions 会自动缓存 Maven 依赖和 npm 依赖，后续构建速度会显著提升。
- **服务器磁盘空间**：定期运行 `docker system prune -a` 清理不再使用的镜像和缓存。
