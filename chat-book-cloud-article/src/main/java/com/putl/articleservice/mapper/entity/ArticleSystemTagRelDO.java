package com.putl.articleservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_system_tag_rel")
public class ArticleSystemTagRelDO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer articleId;

    private Integer systemTagId;

    private String source;

    private BigDecimal confidence;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
