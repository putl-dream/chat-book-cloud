import request from '@/utils/index.js'

/**
 * 上传文件
 * @param {string} file
 * @returns
 */
export function uploadFile(file) {
    // 创建 FormData 对象
    const formData = new FormData();
    // 将文件添加到 FormData 中
    formData.append('file', file);
    // 配置请求头
    const config = {
        headers: {
            'Content-Type': 'multipart/form-data' // 重要：让浏览器自动设置 multipart/form-data 的边界
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
    return request.post(`/article/query?id=${id}`);
}

/**
 * 添加文章
 * @param {object} params ArticleVO
 * @param {Number} params.id
 * @param {string} params.title
 * @param {string} params.content
 * @returns
 */
export function addArticle(params) {
    return request.post(`/article/add`, params);
}

/**
 * 更新文章
 * @param {object} params ArticleVO
 * @returns
 */
export function updateArticle(params) {
    return request.post(`/article/update`, params);
}

/**
 * 删除文章
 * @param {string} id
 * @returns
 */
export function deleteArticle(id) {
    return request.post(`/article/delete?id=${id}`);
}

/**
 * 获取最新文章列表
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
 * 根据关键词搜索文章
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
 * 获取用户发布的文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} userId
 */
export function getUserArticlePage(pageNo, pageSize, userId) {
    return request.post('/page/userArticlePage', { pageNo, pageSize, userId });
}

/**
 * 获取用户草稿箱文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 * @param {string} userId
 */
export function getUserDraftArticlePage(pageNo, pageSize) {
    return request.post('/page/userDraftArticlePage', { pageNo, pageSize });
}

/**
 * 获取管理员审核文章列表
 * @param {number} pageNo
 * @param {number} pageSize
 */
export function getAdminArticlePage(pageNo, pageSize) {
    return request.post('/page/adminArticlePage', { pageNo, pageSize });
}

/**
 * 查询文章评论
 * @param {string} articleId
 * @returns
 */
export function getByArticleId(articleId) {
    return request.get(`/user/review/getByArticleId?articleId=${articleId}`);
}
