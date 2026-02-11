package com.putl.articleservice.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStatus {
    DRAFT(0),
    UNPUBLISHED(1),
    PUBLISHED(2),
    DELETED(-1);

    @EnumValue
    @JsonValue
    private final int status;
}
