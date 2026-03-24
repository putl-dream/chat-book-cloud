package com.putl.interactionservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("article_stat")
public class ArticleStatDO {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer articleId;
    private Long viewCount;
    private Long praiseCount;
    private Long commentCount;
    private Long collectCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
