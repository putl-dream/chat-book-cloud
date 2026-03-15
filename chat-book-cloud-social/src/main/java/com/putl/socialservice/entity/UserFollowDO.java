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
     * 状态：0-关注 1-好友 2-拉黑
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Boolean deleted;
}
