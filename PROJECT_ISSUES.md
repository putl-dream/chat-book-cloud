# 项目问题梳理报告 (Project Issues Report)

根据对 `chat-book-cloud-user`、`chat-book-cloud-article` 和 `chat-book-ui-blog` 模块的代码分析，发现以下关键设计问题和改进建议。

## 1. 高优先级问题 (High Priority) - 严重逻辑缺陷

### 1.1 DO/VO 定义严重不一致

- **问题描述**:
  - `ArticleVO` (View Object) 用于前端交互，但缺失了 `ArticleDO` (Database Object) 中的关键字段，如 `cover` (封面), `category` (分类), `abstractText` (摘要), `status` (状态)。
  - 在 `ArticleServiceImpl` 中使用 `BeanUtil` 进行属性对拷时，这些缺失字段会导致数据丢失（写入 null），导致文章发布后缺失关键信息。
- **涉及文件**:
  - `chat-book-cloud-article/.../ArticleVO.java`
  - `chat-book-cloud-article/.../ArticleDO.java`

### 1.2 跨服务数据对象类型不匹配

- **问题描述**: `UserFootVO` 在两个服务中定义重复且类型不一致，会导致 Feign 调用序列化失败或精度丢失。
  - **User Service**: `praiseStat`, `collectStat` 为 `Integer` 类型。
  - **Article Service**: `praiseStat`, `collectStat` 为 `Long` 类型。
- **涉及文件**:
  - `chat-book-cloud-user/.../UserFootVO.java`
  - `chat-book-cloud-article/.../client/result/UserFootVO.java`

### 1.3 硬编码环境地址

- **问题描述**: 代码中大量硬编码了 `http://localhost:8080`，导致无法部署到测试/生产环境，也无法在容器化环境中使用。
- **涉及文件**:
  - **前端**: `chat-book-ui-blog/src/config/index.js`, `Text.vue`, `Chat.vue`
  - **后端**: `FileController.java` (返回图片URL), `ImageUtil.java`

---

## 2. 中优先级问题 (Medium Priority) - 架构与扩展性

### 2.1 文件存储依赖本地磁盘

- **问题描述**: `FileController` 将文件保存到应用运行目录的 `upload/` 文件夹。
  - **后果**: 容器重启数据丢失；无法横向扩展（多实例无法共享文件）；无法使用 CDN 加速。
- **建议**: 接入 MinIO 或阿里云 OSS 等对象存储服务。

### 2.2 搜索功能性能瓶颈

- **问题描述**: 文章搜索直接使用数据库 `LIKE %keyword%` 模糊查询。
  - **后果**: 数据量增大后无法命中索引，全表扫描导致数据库崩溃；不支持分词和相关度排序。
- **建议**: 引入 Elasticsearch 进行全文检索。

### 2.3 缺乏统一的枚举与常量管理

- **问题描述**: 代码中存在大量魔法值（如状态 `1`, `0`），缺乏统一枚举管理。
- **建议**: 提取 `ArticleStatusEnum`, `UserRoleEnum` 等。

---

## 3. 低优先级问题 (Low Priority) - 代码质量与规范

### 3.1 全局异常处理过于简陋

- **问题描述**: `GlobalExceptionHandler` 仅捕获 `Exception` 并返回 500，丢失了具体的业务错误码和堆栈信息，不利于前端展示友好提示和后端排查。
- **建议**: 细化异常捕获（如 `MethodArgumentNotValidException`），定义统一的业务异常类和错误码枚举。

### 3.2 代码重复

- **问题描述**: `UserFootVO` 等对象在多个模块中手动复制粘贴。
- **建议**: 提取公共模块 `chat-book-cloud-api` 或 `chat-book-cloud-common`，存放共享的 DTO/VO/Enum。

### 3.3 "伪"推荐算法

- **问题描述**: 热门推荐仅简单查询最近 N 天的数据，非真实热度计算。
- **建议**: 后期接入基于用户行为（点击、阅读时长）的推荐算法。

