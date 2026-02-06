package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.userservice.controller.vo.MessageVO;
import com.putl.userservice.mapper.MessageMapper;
import com.putl.userservice.mapper.entity.MessageDO;
import com.putl.userservice.service.MessageService;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * [Message]表服务接口  消息
 *
 * @since 2025-01-16 12:26:36
 */

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, MessageDO> implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public List<MessageDO> queryUserMessage(Integer fromUserId, Integer toUserId){
        return messageMapper.selectList(Wrappers.<MessageDO>lambdaQuery()
                .and(wrapper -> {wrapper.eq(MessageDO::getReceiverId, fromUserId).eq(MessageDO::getSenderId, toUserId);})
                .or(wrapper -> {wrapper.eq(MessageDO::getReceiverId, toUserId).eq(MessageDO::getSenderId, fromUserId);})
                .orderByAsc(MessageDO::getSentTime)
        );
    }

    @Override
    public boolean save(MessageVO message){
        MessageDO messageDO = BeanUtil.toBean(message, MessageDO.class);
        return this.save(messageDO);
    }

    @Override
    public MessageDO queryLastMessage(Integer fromUserId, Integer toUserId){
        return messageMapper.selectOne(Wrappers.<MessageDO>lambdaQuery()
                .and(wrapper -> {wrapper.eq(MessageDO::getReceiverId, fromUserId).eq(MessageDO::getSenderId, toUserId);})
                .or(wrapper -> {wrapper.eq(MessageDO::getReceiverId, toUserId).eq(MessageDO::getSenderId, fromUserId);})
                .orderByDesc(MessageDO::getSentTime).last("limit 1")
        );
    }
}

