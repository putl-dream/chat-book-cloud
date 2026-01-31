package com.putl.userservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * [UserFriends]实体类
 *
 * @since 2025-01-15 16:49:34
 */

@Data
@Builder
@TableName("user_friends")
public class UserFriendsDO {
    @TableId(value = "id", type = IdType.AUTO)
    //主键
    private Integer id;

    //当前用户
    private Integer userId1;

    //被关注用户
    private Integer userId2;

    //0-关注 1-好友 2-拉黑
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}

