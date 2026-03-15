import request from '@/utils/http.js'

/**
 * 获取用户浏览记录
 * @param {string} page
 * @param {string} size
 * @returns
 */
export function getHistory(page, size) {
    return request.get(`/interaction/foot/getHistory`, { params: { page, size } });
}

/**
 * 更新收藏记录
 * @param {string} articleId
 * @returns
 */
export function updateCollection(articleId) {
    return request.post(`/interaction/foot/collection`, null, { params: { articleId } });
}

/**
 * 更新点赞记录
 * @param {string} articleId
 * @returns
 */
export function updatePraise(articleId) {
    return request.post(`/interaction/foot/praise`, null, { params: { articleId } });
}

/**
 * 添加评论
 * @param {object} params 评论参数
 * @param {number} params.articleId 文章id
 * @param {number} params.parentId 父评论id
 * @param {string} params.content 评论内容
 * @returns
 */
export function saveReview(params) {
    return request.post(`/interaction/review/save`, params);
}

/**
 * 获取互动通知（点赞/收藏/评论/浏览）
 * @returns {Promise<NotificationVO[]>}
 */
export function getNotifications() {
    return request.get(`/interaction/foot/getNotifications`);
}

/**
 * 查询文章评论
 * @param {string} articleId
 * @returns
 */
export function getByArticleId(articleId) {
    return request.get(`/interaction/review/getByArticleId?articleId=${articleId}`);
}
