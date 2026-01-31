package com.putl.userservice.controller.vo;

import lombok.Data;

/**
 * [Message]实体类  消息
 *
 * @since 2025-01-16 12:26:36
 */

@Data
public class MessageVO {
    //发送者id
    private Integer senderId;

    //接收者id
    private Integer receiverId;

    //文本内容
    private String content;
}

