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
};

/**
 * 标签类型名称映射
 */
export const TAG_TYPE_NAMES = {
    [TAG_TYPE_ENUM.TECH]: '技术栈',
    [TAG_TYPE_ENUM.PATH]: '学习路径',
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
