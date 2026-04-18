# Chat Book Cloud

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue)](https://spring.io/projects/spring-cloud)
[![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2023.0.1.0-orange)](https://github.com/alibaba/spring-cloud-alibaba)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen)](https://vuejs.org/)

Chat Book Cloud 是一个面向技术内容社区、知识库管理与 AI 辅助创作场景的微服务平台。它把博客前台、创作中心、Agent 共创工作台和后台治理端放在同一套系统里，形成从内容消费到内容生产再到内容治理的完整闭环。

快速导航：项目亮点 / 技术架构 / 界面速览 / Agent 亮点 / 核心功能 / 快速开始 / API 文档 / 构建测试

## 项目亮点

- 一体化内容平台：同一套系统同时覆盖阅读、创作、互动、社交、私聊和后台治理
- Agent 深度嵌入写作流程：从选题澄清、草稿生成、版本优化到编辑器改写，形成连续共创体验
- 微服务边界清晰：Gateway、Auth、Article、User、Interaction、Social、Chat、Agent 各司其职
- 双前端协同：博客前台服务读者与作者，管理端服务运营与审核
- 工程化基础完整：Docker Compose、CI/CD、Nacos、Redis、RabbitMQ、MinIO、Knife4j 已基本成型

## 技术架构

### 总体架构

```text
chat-book-ui-blog (Vue 3)         chat-book-ui-admin (Vue 3 + TS)
          |                                   |
          +----------- Gateway 统一入口 -----------+
                              |
    +---------+---------+---------+---------+---------+---------+---------+
    |         |         |         |         |         |         |         |
  Auth      User     Article  Interaction  Social    Chat     Agent
    |         |         |         |         |         |         |
    +---------------- MySQL / Redis / RabbitMQ / MinIO / Nacos --+
```

- Gateway 负责统一入口、路由转发与认证透传
- Agent、Article、User、Interaction、Social、Chat 按领域拆分，避免单体堆叠
- 博客前台与管理端独立构建、独立部署，但共享同一套服务体系
- Redis、RabbitMQ、MinIO、Nacos 组成基础设施层，为缓存、消息、对象存储与配置治理提供支撑

### 后端技术栈

| 类别 | 技术 |
| --- | --- |
| 语言与运行时 | Java 17 |
| 微服务框架 | Spring Boot 3.2.4、Spring Cloud 2023.0.1、Spring Cloud Alibaba 2023.0.1.0 |
| 服务治理 | Nacos（配置中心 + 服务发现） |
| 网关 | Spring Cloud Gateway |
| 持久层 | MyBatis-Plus 3.5.7、MySQL 8 |
| 缓存 | Redis |
| 消息队列 | RabbitMQ |
| 对象存储 | MinIO |
| 文档解析 | Apache Tika |
| 文档接口 | Knife4j / OpenAPI 3 |
| 容错能力 | OpenFeign、LoadBalancer、Resilience4j |
| AI 集成 | Anthropic Java SDK |

### 前端技术栈

| 应用 | 技术 |
| --- | --- |
| 博客前台 | Vue 3、Vite 5、Element Plus、Pinia、Vue Router、Axios、Tiptap、Vitest |
| 管理端 | Vue 3、Vite 6、TypeScript、Pinia、Vue Router、Tailwind CSS 4 |

## 界面速览

README 只保留几张最能代表产品形态的界面，其余截图仍可在 `doc/images/` 中查看。

| Agent 共创工作台 | 创作中心编辑器 | 后台内容治理 |
| --- | --- | --- |
| ![Agent 共创工作台](doc/images/博客-AI生文.png) | ![创作中心编辑器](doc/images/博客-文章创作.png) | ![后台内容治理](doc/images/后台-内容管理.png) |

## Agent 亮点能力

### 1. 面向写作场景的 Agent 设计

这里的 Agent 不是普通问答框，而是围绕“内容澄清 -> 共创生成 -> 草稿优化 -> 编辑器接力”组织起来的写作型运行时。仓库里已经落地了独立的 `chat-book-cloud-agent` 服务、Prompt 资源目录、会话持久化、流式回复契约，以及与创作中心衔接的前端工作台。

- 独立 Agent 服务边界：会话管理、对话编排、草稿生成、草稿优化、摘要提取与编辑器辅助都收口在 `chat-book-cloud-agent`
- 流式运行时设计：`/agent/session/chat/stream` 已支持 SSE 流式返回，前端 `AgentStudio` 按会话维度消费增量内容
- 共享流式协议演进：仓库内已有 [Agent 运行时重构说明](doc/agent-run-refactor-v1.md)，对聊天流、草稿流和最终产物做了统一约束
- Prompt 与场景分层：聊天、生成、优化、摘要、编辑辅助、Notebook 摘要等能力使用独立模板，而不是混在单一 Prompt 中
- 可继续演进为场景化 Runtime：`doc/article-agent-runtime-plan.md` 已规划 `DISCOVER / OUTLINE / DRAFT / EDIT / INLINE_COMPLETE` 等场景能力

### 2. Agent 文章共创

当前系统中的 Agent 共创更像“协作推进”，而不是“一次性吐出全文”。

- 在 `Agent Studio` 中先创建会话，围绕选题、目标读者、观点重点做多轮澄清
- Agent 在对话过程中沉淀上下文，再触发草稿生成与版本化优化
- 生成后的草稿可以被采用、继续优化，再导入创作中心进行人工编辑
- 前端已经具备会话侧边栏、聊天控制器、状态徽标、历史会话分页与详情回填能力
- 后端已经具备会话创建、历史分页、详情获取、草稿生成、草稿优化、版本采用等完整 API 链路

这使 Agent 更接近“共创搭子”：

- 先帮助作者把问题问清楚
- 再帮助作者把结构和表达铺出来
- 最后把结果交给创作中心继续精修与发布

### 3. Agent 智能体修改文章

除了独立的 Agent Studio，系统还在编辑器内嵌入了 AI 编辑助手，用于对现有正文做更细粒度的智能修改。

- 基于当前标题、摘要、全文与选区构造上下文
- 支持“仅聊天 / 替换选区 / 插入光标 / 追加末尾”四种编辑方式
- 作者可以直接描述“润色、扩写、压缩、改口吻、补过渡、补论据”等意图
- AI 先给出候选修改，再由作者决定是否应用到正文
- 对选区变更、应用失败、撤销场景做了基本保护，避免误覆盖正文

这种设计的价值比较直接：

- 把 AI 能力前移到真实编辑动作，而不是停留在“生成后复制粘贴”
- 保留作者对正文的最终控制权，避免黑盒式直接覆盖
- 让 AI 更像一个可讨论、可采纳、可回退的协作编辑伙伴

## 核心功能

### 1. 内容平台与社区互动

- 首页、分类页、标签页、搜索页、文章详情页
- 最新、热门、今日热门、分类筛选、标签筛选、相关推荐
- 用户浏览历史、收藏列表、个人文章、草稿文章查询
- 点赞、收藏、评论、通知、未读消息统计
- 关注 / 取关、好友列表、好友详情列表
- 私聊消息列表、会话最后一条消息、已读状态更新

### 2. 创作中心

- 基于 Tiptap 的富文本编辑器与图文内容编辑能力
- 草稿保存、草稿箱、文章发布、旧接口兼容的新增 / 更新文章接口
- 作者标签搜索、热门标签、后台作者标签分页
- 系统标签分页、创建、更新、删除、文章标签人工修正
- 标签映射分页、单条更新、批量回刷文章系统标签
- 封面与文件上传、头像上传

### 3. AI Agent 写作工作台

- 会话创建、历史会话分页、会话详情查询
- 普通聊天回复与 SSE 流式聊天回复
- 从会话生成草稿、草稿优化与版本采用
- 摘要提取与编辑器内续写 / 改写辅助
- 独立 Agent 数据库与 Prompt 资源目录

### 4. 后台运营治理

- 管理员账号登录与 JWT/Refresh Token 会话续期
- 仪表盘统计：用户数、文章数、待审核数、评论治理指标
- 用户分页、角色变更、禁用 / 启用、操作日志分页
- 文章审核通过 / 驳回 / 批量审核
- 全站内容分页、上架 / 下架 / 删除 / 恢复 / 批量治理
- 评论分页、统计、隐藏、恢复、删除
- 主题系统与后台风格配置

## 仓库结构

```text
chat-book-cloud/
├── chat-book-cloud-dependencies         # Maven BOM
├── chat-book-cloud-framework            # 公共基础能力
│   ├── chat-book-cloud-common
│   ├── chat-book-cloud-excel
│   ├── chat-book-cloud-minio
│   ├── chat-book-cloud-mybatis
│   ├── chat-book-cloud-parsing
│   ├── chat-book-cloud-rabbitmq
│   ├── chat-book-cloud-redis
│   ├── chat-book-cloud-security-mvc
│   └── chat-book-cloud-websocket
├── chat-book-cloud-api                  # OpenFeign API 聚合
│   ├── chat-book-cloud-api-article
│   ├── chat-book-cloud-api-chat
│   ├── chat-book-cloud-api-interaction
│   ├── chat-book-cloud-api-social
│   └── chat-book-cloud-api-user
├── chat-book-cloud-gateway              # 网关服务，默认 8080
├── chat-book-cloud-auth                 # 认证服务，默认 8081
├── chat-book-cloud-article              # 文章服务，默认 8082
├── chat-book-cloud-user                 # 用户服务，默认 8083
├── chat-book-cloud-interaction          # 互动服务，默认 8084
├── chat-book-cloud-chat                 # 聊天服务，默认 8085
├── chat-book-cloud-social               # 社交服务，默认 8086
├── chat-book-cloud-agent                # AI Agent 服务，默认 8087
├── chat-book-ui-blog                    # 博客前台，Vite 开发端口 5173
├── chat-book-ui-admin                   # 管理端，Vite 开发端口 3001
├── doc/                                 # 设计说明与界面截图
├── docker-compose.yml
├── docker-compose.dev.yml
└── README.md
```

## 服务与端口

| 模块 | 默认端口 | 说明 |
| --- | --- | --- |
| Gateway | `8080` | 统一网关入口 |
| Auth | `8081` | 注册、登录、验证码、刷新 Token、登出、OAuth2 |
| Article | `8082` | 文章、标签、草稿、文件、后台内容治理 |
| User | `8083` | 用户资料、头像、后台用户治理 |
| Interaction | `8084` | 点赞、收藏、评论、通知、评论治理 |
| Chat | `8085` | 私聊消息与未读统计 |
| Social | `8086` | 关注、好友、关系查询 |
| Agent | `8087` | AI 会话、SSE、草稿生成、编辑器辅助 |
| Blog UI | `5173` | 前台开发服务 |
| Admin UI | `3001` | 管理端开发服务 |

容器模式下：

- 博客前台暴露 `80`
- 管理端默认暴露 `3000`
- MinIO 暴露 `9000`，控制台暴露 `9001`
- Nacos 暴露 `8848`
- RabbitMQ 管理台暴露 `15672`

## 安装与配置

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+（CI / Deploy 使用 Node.js 24）
- MySQL 8
- Redis 7
- Nacos 2.x
- RabbitMQ 3.x
- MinIO

### 关键环境变量

| 变量 | 用途 | 是否必需 |
| --- | --- | --- |
| `GOOGLE_CLIENT_ID` | Google OAuth2 登录 | Auth 使用时必需 |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 登录 | Auth 使用时必需 |
| `OAUTH2_SUCCESS_REDIRECT_URL` | OAuth2 成功后跳转地址 | 推荐配置 |
| `ANTHROPIC_API_KEY` | Agent 模型调用 | Agent 使用时必需 |
| `ANTHROPIC_BASE_URL` | Agent 模型网关地址 | 可选，默认已提供 |
| `MYSQL_HOST` / `MYSQL_PORT` | 各服务数据库连接 | 本地或 Docker 运行时需要 |
| `REDIS_HOST` / `REDIS_PORT` | Redis 连接 | 相关服务需要 |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | RabbitMQ 连接 | Auth / Article / User / Interaction 需要 |
| `MINIO_ENDPOINT` / `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | 文件与头像存储 | Article / User / Interaction 需要 |
| `MINIO_PUBLIC_URL` | 前端可访问的对象存储地址 | 推荐配置 |

根目录提供了最基础的环境变量模板：

```bash
cp .env.example .env
```

`.env.example` 当前包含 OAuth2 相关变量；如果使用 Docker Compose 运行完整系统，建议在 `.env` 中额外补齐 Agent 与对象存储相关变量。

### 数据库初始化

推荐先创建以下数据库：

```sql
CREATE DATABASE IF NOT EXISTS chat_book_auth;
CREATE DATABASE IF NOT EXISTS chat_book_user;
CREATE DATABASE IF NOT EXISTS chat_book_article;
CREATE DATABASE IF NOT EXISTS chat_book_interaction;
CREATE DATABASE IF NOT EXISTS chat_book_social;
CREATE DATABASE IF NOT EXISTS chat_book_chat;
CREATE DATABASE IF NOT EXISTS chat_book_agent;
```

## 快速开始

### 方式一：本地分模块启动

1. 启动基础设施：Nacos、MySQL、Redis、RabbitMQ、MinIO
2. 启动后端服务：

```bash
mvn clean install -DskipTests
```

按推荐顺序启动服务：

1. `chat-book-cloud-gateway`
2. `chat-book-cloud-auth`
3. `chat-book-cloud-agent`
4. `chat-book-cloud-user`
5. `chat-book-cloud-article`
6. `chat-book-cloud-interaction`
7. `chat-book-cloud-chat`
8. `chat-book-cloud-social`

单模块打包：

```bash
mvn clean install -pl chat-book-cloud-gateway -am -DskipTests
```

在服务目录运行单个 Spring Boot 应用：

```bash
mvn spring-boot:run
```

3. 启动前端：

博客前台：

```bash
cd chat-book-ui-blog
npm install
npm run dev
```

管理端：

```bash
cd chat-book-ui-admin
npm install
npm run dev
```

访问地址：

- 博客前台：[http://localhost:5173](http://localhost:5173)
- 管理端：[http://localhost:3001](http://localhost:3001)
- 网关：[http://localhost:8080](http://localhost:8080)

### 方式二：Docker Compose 一键运行

完整本地编排：

```bash
cp .env.example .env
docker compose --env-file .env -f docker-compose.yml up -d --build
```

开发环境编排：

```bash
docker compose --env-file .env -f docker-compose.dev.yml up -d --build
```

## 使用示例

### 1. 密码登录

```bash
curl -X POST "http://localhost:8080/api/auth/account/login/password" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

### 2. 获取最新文章列表

```bash
curl -X POST "http://localhost:8080/api/page/newPage" \
  -H "Content-Type: application/json" \
  -d "{\"pageNo\":1,\"pageSize\":10}"
```

### 3. 创建 Agent 会话

```bash
curl -X POST "http://localhost:8080/api/agent/session/create" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Spring Cloud 实战写作\",\"scene\":\"article\"}"
```

### 4. Agent 流式聊天

```bash
curl -N -X POST "http://localhost:8080/api/agent/session/chat/stream" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\":1,\"message\":\"帮我整理一篇关于 Spring Cloud Gateway 的文章大纲\"}"
```

### 5. 管理员审核文章

```bash
curl -X POST "http://localhost:8080/api/article/admin/review/approve" \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d "{\"articleId\":1001}"
```

## API 文档

### 在线文档入口

各后端服务均集成了 Knife4j / OpenAPI 3，通常可通过各服务自身的 `doc.html` 查看接口文档，例如：

- Gateway: [http://localhost:8080/doc.html](http://localhost:8080/doc.html)
- Auth: [http://localhost:8081/doc.html](http://localhost:8081/doc.html)
- Article: [http://localhost:8082/doc.html](http://localhost:8082/doc.html)
- User: [http://localhost:8083/doc.html](http://localhost:8083/doc.html)
- Interaction: [http://localhost:8084/doc.html](http://localhost:8084/doc.html)
- Chat: [http://localhost:8085/doc.html](http://localhost:8085/doc.html)
- Social: [http://localhost:8086/doc.html](http://localhost:8086/doc.html)
- Agent: [http://localhost:8087/doc.html](http://localhost:8087/doc.html)

### 主要接口分组

#### Auth

- `POST /auth/account/login`
- `POST /auth/account/login/password`
- `POST /auth/account/login/captcha`
- `POST /auth/account/register`
- `GET /auth/account/captcha`
- `POST /auth/account/refresh`
- `POST /auth/account/logout`

#### Article / Page / Tag / Draft

- `POST /article/saveDraft`
- `POST /article/publish`
- `POST /article/queryPage`
- `GET /article/query`
- `DELETE /article/delete`
- `POST /page/newPage`
- `POST /page/hotPage`
- `POST /page/todayHotPage`
- `POST /page/multiFilterPage`
- `POST /page/relatedPage`
- `GET /author-tag/search`
- `GET /author-tag/hot`
- `POST /system-tag/page`
- `GET /system-tag/list`
- `POST /tag-map/batch-apply`
- `POST /draft/internal/create`

#### Interaction / Social / Chat

- `POST /interaction/foot/praise`
- `POST /interaction/foot/collection`
- `POST /interaction/review/save`
- `GET /interaction/review/getByArticleId`
- `POST /social/follow/{followId}`
- `GET /social/friends`
- `GET /chat/messages`
- `GET /chat/unread/count`
- `PUT /chat/messages/read`

#### Admin

- `GET /user/admin/count`
- `GET /user/admin/user`
- `PUT /user/admin/{userId}/role`
- `GET /user/admin/operation-log/page`
- `POST /article/admin/review/approve`
- `POST /article/admin/review/reject`
- `GET /article/admin/page`
- `PUT /article/admin/{articleId}/publish`
- `GET /interaction/admin/review/page`
- `GET /interaction/admin/review/stats`

#### Agent

- `POST /agent/session/create`
- `POST /agent/session/chat`
- `POST /agent/session/chat/stream`
- `GET /agent/session/detail`
- `GET /agent/session/page`
- `POST /agent/draft/generate`
- `POST /agent/draft/optimize`
- `POST /agent/draft/summary`
- `POST /agent/draft/version/adopt`
- `POST /agent/editor/assist`

## 构建、测试与质量保障

### 后端

```bash
mvn test -Dtest=ArticlePageServiceApplicationTests
mvn package -DskipTests
```

### 前端

博客前台：

```bash
cd chat-book-ui-blog
npm run build
```

管理端：

```bash
cd chat-book-ui-admin
npm run build
```

### CI / CD

仓库当前包含两条 GitHub Actions 工作流：

- `ci.yml`
  - PR 到 `master` 时执行
  - 启动 MySQL / Redis / RabbitMQ / MinIO
  - 创建测试数据库
  - 执行 `mvn test`
  - 分别构建 `chat-book-ui-blog` 与 `chat-book-ui-admin`
- `deploy-dev.yml`
  - `master` 分支推送或手动触发
  - 自动分析改动范围，仅构建受影响服务
  - 使用 `docker-compose.dev.yml` 在自托管环境增量部署

## 开发说明

### 认证链路

- Gateway 负责校验 JWT 并透传用户信息 Header
- 业务服务通过 `UserContextFilter` 建立当前用户上下文
- 前台与管理端都实现了 Access Token + Refresh Token 自动续期

### Agent 服务说明

- Agent 服务使用独立数据库 `chat_book_agent`
- Prompt 模板位于 `chat-book-cloud-agent/src/main/resources/prompts/`
- 流式聊天接口使用 `text/event-stream`
- 默认 WebSocket 路径为 `/agent/ws`

### 管理端说明

- 管理端当前为 Vue 3 单页应用，不再是 Next.js
- 默认读取 `VITE_API_BASE_URL`，未配置时走相对路径 `/api`
- 本地开发端口固定为 `3001`

## 参考文档

- [Agent 运行时重构说明](doc/agent-run-refactor-v1.md)
- [Article Agent Runtime 规划](doc/article-agent-runtime-plan.md)
- [Java 并发评审记录](doc/java-concurrency-review.md)
