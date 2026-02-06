# Chat Book Cloud (大模型知识库云平台)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue)](https://spring.io/projects/spring-cloud)
[![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2023.0.1.0-orange)](https://github.com/alibaba/spring-cloud-alibaba)

## 📖 项目简介

**Chat Book Cloud** 是一个基于 Spring Cloud 微服务架构的大模型知识库与博客平台。本项目以**插件化**、**高复用**为核心设计理念，致力于打造一套"兼容万物"的基础架构体系。

项目将通用的中间件、工具类、安全认证等核心能力封装为独立的可插拔模块（Framework），配合统一的网关（Gateway）与鉴权中心，实现不同业务平台（如文章博客、知识库、AI对话等）的快速接入与能力复用。

## 🏗 系统架构

项目采用经典的微服务分层架构，核心分为**基础架构层**、**网关层**、**业务服务层**与**前端应用层**。

### 核心设计理念

1.  **统一鉴权 (Unified Authentication)**：
    - **鉴权中心**：由 `chat-book-cloud-auth` 负责颁发 Token。
    - **网关拦截**：`chat-book-cloud-gateway` 作为流量入口，通过 `AuthenticationFilter` 进行统一的 Token 校验与解析。
    - **上下文透传**：通过 HTTP Header (`X-User-Id`, `X-User-Name`) 将用户信息透传至下游服务，下游服务通过 `UserContextFilter` 自动填充上下文。
    - **设计优势**：业务服务无需重复实现繁琐的鉴权逻辑，仅需关注业务本身，实现"一次鉴权，处处通行"。

2.  **插件化架构 (Plugin-Oriented Framework)**：
    - **高度封装**：将 Redis、MinIO、Security、WebSocket、Excel 处理等通用能力封装为独立的 Starter 模块。
    - **即插即用**：业务服务只需引入对应的 Maven 依赖即可获得相关能力，配置统一管理，降低了微服务开发的复杂度。
    - **兼容万物**：设计之初即考虑到多平台接入，无论是博客、后台管理还是未来的 AI 服务，均可共用同一套核心包与网关。

### 核心特性详解

#### 1. 统一鉴权与上下文透传

**流程**：`Request` -> `Gateway` (校验 Token & 解析) -> `Header` (注入 X-User-Id) -> `MicroService` (拦截器读取 Header) -> `UserContext`

在业务服务中，你可以随时随地获取当前用户信息：

```java
// 在任意 Service 或 Controller 中
Long userId = UserContext.getUserId();
String username = UserContext.getUsername();
```

#### 2. 通用异常处理与响应封装

框架层(`common` 模块)统一了全局异常处理 (`GlobalExceptionHandler`) 和响应结构 (`Result<T>`)。

- **统一响应**：所有 API 均返回 `code`, `message`, `data` 格式。
- **异常拦截**：自动捕获 `BusinessException` 并转换为对应的错误码响应。

## 📂 目录结构

```
chat-book-cloud
├── chat-book-cloud-dependencies   # 统一依赖版本管理 (BOM)
├── chat-book-cloud-framework      # 核心框架层 (Infrastructure)
│   ├── chat-book-cloud-common     # 通用工具、上下文、全局异常、Result封装
│   ├── chat-book-cloud-security-mvc # 微服务安全组件 (Token解析、上下文填充)
│   ├── chat-book-cloud-redis      # Redis 增强配置 (CacheManager, RedisTemplate)
│   ├── chat-book-cloud-minio      # 对象存储服务封装 (MinioTemplate)
│   ├── chat-book-cloud-websocket  # WebSocket 通信基座
│   ├── chat-book-cloud-parsing    # 文档解析与 AI 上下文处理 (Tika, LangChain4j)
│   └── chat-book-cloud-excel      # Excel 导入导出工具 (EasyExcel)
├── chat-book-cloud-gateway        # API 网关 (路由、鉴权、限流)
├── chat-book-cloud-auth           # 认证中心 (登录、注册、Token颁发)
├── chat-book-cloud-user           # 用户服务 (用户信息、关注、消息)
├── chat-book-cloud-article        # 文章服务 (博客、分类、评论)
└── chat-book-ui-blog              # 前端工程 (Vue 3 + Vite)
```

## 📦 模块功能深度解析

### 1. 基础架构 (Infrastructure)

| 模块名称                         | 说明         | 关键功能点                                                                                         |
| :------------------------------- | :----------- | :------------------------------------------------------------------------------------------------- |
| **chat-book-cloud-common**       | **通用基座** | `UserContext` 线程隔离上下文, `Result` 统一响应, `GlobalExceptionHandler` 全局异常, `JwtUtil` 工具 |
| **chat-book-cloud-security-mvc** | **安全插件** | 提供 `UserContextFilter`，自动拦截请求并填充 `UserContext`，实现微服务内部无感鉴权                 |
| **chat-book-cloud-minio**        | **存储插件** | 封装 MinIO SDK，提供文件上传、下载、预览 URL 生成等一键式服务                                      |
| **chat-book-cloud-parsing**      | **AI 解析**  | 集成 Apache Tika 和 LangChain4j，支持 PDF/Word/Txt 等多格式文档解析与分块                          |
| **chat-book-cloud-websocket**    | **实时通信** | 基于 Netty/Tomcat 封装的 WebSocket 服务，支持点对点消息推送                                        |
| **chat-book-cloud-excel**        | **数据导出** | 集成 EasyExcel，提供注解式导入导出功能                                                             |

### 2. 网关与认证 (Gateway & Auth)

- **chat-book-cloud-gateway**:
  - **鉴权过滤器 (`AuthenticationFilter`)**: 解析 JWT Token，验证有效期，提取 UserID 并放入 Header。
  - **白名单机制**: 通过配置可放行特定路径（如登录、注册接口）。
  - **跨域配置**: 统一处理 CORS 问题。

- **chat-book-cloud-auth**:
  - 负责用户的登录认证与 Token 颁发。
  - 与 User 服务交互验证用户身份。

## 🛠 技术栈 (Dependencies)

本项目紧跟主流技术栈，确保系统的先进性与稳定性：

- **核心框架**: Spring Boot 3.2.4, Spring Cloud 2023.0.1
- **微服务治理**: Spring Cloud Alibaba (Nacos Discovery/Config)
- **持久层**: MyBatis Plus 3.5.7, MySQL 9.3.0
- **缓存与消息**: Redis, Kafka (Reserved), RabbitMQ
- **对象存储**: MinIO 8.5.3
- **API 文档**: Knife4j 4.4.0
- **AI 与大模型**: LangChain4j 0.31.0
- **工具库**: Apache Commons, FastJSON2, Hutool, EasyExcel

## 🚀 快速开始

### 环境准备

- **JDK**: 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+ (导入各服务下的 SQL 脚本)
- **Redis**: 启动默认端口 6379
- **Nacos**: 2.x (启动并导入配置)
- **MinIO**: (可选，涉及文件上传时需要)

### 配置中心 (Nacos) 设置

1.  访问 Nacos 控制台 (默认 `http://localhost:8848/nacos`).
2.  创建命名空间 (Namespace) 或使用默认 `public`.
3.  在各服务的 `bootstrap.yaml` 中配置 Nacos 地址和 DataId.
    - 通常 DataId 格式为: `${spring.application.name}-${spring.profiles.active}.yaml`
    - 例如: `chat-book-cloud-user-dev.yaml`

### 启动流程

1.  **基础设施**: 启动 MySQL, Redis, Nacos.
2.  **后端服务 (按顺序)**:
    1.  `GatewayServiceApplication` (网关)
    2.  `AuthenticationApplication` (认证)
    3.  `UserServiceApplication` (用户)
    4.  `ArticleApplication` (文章)
3.  **前端应用**:
    ```bash
    cd chat-book-ui-blog
    npm install
    npm run dev
    ```
4.  **访问验证**: 打开浏览器访问 `http://localhost:5173` (前端) 或 `http://localhost:15020/doc.html` (网关 Knife4j 文档).

## 🔮 未来规划

- [ ] **AI 能力增强**: 集成 RAG (检索增强生成) 流程，支持知识库问答。
- [ ] **全文检索**: 引入 ElasticSearch 替代 MySQL 模糊查询。
- [ ] **社区功能**: 增加点赞、评论、收藏等互动功能 (已部分实现)。
- [ ] **监控告警**: 完善 Prometheus + Grafana 监控体系。

---

**Designed by Amireux**
