package com.putl.userservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * [Review]实体类  评论表
 *
 * @since 2025-01-18 12:03:23
 */

@Data
@TableName("review")
public class ReviewDO {
    @TableId(value = "id", type = IdType.AUTO)
    //评论主键
    private Integer id;

    //文章ID，关联到文章表
    private Integer textId;

    //评论者ID
    private Integer userId;

    //父评论ID，0-评论文章，other-评论用户评论
    private Integer parentId;

    //评论内容
    private String content;

    //评论创建时间
    private LocalDateTime createTime;

    //更新时间
    private LocalDateTime updateTime;
}

