package com.putl.userservice.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceCacheKeyExpressionTest {

    private static final String CACHE_KEY_EXPRESSION =
            "'ids:' + T(org.springframework.util.StringUtils).collectionToCommaDelimitedString(#ids)";

    @Test
    void shouldBuildStableCacheKeyForUserIdList() {
        Expression expression = new SpelExpressionParser().parseExpression(CACHE_KEY_EXPRESSION);
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("ids", List.of(3, 1, 2));

        String cacheKey = expression.getValue(context, String.class);

        assertThat(cacheKey).isEqualTo("ids:3,1,2");
    }
}
