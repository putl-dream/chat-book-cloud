---
alwaysApply: false
globs: 
---
## Vue 业务开发“三层分离”架构协议 (V-TSC Protocol)

### 1. 架构映射表 (Architecture Mapping)

定义代码的物理归属，编译器需以此为基准进行文件结构审计。

| 层级 | 物理表现 | 核心职责 (Responsibility) | 稳定性 |
| --- | --- | --- | --- |
| **视图层 (View)** | `.vue` | **UI 声明**：HTML 结构、CSS 局部样式、基础响应式状态 (Ref/Reactive)。 | 长期存在 |
| **逻辑层 (Service)** | `use*.js/ts` | **动作封装**：API 调用、复杂逻辑流、生命周期钩子、派生状态。 | 过程性 |
| **配置层 (Config)** | `config.js` | **静态元数据**：校验规则 (Rules)、枚举 (Enums)、常量、I18n 配置。 | 静态/只读 |

---

### 2. 目录工程化规范 (Directory Engineering)

采用“就近原则”与“功能内聚”，编译器应监控非规范目录的创建。

```text
FeatureModule/           # 业务模块大驼峰 (如: OrderCenter)
├── _hooks/              # 逻辑层：必须以 use 开头，处理业务动词
│   ├── useSubmit.ts     # 提交逻辑
│   └── useTableQuery.ts # 查询逻辑
├── _utils/              # 配置层/工具类
│   ├── config.ts        # 表单 rules、静态 options
│   └── constants.ts     # 模块内硬编码常量
├── components/          # 视图层：私有原子组件
│   └── OrderItem.vue
└── Index.vue            # 入口页面：仅负责胶水逻辑与 UI 排版
```

### 3. 决策算法：名词 vs 动词 (Decision Logic)

编译器在代码审查（Code Review）时应遵循的逻辑：

* **IF (变量 == 名词)**:
* `Static?` -> 存入 `_utils/config.ts` (如：`USER_STATUS_OPTIONS`)
* `UI State?` -> 留在 `.vue` (如：`isModalVisible`, `formData`)


* **IF (方法 == 动词)**:
* `Pure Logic / API?` -> 抽离至 `_hooks/` (如：`fetchData`, `handleValidate`)
* `UI Glue?` -> 留在 `.vue` (如：`toggleModal`, `scrollToTop`)

### 4. 命名与格式规范 (Linting Rules)

这是给 Lint 插件或编译器设置的硬性过滤条件：

| 目标类型 | 命名格式 | 示例 | 强制约束 |
| --- | --- | --- | --- |
| **组件文件** | `PascalCase` | `UserList.vue` | 必须双单词以上 |
| **逻辑文件** | `camelCase` | `useAuth.ts` | **必须**以 `use` 开头 |
| **配置文件** | `camelCase` | `formRules.ts` | 严禁包含 UI 渲染代码 |
| **状态变量** | `camelCase` | `isLoading` | 名词性，Boolean 需带 is/has |
| **业务函数** | `camelCase` | `saveOrder` | 动词开头 |
| **硬编码常量** | `SNAKE_CASE` | `MAX_RETRY_COUNT` | 必须在 config/constants 中定义 |