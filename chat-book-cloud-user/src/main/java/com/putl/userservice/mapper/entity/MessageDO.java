package com.putl.userservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * [Message]实体类  消息
 *
 * @since 2025-01-16 12:26:36
 */

@Data
@TableName("message")
public class MessageDO {
    @TableId(value = "id", type = IdType.AUTO)
    //主键
    private Integer id;

    //发送者id
    private Integer senderId;

    //接收者id
    private Integer receiverId;

    //文本内容
    private String content;

    //发送时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime sentTime;

    //0-已发送 1-未读 2-已读
    private Integer status;

}

