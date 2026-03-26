package com.putl.articleservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleReviewAction {
    APPROVE("审核通过"),
    REJECT("审核驳回");

    private final String label;
}
