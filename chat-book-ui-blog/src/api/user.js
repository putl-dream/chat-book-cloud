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
    return request.post(`/api/auth/account/login`, {
        ...params,
        loginMethod: 'PASSWORD'
    });
}

/**
 * 验证码登录
 * @param {object} params 登录
 * @param {string} params.email 邮箱
 * @param {string} params.verificationCode 验证码
 * @returns
 */
export function loginByEmailCode(params) {
    return request.post(`/api/auth/account/login`, {
        email: params.email,
        loginMethod: 'VERIFICATION_CODE',
        verificationCode: params.verificationCode
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
    return request.post(`/api/auth/account/registered`, params);
}

/**
 * captcha
 * @param {string} email
 * @returns
 */
export function captcha(email) {
    return request.get(`/api/auth/account/captcha`, { params: { email } });
}

/**
 * 查询自己
 * @returns
 */
export function getUserBySelf() {
    return request.get(`/api/user/bySelf`);
}

/**
 * 根据id查询用户
 * @param {string} id
 * @returns
 */
export function getUserById(id) {
    return request.get(`/api/user/byId`, { params: { id } });
}

/**
 * 根据id查询用户好友列表
 * @returns
 */
export function getFriendList() {
    return request.get(`/api/user/friendList`);
}

/**
 * 添加关注
 * @param {string} userId
 * @param {string} friendId
 * @returns
 */
export function addFriend(userId, friendId) {
    return request.post(`/api/social/follow`, null, { params: { userId, friendId } });
}

/**
 * 更新用户信息
 * @param {object} data
 * @returns
 */
export function updateUser(data) {
    return request.post(`/api/user/update`, data);
}

/**
 * 查询用户消息
 * @param {string} receiveId
 * @returns
 */
export function queryUserMessage(receiveId) {
    return request.get(`/api/user/message/queryUserMessage`, { params: { receiveId } });
}

/**
 * 分页查询文章
 * @param {string} pageNum
 * @param {string} pageSize
 * @returns
 */
export function queryArticlePage(pageNum, pageSize) {
    return request.post(`/api/article/queryPage?pageNum=${pageNum}&pageSize=${pageSize}`);
}

/**
 * 获取用户浏览记录
 * @param {string} page
 * @param {string} size
 * @returns
 */
export function getHistory(page, size) {
    return request.get(`/api/interaction/foot/getHistory`, { params: { page, size } });
}

/**
 * 更新收藏记录
 * @param {string} articleId
 * @returns
 */
export function updateCollection(articleId) {
    return request.post(`/api/interaction/foot/collection`, null, { params: { articleId } });
}

/**
 * 更新点赞记录
 * @param {string} articleId
 * @returns
 */
export function updatePraise(articleId) {
    return request.post(`/api/interaction/foot/praise`, null, { params: { articleId } });
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
    return request.post(`/api/interaction/review/save`, params);
}

/**
 * 获取互动通知（点赞/收藏/评论/浏览）
 * P0 Fix: 原 /getMessage 接口返回数据有误，已修复为 /getNotifications
 * @returns {Promise<NotificationVO[]>}
 */
export function getNotifications() {
    return request.get(`/api/interaction/foot/getNotifications`);
}
