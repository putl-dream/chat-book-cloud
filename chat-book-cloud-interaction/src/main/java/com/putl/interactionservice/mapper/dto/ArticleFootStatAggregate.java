package com.putl.interactionservice.mapper.dto;

import lombok.Data;

@Data
public class ArticleFootStatAggregate {

    private Integer articleId;

    private Long totalCount;

    private Long readCount;

    private Long praiseCount;

    private Long collectCount;
}
