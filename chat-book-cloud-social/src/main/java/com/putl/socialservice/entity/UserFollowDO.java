package com.putl.socialservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体类
 */
@Data
@Builder
@TableName("user_follow")
public class UserFollowDO {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关注者用户ID
     */
    private Integer userId;

    /**
     * 被关注者用户ID
     */
    private Integer followId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
