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