---

## 4. 前端展示但后端未实现/实现错误 (Frontend-Backend Gap)

### 4.1 AI 助手 (AI Assistant) - 完全缺失

- **前端现状**: `SidebarAI.vue` 使用 `setTimeout` 模拟 AI 回复，提示语硬编码为 `(AI功能开发中)`。
- **后端现状**: 完全没有对应的 Controller/Service。
- **建议**: 接入 OpenAI/Gemini/DeepSeek 等大模型 API，并实现后端流式接口 (SSE)。

### 4.2 消息通知 (Message Center) - 逻辑错误

- **前端需求**: 需要展示“谁”在“什么时候”对“哪篇文章”做了“什么操作”（点赞/收藏/评论）。
- **后端现状**: `UserFootServiceImpl.getMessage` 仅返回了被操作的**文章列表** (`List<ArticleListVO>`)，丢失了操作人、操作类型和操作时间。
  - **代码逻辑**: 查出所有相关 `UserFootDO` -> 提取 `articleId` -> 调用文章服务查询文章。
  - **后果**: 用户只能看到一堆文章，不知道为什么出现在消息里。
- **建议**: 重构 `MessageVO`，包含 `operatorUser`, `actionType`, `targetArticle`, `createTime` 等字段。

### 4.3 好友与足迹操作 - 违反 RESTful 规范

- **问题描述**: 写操作（增删改）使用了 `GET` 请求。
  - `UserFriendsController.addFriend` (GET) -> 实际是添加关注。
  - `UserFootController.addBrowse` (GET) -> 实际是写入浏览记录。
- **建议**: 改为 `POST` 请求，符合 HTTP 语义。

## 5. 维护性与规范性问题 (Maintenance & Conventions)

### 5.1 异常处理缺失与不一致 (Critical)

- **用户服务**: 没有任何 `GlobalExceptionHandler`，Service 层直接抛出 `RuntimeException`，前端收到的是 500 错误和一堆堆栈信息，体验极差。
- **文章服务**: 使用了 `GlobalExceptionHandler`，但返回结构是 `ResponseEntity`，与其他模块的 `CommonResult` 不一致，导致前端需要编写多套错误处理逻辑。
- **建议**:
  1. 引入统一的 `GlobalExceptionHandler` 到公共模块。
  2. 统一使用 `BusinessException` + `ErrorCodeEnum` 抛出业务异常。

### 5.2 事务管理缺失 (Critical)

- **现状**: 全局搜索 `@Transactional` 结果为 0。所有 Service 层的写操作均未开启事务。
- **风险**: `UserServiceImpl.updateUser` 涉及多表操作（User 和 UserInfo），如果中途失败，数据将不一致。
- **建议**: 在所有涉及增删改的 Service 方法上添加 `@Transactional`。

### 5.3 日志与调试代码遗留

- **现状**: `UserServiceImpl` 中保留了 `System.out.println` 和 `System.err.println`。
- **建议**: 替换为 `@Slf4j` 的 `log.info/error`，并移除调试打印。

### 5.4 接口响应结构混乱

- **现状**: 大部分接口返回 `CommonResult`，但 `FileController` 返回自定义的 `ImageResult`。
- **建议**: 废弃 `ImageResult`，统一使用 `CommonResult<ImageVO>`。

---

## 6. 改进路线图 (Refactor Roadmap)

1.  **Phase 1 (Fix Bugs)**: 修正 DO/VO 不一致，统一跨服务对象类型，提取配置消除硬编码。
2.  **Phase 2 (Standardization)**: **(新增)** 统一异常处理、响应结构、日志规范；添加事务注解。
3.  **Phase 3 (Gap Filling)**: 重构消息中心逻辑，实现真正的通知列表；补全 AI 助手后端。
4.  **Phase 4 (Architecture)**: 改造文件上传为 OSS，独立公共依赖模块。
5.  **Phase 5 (Performance)**: 引入 ES 搜索，优化缓存策略。
