package com.putl.chatservice.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息 VO
 */
@Data
public class MessageVO {

    private Integer id;

    /**
     * 发送者用户ID
     */
    private Integer senderId;

    /**
     * 发送者用户名
     */
    private String senderUsername;

    /**
     * 发送者头像
     */
    private String senderPhoto;

    /**
     * 接收者用户ID
     */
    private Integer receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: TEXT/IMAGE/FILE
     */
    private String msgType;

    /**
     * 消息状态: 0=已发送, 1=未读, 2=已读
     */
    private Integer status;

    /**
     * 发送时间
     */
    private LocalDateTime sentTime;

    /**
     * 发送方标识: self=自己发送, other=对方发送
     */
    private String sender;
}
