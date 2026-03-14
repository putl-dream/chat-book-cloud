package com.putl.chatservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
@TableName("message")
public class MessageDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 发送者用户ID
     */
    private Integer senderId;

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
     * 读取时间
     */
    private LocalDateTime readTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
}
