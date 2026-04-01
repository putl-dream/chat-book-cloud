# Chat Book Cloud

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue)](https://spring.io/projects/spring-cloud)
[![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2023.0.1.0-orange)](https://github.com/alibaba/spring-cloud-alibaba)

`Chat Book Cloud` 是一个基于 Spring Cloud 的内容社区与 AI 能力平台，包含网关、认证、文章、用户、互动、社交、聊天、Agent 服务，以及博客前台和后台管理端两个前端应用。

当前仓库采用 Maven 多模块管理，后端服务通过 Nacos 做服务发现与配置管理，统一由 Gateway 对外暴露 API；前端通过 Vite 构建，开发和部署方式都已在仓库中落地。

## 项目概览

### 技术栈

- 后端：Java 17、Spring Boot 3.2.4、Spring Cloud 2023.0.1、Spring Cloud Alibaba
- 持久层：MyBatis-Plus、MySQL 8
- 基础设施：Redis、RabbitMQ、MinIO、Nacos
- AI 能力：Anthropic 兼容接口、LangChain4j、文档解析组件
- 前端：Vue 3、Vite
- 部署：Docker Compose、GitHub Actions

### 核心能力

- 统一网关入口，按 `/api/{service}` 方式路由到各业务服务
- JWT 鉴权与 OAuth2 Google 登录
- 文章发布、分页、标签、静态文件访问
- 用户信息、个人中心、消息与资料能力
- 点赞、收藏、评论、通知等互动能力
- 关注、粉丝、社交关系能力
- 聊天服务与 WebSocket 通道
- Agent 服务与大模型配置接入

## 仓库结构

```text
chat-book-cloud
├─ chat-book-cloud-dependencies        # Maven BOM，统一第三方依赖版本
├─ chat-book-cloud-framework           # 内部基础组件
│  ├─ chat-book-cloud-common
│  ├─ chat-book-cloud-mybatis
│  ├─ chat-book-cloud-redis
│  ├─ chat-book-cloud-websocket
│  ├─ chat-book-cloud-security-mvc
│  ├─ chat-book-cloud-minio
│  ├─ chat-book-cloud-parsing
│  ├─ chat-book-cloud-excel
│  └─ chat-book-cloud-rabbitmq
├─ chat-book-cloud-gateway             # 网关服务
├─ chat-book-cloud-auth                # 认证服务
├─ chat-book-cloud-agent               # Agent 服务
├─ chat-book-cloud-article             # 文章服务
├─ chat-book-cloud-user                # 用户服务
├─ chat-book-cloud-interaction         # 互动服务
├─ chat-book-cloud-social              # 社交服务
├─ chat-book-cloud-chat                # 聊天服务
├─ chat-book-cloud-api                 # OpenFeign API 模块聚合
│  ├─ chat-book-cloud-api-user
│  ├─ chat-book-cloud-api-article
│  ├─ chat-book-cloud-api-interaction
│  ├─ chat-book-cloud-api-social
│  └─ chat-book-cloud-api-chat
├─ chat-book-ui-blog                   # 博客前台
├─ chat-book-ui-admin                  # 后台管理端
├─ docker-compose.yml                  # 本地完整环境
└─ docker-compose.dev.yml              # 开发/服务器环境
```

## 服务与端口

| 模块 | 服务名 | 默认端口 | 说明 |
| --- | --- | --- | --- |
| Gateway | `chat-book-cloud-gateway` | `8080` | 统一入口与路由转发 |
| Auth | `chat-book-cloud-auth` | `8081` | 登录、鉴权、OAuth2 |
| Article | `chat-book-cloud-article` | `8082` | 文章与静态资源 |
| User | `chat-book-cloud-user` | `8083` | 用户资料与用户侧能力 |
| Interaction | `chat-book-cloud-interaction` | `8084` | 点赞、收藏、评论、通知 |
| Chat | `chat-book-cloud-chat` | `8085` | 聊天服务 |
| Social | `chat-book-cloud-social` | `8086` | 关注、粉丝、社交关系 |
| Agent | `chat-book-cloud-agent` | `8087` | Agent 与模型调用 |
| Blog UI | `chat-book-ui-blog` | `5173`（开发）/ `80`（容器） | 博客前台 |
| Admin UI | `chat-book-ui-admin` | `3001`（开发）/ `3000`（容器映射默认） | 管理后台 |

## 网关路由

Gateway 当前已配置以下前缀路由：

- `/api/auth/**` -> `chat-book-cloud-auth`
- `/api/article/**`、`/api/page/**`、`/api/tag/**` -> `chat-book-cloud-article`
- `/api/file/**`、`/api/images/**`、`/api/videos/**` -> `chat-book-cloud-article`
- `/api/user/**` -> `chat-book-cloud-user`
- `/api/chat/**` -> `chat-book-cloud-chat`
- `/api/agent/**` -> `chat-book-cloud-agent`
- `/api/interaction/**` -> `chat-book-cloud-interaction`
- `/api/social/**` -> `chat-book-cloud-social`

已配置的 WebSocket 路径：

- `/api/article/ws`
- `/api/chat/ws`

## 运行依赖

本地开发至少需要准备以下组件：

- JDK 17+
- Maven 3.8+
- Node.js 18+，仓库 CI/CD 使用 Node.js 24
- MySQL 8
- Redis 7
- Nacos 2.x

按功能启用时还需要：

- RabbitMQ
- MinIO
- Google OAuth2 配置
- Agent 模型接口 Key

## 数据库

从当前仓库配置文件可以确认的数据库名如下：

| 服务 | 数据库 |
| --- | --- |
| `chat-book-cloud-auth` | `chat_book_user` |
| `chat-book-cloud-user` | `chat_book_user` |
| `chat-book-cloud-article` | `chat_book_article` |
| `chat-book-cloud-interaction` | `chat_book_interaction` |
| `chat-book-cloud-social` | `chat_book_social` |
| `chat-book-cloud-chat` | `chat_book_chat` |
| `chat-book-cloud-agent` | `chat_book_agent` |

注意：

- 当前仓库中没有统一维护的 `Flyway` / `Liquibase` 初始化脚本目录。
- GitHub Actions 的 CI 会显式创建上述数据库，并额外为测试创建部分表。
- 如果你在本地首次启动，请先手动创建数据库，并根据业务需要准备表结构或导入初始化数据。

示例：

```sql
CREATE DATABASE IF NOT EXISTS chat_book_user;
CREATE DATABASE IF NOT EXISTS chat_book_article;
CREATE DATABASE IF NOT EXISTS chat_book_interaction;
CREATE DATABASE IF NOT EXISTS chat_book_social;
CREATE DATABASE IF NOT EXISTS chat_book_chat;
CREATE DATABASE IF NOT EXISTS chat_book_agent;
```

## 本地开发启动

### 1. 启动基础设施

至少先启动：

- MySQL
- Redis
- Nacos

如果要完整验证文章上传、通知或 OAuth 登录，再补充启动：

- RabbitMQ
- MinIO

### 2. 配置 Nacos

各服务都通过 `bootstrap.yaml` 指定了服务名，并通过 `application.yml` 中的 `spring.config.import` 从 Nacos 拉取环境配置。

常见 Data ID：

- `agent-service-local.yml`
- `article-service-local.yml`
- 其他服务以相同规则类推

如果你本地不打算使用 Nacos，需要自行补齐对应环境下的本地配置。

### 3. 启动后端

推荐顺序：

1. `chat-book-cloud-gateway`
2. `chat-book-cloud-auth`
3. `chat-book-cloud-user`
4. `chat-book-cloud-article`
5. `chat-book-cloud-interaction`
6. `chat-book-cloud-social`
7. `chat-book-cloud-chat`
8. `chat-book-cloud-agent`

也可以直接在根目录执行 Maven 构建：

```bash
mvn clean package
```

### 4. 启动前端

博客前台：

```bash
cd chat-book-ui-blog
npm install
npm run dev
```

后台管理端：

```bash
cd chat-book-ui-admin
npm install
npm run dev
```

前端脚本说明：

- `chat-book-ui-blog`：`dev`、`build`、`preview`
- `chat-book-ui-admin`：`dev`、`build`、`preview`、`typecheck`、`format`、`lint:css`

## Docker Compose

### 本地完整环境

仓库根目录的 `docker-compose.yml` 会启动：

- Nacos
- MySQL
- Redis
- RabbitMQ
- MinIO
- Gateway
- Auth
- Agent
- User
- Article
- Interaction
- Social
- Chat
- Blog UI
- Admin UI

启动示例：

```bash
cp .env.example .env
docker compose --env-file .env -f docker-compose.yml up -d --build
```

### 开发环境部署

`docker-compose.dev.yml` 更偏向服务器或联调环境，依赖外部已有的 MySQL、Redis、MinIO、RabbitMQ、Nacos。

启动示例：

```bash
cp .env.example .env
docker compose --env-file .env -f docker-compose.dev.yml up -d --build
```

## 关键环境变量

### Auth 服务

`.env.example` 当前包含以下变量：

```env
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
OAUTH2_SUCCESS_REDIRECT_URL=http://localhost:5173/login
```

### Agent 服务

常用变量：

- `ANTHROPIC_API_KEY`
- `ANTHROPIC_BASE_URL`
- `ANTHROPIC_CHAT_MODEL`
- `ANTHROPIC_GENERATE_MODEL`
- `ANTHROPIC_OPTIMIZE_MODEL`
- `ANTHROPIC_NOTEBOOK_MODEL`

### 文件存储

常用变量：

- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET`
- `MINIO_PUBLIC_URL`
- `FILE_STORAGE_BASE_URL`

## CI/CD

仓库当前包含两套 GitHub Actions：

- `CI`
  - PR 到 `master` 时触发
  - 启动 MySQL、Redis、RabbitMQ、MinIO
  - 创建测试数据库
  - 执行 `mvn test`
  - 构建 `chat-book-ui-blog` 和 `chat-book-ui-admin`
- `Deploy Dev`
  - `master` 分支 push 或手动触发时执行
  - 自动识别变更范围
  - 按需构建后端模块与前端应用
  - 通过 `docker compose -f docker-compose.dev.yml up -d --build` 完成部署

## 当前文档说明

这版 README 基于当前仓库中的以下实际内容重新整理：

- Maven 模块清单
- `bootstrap.yaml` / `application.yml`
- `docker-compose.yml` / `docker-compose.dev.yml`
- 前端 `package.json`
- GitHub Actions 工作流

如果后续你补充了数据库初始化脚本、接口文档地址或各服务能力细节，建议继续在本文件基础上增量维护。
