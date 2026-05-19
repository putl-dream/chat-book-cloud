# MCP 内容发布与 Agent 转换方向

## 背景

Chat Book Cloud 已经具备文章、草稿版本、Agent 会话和文章生成/优化的基础能力。下一步可以提供 MCP Server，让 Claude Code、Codex、Cursor 等 AI 编程工具把开发上下文、代码变更、问题排查过程或技术方案直接沉淀为平台文章草稿。

这个方向的核心价值不是“远程发文章”，而是把开发过程中的上下文转换为可复用的知识资产。

## 产品定位

MCP 能力应定位为“内容发布与知识沉淀入口”：

- 从 AI 编程工具接收上下文、diff、命令输出、需求说明等材料。
- 调用 `chat-book-cloud-agent` 将上下文转换为结构化文章草稿。
- 调用 `chat-book-cloud-article` 保存草稿和版本。
- 默认生成草稿，由用户在前端确认、编辑、审核后发布。

第一阶段不建议默认直接发布，避免敏感信息、错误结论或未审查内容进入正式文章。

## 建议架构

```text
Claude Code / Codex / Cursor
        ↓ MCP
chat-book-mcp-server
        ↓ HTTP
gateway / agent / article
        ↓
article_draft / article_draft_version / article
```

MCP Server 不直接访问数据库，统一通过现有 HTTP API 进入系统，以复用认证、权限、审计、草稿和版本能力。

## MVP 工具清单

- `convert_context_to_article`：接收上下文并调用 Agent 生成文章草稿。
- `create_article_draft`：直接创建文章草稿，适合外部工具已生成正文的场景。
- `optimize_article_draft`：对已有草稿进行润色、扩写、总结或结构调整。
- `list_my_drafts`：查询当前用户草稿列表。
- `get_draft_detail`：读取草稿详情，便于外部工具继续编辑。
- `publish_article`：发布文章，要求显式 `confirm: true`，并校验用户权限。

## 权限与安全要求

- 使用独立 MCP token，不复用普通前端会话。
- 权限粒度建议拆分为 `article:draft:create`、`article:draft:update`、`article:publish`。
- 默认只创建草稿；发布必须显式确认。
- 在入库前做敏感信息扫描，包括 token、密钥、数据库连接、内网地址等。
- 记录 MCP 操作审计日志，包括用户、工具名、草稿 ID、来源客户端和时间。
- 限制上下文大小；超长内容进入异步任务或分块总结流程。

## 实施阶段

1. 新增独立 `chat-book-mcp-server` 或轻量 Node/Java MCP 服务。
2. 打通 `convert_context_to_article` 到 Agent 草稿生成链路。
3. 打通 `create_article_draft` 和 `list_my_drafts`。
4. 增加敏感信息扫描与审计日志。
5. 在前端草稿页增加 “MCP 导入” 来源标记。
6. 最后再开放受控的 `publish_article`。

## 成功标准

- Claude Code 或 Codex 可以一键把当前任务上下文生成平台草稿。
- 草稿保留原始上下文摘要、生成正文、标签建议和来源信息。
- 用户无需复制粘贴，即可在平台内继续编辑并发布。
- 敏感信息不会默认进入正式文章。
