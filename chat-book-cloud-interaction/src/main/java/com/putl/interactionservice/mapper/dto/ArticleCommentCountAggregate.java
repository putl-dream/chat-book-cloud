package com.putl.interactionservice.mapper.dto;

import lombok.Data;

@Data
public class ArticleCommentCountAggregate {

    private Integer articleId;

    private Long commentCount;
}
