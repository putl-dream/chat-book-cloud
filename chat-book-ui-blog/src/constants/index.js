/**
 * 文章分类枚举
 */
export const CATEGORY_ENUM = {
    BACKEND: 0,
    FRONTEND: 1,
    MYSQL: 2,
    ALGORITHM: 3,
    OTHER: 4,
};

/**
 * 文章分类名称映射
 */
export const CATEGORY_NAMES = {
    [CATEGORY_ENUM.BACKEND]: '后端',
    [CATEGORY_ENUM.FRONTEND]: '前端',
    [CATEGORY_ENUM.MYSQL]: 'MySQL',
    [CATEGORY_ENUM.ALGORITHM]: '算法',
    [CATEGORY_ENUM.OTHER]: '其他',
};

/**
 * 内容类型枚举
 */
export const CONTENT_TYPE_ENUM = {
    LEARN: 0,      // 学习/教程
    PRACTICE: 1,    // 实战/项目
};

/**
 * 内容类型名称映射
 */
export const CONTENT_TYPE_NAMES = {
    [CONTENT_TYPE_ENUM.LEARN]: '学习',
    [CONTENT_TYPE_ENUM.PRACTICE]: '实战',
};

/**
 * 标签类型枚举
 */
export const TAG_TYPE_ENUM = {
    TECH: 1,    // 技术栈
    PATH: 2,    // 学习路径
    TOPIC: 3,   // 主题标签
};

/**
 * 标签类型名称映射
 */
export const TAG_TYPE_NAMES = {
    [TAG_TYPE_ENUM.TECH]: '技术栈',
    [TAG_TYPE_ENUM.PATH]: '学习路径',
    [TAG_TYPE_ENUM.TOPIC]: '主题标签',
};

/**
 * 文章类型
 */
export const ARTICLE_TYPE_ENUM = {
    ORIGINAL: 'ORIGINAL',
    REPRINT: 'REPRINT',
    TRANSLATION: 'TRANSLATION',
};

export const ARTICLE_TYPE_NAMES = {
    [ARTICLE_TYPE_ENUM.ORIGINAL]: '原创',
    [ARTICLE_TYPE_ENUM.REPRINT]: '转载',
    [ARTICLE_TYPE_ENUM.TRANSLATION]: '翻译',
};

/**
 * 创作声明
 */
export const CREATION_STATEMENT_ENUM = {
    PERSONAL_VIEW: 'PERSONAL_VIEW',
    NETWORK_SOURCE: 'NETWORK_SOURCE',
    AI_ASSISTED: 'AI_ASSISTED',
};

export const CREATION_STATEMENT_NAMES = {
    [CREATION_STATEMENT_ENUM.PERSONAL_VIEW]: '个人观点',
    [CREATION_STATEMENT_ENUM.NETWORK_SOURCE]: '网络来源',
    [CREATION_STATEMENT_ENUM.AI_ASSISTED]: 'AI辅助创作',
};

/**
 * HTTP 状态码
 */
export const HTTP_STATUS = {
    OK: 200,
    BAD_REQUEST: 400,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    INTERNAL_ERROR: 500,
};

/**
 * 默认头像
 */
export const DEFAULT_AVATAR = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
