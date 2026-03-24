package com.putl.chatservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.putl.chatservice.entity.MessageDO;
import com.putl.chatservice.mapper.MessageMapper;
import com.putl.chatservice.service.MessageService;
import com.putl.chatservice.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息服务实现类
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(Integer senderId, Integer receiverId, String content, String msgType) {
        MessageDO message = new MessageDO();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMsgType(msgType != null ? msgType : "TEXT");
        message.setStatus(1); // 未读
        message.setSentTime(LocalDateTime.now());

        messageMapper.insert(message);

        return convertToVO(message, senderId);
    }

    @Override
    public List<MessageVO> getHistoryMessages(Integer userId, Integer targetUserId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<MessageDO> messages = messageMapper.selectHistoryMessages(userId, targetUserId, size, offset);

        List<MessageVO> voList = new ArrayList<>();
        for (MessageDO message : messages) {
            voList.add(convertToVO(message, userId));
        }
        return voList;
    }

    @Override
    public Integer getUnreadCount(Integer userId) {
        return messageMapper.countUnreadMessages(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Integer userId, Integer targetUserId) {
        messageMapper.markMessagesAsRead(userId, targetUserId);
    }

    @Override
    public MessageVO queryLastMessage(Integer userId, Integer targetUserId) {
        List<MessageDO> messages = messageMapper.selectHistoryMessages(userId, targetUserId, 1, 0);
        if (messages != null && !messages.isEmpty()) {
            return convertToVO(messages.get(0), userId);
        }
        return null;
    }

    /**
     * 转换为 VO
     */
    private MessageVO convertToVO(MessageDO message, Integer currentUserId) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setMsgType(message.getMsgType());
        vo.setStatus(message.getStatus());
        vo.setSentTime(message.getSentTime());

        // 判断是自己发送的还是对方发送的
        if (message.getSenderId().equals(currentUserId)) {
            vo.setSender("self");
        } else {
            vo.setSender("other");
        }
        return vo;
    }
}
