package com.putl.articleservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStatus {
    DRAFT(0),
    UNPUBLISHED(1),
    PUBLISHED(2),
    DELETED(-1);
    private final int status;
}
