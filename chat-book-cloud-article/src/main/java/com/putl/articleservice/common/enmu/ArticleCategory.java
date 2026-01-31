package com.putl.articleservice.common.enmu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleCategory {
    Algorithm(3),
    Mysql(2),
    Frontend(1),
    Backend(0);
    private final Integer value;
}
