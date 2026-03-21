import request from '@/utils/http.js'

/**
 * 获取好友列表（详细）
 * @returns
 */
export function getFriendList() {
    return request.get(`/social/friends/detailed`);
}

/**
 * 添加关注
 * @param {string} friendId
 * @returns
 */
export function addFriend(friendId) {
    return request.post(`/social/follow/${friendId}`);
}
