package com.putl.articleservice.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章标签对
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleTagPair {
    public Integer articleId;
    public Integer tagId;
}