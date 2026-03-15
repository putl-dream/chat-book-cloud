package com.putl.chatservice.service;

import com.putl.chatservice.vo.MessageVO;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 发送消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param msgType    消息类型
     * @return 消息VO
     */
    MessageVO sendMessage(Integer senderId, Integer receiverId, String content, String msgType);

    /**
     * 获取历史消息
     *
     * @param userId     当前用户ID
     * @param targetUserId 对方用户ID
     * @param page       页码
     * @param size       每页数量
     * @return 消息列表
     */
    List<MessageVO> getHistoryMessages(Integer userId, Integer targetUserId, Integer page, Integer size);

    /**
     * 获取未读消息数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    Integer getUnreadCount(Integer userId);

    /**
     * 标记消息为已读
     *
     * @param userId       当前用户ID
     * @param targetUserId 对方用户ID
     */
    void markAsRead(Integer userId, Integer targetUserId);

    /**
     * 查询最后一条消息
     * @param userId 当前用户ID
     * @param targetUserId 对方用户ID
     * @return 消息VO
     */
    MessageVO queryLastMessage(Integer userId, Integer targetUserId);
}
