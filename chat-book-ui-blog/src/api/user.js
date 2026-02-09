import request from '@/utils/http.js'

/**
 * login
 * @param {object} params 登录
 * @param {string} params.email 邮箱
 * @param {string} params.password 密码
 * @param {string} params.loginMethod PASSWORD
 * @returns
 */
export function login(params) {
    return request.post(`/auth/account/login`, {
        ...params,
        loginMethod: 'PASSWORD'
    });
}

/**
 * signIn
 * @param {object} params 注册
 * @param {string} params.username 用户名
 * @param {string} params.email 邮箱
 * @param {string} params.password 密码
 * @param {string} params.captcha 验证码
 * @returns
 */
export function signUp(params) {
    return request.post(`/auth/account/registered`, params);
}

/**
 * captcha
 * @param {string} email
 * @returns
 */
export function captcha(email) {
    return request.get(`/auth/account/captcha`, { params: { email } });
}

/**
 * 查询自己
 * @returns
 */
export function getUserBySelf() {
    return request.get(`/user/bySelf`);
}

/**
 * 根据id查询用户
 * @param {string} id
 * @returns
 */
export function getUserById(id) {
    return request.get(`/user/byId`, { params: { id } });
}

/**
 * 根据id查询用户好友列表
 * @returns
 */
export function getFriendList() {
    return request.get(`/user/friendList`);
}

/**
 * 添加关注
 * @param {string} userId
 * @param {string} friendId
 * @returns
 */
export function addFriend(userId, friendId) {
    return request.get(`/user/friends`, { params: { userId, friendId } });
}

/**
 * 更新用户信息
 * @param {object} data
 * @returns
 */
export function updateUser(data) {
    return request.post(`/user/update`, data);
}

/**
 * 查询用户消息
 * @param {string} receiveId
 * @returns
 */
export function queryUserMessage(receiveId) {
    return request.get(`/user/message/queryUserMessage`, { params: { receiveId } });
}

/**
 * 分页查询文章
 * @param {string} pageNum
 * @param {string} pageSize
 * @returns
 */
export function queryArticlePage(pageNum, pageSize) {
    return request.post(`/article/queryPage?pageNum=${pageNum}&pageSize=${pageSize}`);
}

/**
 * 获取用户浏览记录
 * @param {string} page
 * @param {string} size
 * @returns
 */
export function getHistory(page, size) {
    return request.get(`/user/foot/getHistory`, { params: { page, size } });
}

/**
 * 更新收藏记录
 * @param {string} articleId
 * @returns
 */
export function updateCollection(articleId) {
    return request.get(`/user/foot/collection`, { params: { articleId } });
}

/**
 * 更新点赞记录
 * @param {string} articleId
 * @returns
 */
export function updatePraise(articleId) {
    return request.get(`/user/foot/praise`, { params: { articleId } });
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
    return request.post(`/user/review/save`, params);
}

/**
 * 获取消息
 * @returns
 */
export function getMessage() {
    return request.get(`/user/foot/getMessage`);
}