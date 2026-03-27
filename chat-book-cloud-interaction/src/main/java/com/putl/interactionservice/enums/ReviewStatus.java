package com.putl.interactionservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论状态枚举
 */
@Getter
@AllArgsConstructor
public enum ReviewStatus {
    NORMAL(0, "正常"),
    DELETED(1, "已删除"),
    HIDDEN(2, "已屏蔽");

    private final Integer code;
    private final String desc;
}
