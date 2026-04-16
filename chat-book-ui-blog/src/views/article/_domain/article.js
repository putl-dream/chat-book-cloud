import request from '@/utils/http.js'

/**
 * 上传文件
 * @param {string} file
 * @returns
 */
export function uploadFile(file) {
    // 创建 FormData 对象
    const formData = new FormData();
    // 将文件添加到 FormData ?
    formData.append('file', file);
    // 配置请求?
    const config = {
        headers: {
            'Content-Type': 'multipart/form-data' // 重要：让浏览器自动设?multipart/form-data 的边?
        }
    };
    return request.post(`/article/file/upload`, formData, config);
}

/**
 * 查询文章
 * @param {string} id
 * @returns
 */
export function queryArticle(id) {
    return request.get(`/article/query`, { params: { id } });
}

/**
 * 保存草稿
 * @param {object} params ArticleVO
 * @returns
 */
export function saveDraftArticle(params) {
    return request.post(`/article/saveDraft`, params);
}

/**
 * 发布文章
 * @param {object} params ArticleVO
 * @returns
 */
export function publishArticle(params) {
    return request.post(`/article/publish`, params);
}

/**
 * 兼容旧接口：统一映射到草稿保?
 * @param {object} params ArticleVO
 * @returns
 */
export function addArticle(params) {
    return saveDraftArticle(params);
}

/**
 * 兼容旧接口：统一映射到草稿保?
 * @param {object} params ArticleVO
 * @returns
 */
export function updateArticle(params) {
    return saveDraftArticle(params);
}

/**
 * 删除文章
 * @param {string} id
 * @returns
 */
export function deleteArticle(id) {
    return request.delete(`/article/delete`, { params: { id } });
}

/**
 * 获取最新文章列?
 * @param {number} pageNo
 * @param {number} pageSize
 */
export function getNewPage(pageNo, pageSize) {
    return request.post('/page/newPage', { pageNo, pageSize });
}

/**
 * 获取热门文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 */
export function getHotPage(pageNo, pageSize) {
    return request.post('/page/hotPage', { pageNo, pageSize });
}

/**
 * 获取今日热门文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 */
export function getTodayHotPage(pageNo, pageSize) {
    return request.post('/page/todayHotPage', { pageNo, pageSize });
}

/**
 * 根据分类获取文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} category
 */
export function getCategoryPage(pageNo, pageSize, category) {
    return request.post('/page/categoryPage', { pageNo, pageSize, category });
}

/**
 * 根据关键词搜索文?
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} keyword
 */
export function getLikePage(pageNo, pageSize, keyword) {
    return request.post('/page/likePage', { pageNo, pageSize, keyword });
}

/**
 * 获取系统推荐文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 */
export function getSystemRecommendPage(pageNo, pageSize) {
    return request.post('/page/systemRecommendPage', { pageNo, pageSize });
}

/**
 * 获取个性化推荐文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 */
export function getPersonalRecommendPage(pageNo, pageSize) {
    return request.post('/page/personalRecommendPage', { pageNo, pageSize });
}

/**
 * 获取用户历史阅读文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} userId
 */
export function getUserHistoryPage(pageNo, pageSize) {
    return request.post('/page/userHistoryPage', { pageNo, pageSize });
}

/**
 * 获取用户收藏文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} userId
 */
export function getUserCollectPage(pageNo, pageSize) {
    return request.post('/page/userCollectPage', { pageNo, pageSize });
}

/**
 * 获取用户发布的文章列?
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} userId
 */
export function getUserArticlePage(pageNo, pageSize) {
    return request.post('/page/userArticlePage', { pageNo, pageSize });
}

/**
 * 获取用户草稿箱文章列?
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} userId
 */
export function getUserDraftArticlePage(pageNo, pageSize) {
    return request.post('/page/userDraftArticlePage', { pageNo, pageSize });
}

/**
 * 获取管理员审核文章列?
 * @param {number} pageNo
 * @param {number} pageSize
 */
export function getAdminArticlePage(pageNo, pageSize) {
    return request.post('/page/adminArticlePage', { pageNo, pageSize });
}

/**
 * 根据内容类型获取文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {number} contentType
 */
export function getContentTypePage(pageNo, pageSize, contentType) {
    return request.post('/page/contentTypePage', { pageNo, pageSize, contentType });
}

/**
 * 根据标签获取文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} authorTagName
 */
export function getTagPage(pageNo, pageSize, authorTagName) {
    return request.post('/page/tagPage', { pageNo, pageSize, authorTagName });
}

/**
 * 获取相关推荐文章列表
 * @param {number} articleId
 * @param {number} pageNo
 * @param {number} pageSize
 */
export function getRelatedPage(articleId, pageNo, pageSize) {
    return request.post('/page/relatedPage', { articleId, pageNo, pageSize });
}
