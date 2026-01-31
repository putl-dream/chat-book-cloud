package com.putl.articleservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * [ArticleInfo]实体类  文章详情表
 *
 * @since 2025-01-14 20:46:13
 */

@Data
@Builder
@TableName("article_info")
public class ArticleInfoDO implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    //主键id
    private Integer id;

    //用户id
    private Integer userId;

    //文章id
    private Integer articleId;

    //作者昵称
    private String userName;

    //文章标题
    private String title;

    //内容
    private String content;

    //创建时间
    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

