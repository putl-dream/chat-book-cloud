import request from "@/utils/http.js";

/**
 * 获取历史消息
 * @param {number} targetUserId 对方用户ID
 * @param {number} page 页码
 * @param {number} size 每页数量
 * @returns
 */
export function getChatHistory(targetUserId, page = 1, size = 50) {
    return request.get(`/chat/messages`, { params: { targetUserId, page, size } });
}

/**
 * 获取未读消息数
 * @returns
 */
export function getUnreadCount() {
    return request.get(`/chat/unread/count`);
}

/**
 * 标记消息已读
 * @param {number} targetUserId 对方用户ID
 * @returns
 */
export function markAsRead(targetUserId) {
    return request.put(`/chat/messages/read`, null, { params: { targetUserId } });
}
