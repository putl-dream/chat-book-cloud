package com.putl.articleservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * [Article]实体类  文章表
 *
 * @since 2025-01-13 20:46:01
 */

@Data
@Builder
@TableName("article")
public class ArticleDO implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    //主键id
    private Integer id;

    //作者id
    private Integer userId;

    //作者名
    private String userName;

    //标题
    private String title;

    //封面图
    private String cover;

    // 分类
    private Integer category;

    //摘要
    private String abstractText;

    //文章状态 0-待审核 1-审核通过 -1审核失败
    private Integer status;

    //创建时间
    private LocalDateTime createTime;

    //最后更新时间
    private LocalDateTime updateTime;

}

