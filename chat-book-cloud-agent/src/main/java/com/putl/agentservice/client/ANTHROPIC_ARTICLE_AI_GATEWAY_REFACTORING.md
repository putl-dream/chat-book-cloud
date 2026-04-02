# 一次把 AnthropicArticleAiGateway 从“大而全”重构为“双层架构”的实践总结

## 1. 背景

在 `chat-book-cloud-agent` 模块里，`AnthropicArticleAiGateway` 一开始承担了文章 Agent 相关的几乎所有模型交互能力：

- 对话 `chat`
- 文章首稿生成 `generateDraft`
- 草稿优化 `optimizeDraft`
- 摘要提取 `extractSummary`
- notebook 摘要更新 `summarizeNotebook`

随着功能不断增加，这个类逐渐变成了一个典型的“大而全”类。  
表面上它只是一个 gateway，实际上已经把编排、模板、请求构建、模型调用、流式处理、响应解析、结果规范化全塞在一起了。

这类代码短期写起来快，但一旦进入持续迭代阶段，问题会迅速放大。

## 2. 问题是怎么被发现的

最开始只是直觉上觉得这个类“职责有点重”。真正往下读后，可以把问题拆得更具体：

### 2.1 一个类里混了多种职责

它同时承担了至少 5 类职责：

1. 业务场景编排  
   例如 chat、draft、summary、notebook 分别走哪套 prompt、哪套模型参数。

2. Prompt 组织  
   包括模板加载、变量替换、transcript 拼装、notebook JSON 拼装。

3. Anthropic SDK 请求构造  
   比如 model、maxTokens、temperature、system prompt、messages 的构建。

4. 模型执行  
   包括普通调用、流式调用、stream chunk 累积、token 统计、latency 统计。

5. 响应解析与规范化  
   包括 JSON 提取、结构化聊天解析、interactive form 归一化、fallback 兜底。

### 2.2 代码“能跑”，但不利于演进

这种实现的最大问题不是“今天不能用”，而是“明天不好改”：

- 新增一个任务时，很容易继续往这个类里堆方法
- 改流式逻辑时，容易影响所有场景
- 改响应解析时，chat 和 draft 容易相互污染
- 想补单测时，很难为其中某一小段逻辑建立独立测试边界
- 未来如果接入别的 provider，会发现业务编排和 Anthropic 细节严重耦合

### 2.3 单纯按场景拆接口，不一定解决根因

最开始一个很自然的思路是把它拆成：

- `ArticleChatGateway`
- `ArticleDraftGenerationGateway`
- `ArticleDraftOptimizationGateway`
- `ArticleNotebookGateway`

这个思路看起来正确，但继续想会发现一个问题：

这些场景虽然业务含义不同，但底层执行流程几乎是同构的：

1. 读取模板
2. 组装 prompt
3. 构建请求
4. 调模型
5. 解析响应
6. 返回结构化结果

如果只是按场景拆 4 个 gateway，最终很可能只是把一个大类复制成 4 个小类，重复逻辑并没有真正被收敛。

## 3. 设计思考：先分清“变化点”和“稳定骨架”

重构这类类，关键不是“拆几个类”，而是先识别：

- 哪些东西在不同场景之间是稳定的
- 哪些东西才是每个场景真正不同的地方

### 3.1 稳定骨架是什么

对于文章 AI 调用来说，稳定骨架其实很清晰：

1. 收到业务输入
2. 加载模板或组装 system prompt
3. 构造 Anthropic 请求
4. 执行普通/流式调用
5. 收集 token、model、latency
6. 解析模型返回
7. 返回 `AiInvocationResult<T>`

这条主链路几乎对所有任务都成立。

### 3.2 真正的变化点是什么

不同场景之间变化的，主要是这些策略信息：

- 使用哪个模板
- 用哪个 model / maxTokens / temperature
- 用户消息怎么拼
- 是否支持流式
- 返回结果如何解析

这说明真正应该抽象出来的，不是“再拆 4 个互相重复的 gateway”，而是：

- 一层稳定的执行引擎
- 一组描述差异的任务策略
- 一层继续保留业务语义的强类型门面

## 4. 最终方案：强类型接口 + 统一执行引擎

最后落地采用的是“双层并存”方案。

### 4.1 第一层：强类型门面

对外继续保留业务语义明确的强类型接口，也就是现有的 `ArticleAiGateway`：

