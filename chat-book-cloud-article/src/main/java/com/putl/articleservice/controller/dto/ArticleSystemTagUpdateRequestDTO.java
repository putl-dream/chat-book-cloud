package com.putl.articleservice.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArticleSystemTagUpdateRequestDTO {

    private Integer articleId;

    private List<Integer> systemTagIds;
}
