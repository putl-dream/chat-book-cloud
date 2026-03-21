import request from '@/utils/http.js'

/**
 * 获取好友列表（详细）
 * @returns
 */
export function getFriendList() {
    return request.get(`/social/friends/detailed`);
}

/**
 * 关注用户
 * @param {string|number} userId
 * @returns
 */
export function followUser(userId) {
    return request.post(`/social/follow/${userId}`);
}

/**
 * 取消关注
 * @param {string|number} userId
 * @returns
 */
export function unfollowUser(userId) {
    return request.delete(`/social/follow/${userId}`);
}

/**
 * 查询与目标用户的关注关系
 * @param {string|number} userId
 * @returns
 */
export function getFriendRelation(userId) {
    return request.get(`/social/relation/${userId}`);
}

/**
 * 兼容旧命名
 * @param {string|number} friendId
 * @returns
 */
export function addFriend(friendId) {
    return followUser(friendId);
}