- `chat`
- `generateDraft`
- `optimizeDraft`
- `extractSummary`
- `summarizeNotebook`

这样 service 层不用感知内部重构，调用语义也不会退化成“任务引擎直调”。

这一层的职责被收缩为：

- 选择具体任务策略
- 组装统一上下文
- 调用执行引擎
- 返回强类型结果

### 4.2 第二层：统一执行引擎

新增统一的执行引擎：

- `ArticleAiExecutionEngine`
- `AnthropicArticleAiExecutionEngine`

执行引擎只负责稳定主流程：

1. 根据任务拿到 `MessageCreateParams`
2. 判断是否走流式
3. 调用 Anthropic 执行器
4. 收集原始返回和元信息
5. 调用任务定义的解析器
6. 输出 `AiInvocationResult<T>`

这样一来，通用流程只维护一份。

### 4.3 第三层：任务策略

新增任务抽象：

- `ArticleAiTask<T>`
- `ArticleAiContext`

每个场景变成一个任务策略：

- `ArticleChatTask`
- `ArticleDraftGenerateTask`
- `ArticleDraftOptimizeTask`
- `ArticleSummaryExtractTask`
- `NotebookSummarizeTask`

每个任务只声明自己的差异：

- 请求怎么构造
- 用什么参数
- 是否支持 streaming
- 返回怎么解析

这样“场景差异”被收进策略，“执行共性”被收进引擎。

## 5. 这次重构具体拆了什么

为了避免只是“横向搬代码”，这次重构把大类里的能力按性质拆成了几块基础组件。

### 5.1 请求工厂

`AnthropicRequestFactory`

职责：

- 校验 API Key
- 创建基础 `MessageCreateParams.Builder`
- 统一处理 model、maxTokens、temperature、system prompt

它解决的是“请求构造规范不能散落在各处”的问题。

### 5.2 执行器

`AnthropicExecutor`

职责：

- 调用 Anthropic SDK
- 处理普通请求
- 处理流式请求
- 收集 chunk
- 汇总 token、latency、model

它解决的是“模型调用细节不应该混在业务任务里”的问题。

### 5.3 模板服务

`PromptTemplateService`

职责：

- 加载模板
- 变量替换
- notebook JSON 美化
- prompt 相关公共文本处理

它解决的是“prompt 资源和 prompt 组装逻辑混杂”的问题。

### 5.4 消息组装器

`AgentMessageAssembler`

职责：

- 把内部消息转换为 Anthropic messages
- 统一 role 映射
- 统一 transcript 拼装
- 统一 system message 前缀处理

它解决的是“消息转换逻辑不应该散落在 task 和 gateway 里”的问题。

### 5.5 响应解析器

`AiResponseParser`

职责：

- 提取 JSON payload
- 去除 code fence
- 解析结构化聊天响应
- 解析普通 JSON 响应
- 规范化 interactive form
- 处理文本 fallback

它解决的是“响应解析和业务编排混在一起导致测试困难”的问题。

## 6. 为什么这是一次“优化”，而不是单纯“拆分类”

这次重构的重点不是把文件数变多，而是把边界变清楚。

### 6.1 对外没有失去业务语义

如果直接让业务层改成：

```java
engine.execute(task, context)
```

虽然抽象统一了，但上层代码会变得越来越偏底层，不利于业务可读性。

保留强类型门面后，业务层仍然是：

```java
articleAiGateway.chat(...)
articleAiGateway.generateDraft(...)
```

这层语义是值得保留的。

### 6.2 对内不再重复造流程

如果按场景直接拆 4 个 gateway，实现里仍然会反复出现：

- load template
- build request
- invoke model
- parse result

执行引擎把这条骨架统一起来后，新增任务只需要补策略，不需要复制流程。

### 6.3 单测边界终于清晰了

以前要测 `AnthropicArticleAiGateway`，往往一测就是一整坨。  
现在可以分别测试：

- `AiResponseParser`
- `AgentMessageAssembler`
- 各个任务策略
- 执行引擎

这意味着后续迭代时能更稳地做局部回归。

## 7. 执行过程可以拆成哪些步骤

如果把这次重构当成一个可复用的方法论，大致可以拆成下面几个步骤。

### 步骤一：不要急着拆，先找职责团块

先不要一上来创建新类，而是先标记出原类里每一段代码属于什么职责：

