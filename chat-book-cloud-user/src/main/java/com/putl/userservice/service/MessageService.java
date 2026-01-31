package com.putl.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.userservice.controller.vo.MessageVO;
import com.putl.userservice.mapper.entity.MessageDO;

import java.util.List;

/**
 * [Message]表服务接口  消息
 *
 * @since 2025-01-16 12:26:36
 */
public interface MessageService extends IService<MessageDO> {

    // 查询用户之间的消息
    //fixme : 后续可改为分页查询
    List<MessageDO> queryUserMessage(Integer fromUserId, Integer toUserId);

    boolean save(MessageVO message);

    // 最后一条消息
    MessageDO queryLastMessage(Integer fromUserId, Integer toUserId);

}

