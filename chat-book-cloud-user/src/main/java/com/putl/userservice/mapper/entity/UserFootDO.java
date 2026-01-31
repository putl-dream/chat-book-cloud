package com.putl.userservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * [UserFoot]实体类  用户足迹表
 *
 * @since 2025-01-17 16:00:13
 */

@Data
@Builder
@TableName("user_foot")
public class UserFootDO {
    @TableId(value = "id", type = IdType.AUTO)
    //主键id
    private Integer id;

    //用户ID
    private Integer userId;

    //文本id(文章/评论)
    private Integer documentId;

    //文本类型：1-文章，2评论
    private Integer documentType;

    //发布该文本的用户id
    private Integer documentUserId;

    //收藏状态 0-未收藏 1-已收藏
    private Integer collectionStat;

    //浏览状态 0-未读 1-已读
    private Integer readStat;

    //评论状态 0-未评论 1-已评论
    private Integer commentStat;

    //点赞状态 0-未点赞 1-已点赞
    private Integer praiseStat;

    //创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;



}

