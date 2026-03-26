package com.putl.articleservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_review_log")
public class ArticleReviewLogDO implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer articleId;

    private Integer reviewerId;

    private String reviewerName;

    private String reviewAction;

    private String reviewReason;

    private String batchId;

    private LocalDateTime createTime;
}
