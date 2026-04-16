package com.putl.articleservice.mapper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSystemTagPair {
    private Integer articleId;
    private Integer systemTagId;
}