- 哪些是 prompt
- 哪些是 request build
- 哪些是 SDK invoke
- 哪些是 parse
- 哪些是 normalize

只有先识别职责团块，后面的拆分才不会只是“机械切文件”。

### 步骤二：识别共性流程

从多个方法里抽出共性主流程，确认它是不是一条稳定骨架。

如果不同业务场景的骨架一致，就应该优先抽执行引擎，而不是先横向拆场景类。

### 步骤三：把变化点收敛成策略

把不同任务的差异整理成一张表：

| 任务 | 模板 | model | maxTokens | temperature | 解析方式 | 流式 |
| --- | --- | --- | --- | --- | --- | --- |
| chat | article_chat | chat | chat | 0.2 | structured chat | 否 |
| generateDraft | article_generate | generate | generate | 0.2 | JSON draft | 是 |
| optimizeDraft | article_optimize | optimize | optimize | 0.2 | JSON draft | 否 |
| extractSummary | article_summary | optimize | optimize | 0.1 | JSON summary | 否 |
| summarizeNotebook | notebook_summarize | notebook | notebook | 0.1 | JSON notebook | 否 |

当差异已经可以表格化，说明它们已经适合进入策略层。

### 步骤四：保留上层业务接口稳定

重构内部实现时，尽量不要同时改 service 调用面。  
先把变化控制在 `client` 包内部，让 service 继续依赖 `ArticleAiGateway`。

这样能把风险收在一层里，避免一次重构扩散到整个调用链。

### 步骤五：再补局部测试

大重构之后，最先应该补的是纯逻辑、纯解析、纯转换这类测试，因为它们回报最高、最稳定。

这次优先补的是 `AiResponseParser` 的单测，原因也很简单：

- 逻辑复杂
- 分支多
- 容易回归
- 不需要真实模型调用

## 8. 这次重构带来的直接收益

### 8.1 `AnthropicArticleAiGateway` 从“实现中心”变成“门面入口”

它现在只保留强类型业务接口，不再承载一整套底层流程。

### 8.2 新增任务的成本明显下降

以后新增“提纲生成”“标题生成”“风格改写”这类能力时，通常只需要：

1. 新建一个 task
2. 选择模板和参数
3. 定义解析方式
4. 在门面增加一个强类型方法

而不是把所有逻辑继续往一个 700 行大类里堆。

### 8.3 Anthropic 细节被压缩到更合适的层次

虽然当前仍然只有 Anthropic 一个 provider，但至少 Anthropic 的 SDK 调用细节已经被收敛到：

- request factory
- executor
- task

未来如果要走多 provider，这会比从一个巨型 gateway 开始改轻松得多。

## 9. 这次重构没有做什么

为了避免一次改太猛，这次刻意没有做几件事：

- 没有把 `ArticleAiGateway` 直接拆成多个对外接口
- 没有引入 provider router
- 没有把 Anthropic 配置改造成完整多模型平台
- 没有改 controller、service、DTO、WebSocket/SSE 协议

这不是偷懒，而是控制改动半径。

好的重构不是“一步到位”，而是“把最大的结构问题先解决，同时不制造新的系统性风险”。

## 10. 后续还可以怎么继续演进

如果后续继续做演进，建议顺序如下：

### 10.1 补 task 层和执行引擎层测试

包括：

- 请求构造是否正确
- streaming 是否只在支持任务上开启
- token/model/latency 是否完整传递

### 10.2 继续抽 provider 适配层

当系统真的需要接入 OpenAI、DeepSeek、Gemini 时，再往上加一层 provider 适配，不要现在提前过度设计。

### 10.3 视情况再拆对外门面

如果未来 `ArticleAiGateway` 本身也开始膨胀，再考虑拆成更窄的业务门面接口。  
但那应该是下一阶段的事，不应该和这次“执行骨架重构”绑在一起做。

## 11. 小结

这次重构最重要的收获，不是“把一个大类拆成了很多小类”，而是把原来混在一起的几层东西分开了：

- 业务语义归业务门面
- 执行流程归执行引擎
- 场景差异归任务策略
- SDK 细节归执行器和请求工厂
- 文本解析归响应解析器

很多“职责过重”的类，真正的问题都不是代码行数，而是边界错位。  
一旦边界理顺，后续新增功能、补测试、替换模型、排查问题，成本都会下降。

这也是这次 `AnthropicArticleAiGateway` 重构最核心的价值。
