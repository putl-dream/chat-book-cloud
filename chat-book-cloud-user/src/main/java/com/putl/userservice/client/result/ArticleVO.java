package com.putl.userservice.client.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "ArticleVO")
public class ArticleVO {
    private Integer id;
    private String title;
    private String author;
    private Integer authorId;
    private String createTime;
    private String content;
}