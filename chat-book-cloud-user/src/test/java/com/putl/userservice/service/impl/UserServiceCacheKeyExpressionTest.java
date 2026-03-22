package com.putl.userservice.service.impl;

import com.putl.userservice.controller.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceCacheKeyExpressionTest {

    private static final String CACHE_KEY_EXPRESSION =
            "'ids:' + T(org.springframework.util.StringUtils).collectionToCommaDelimitedString(#ids)";
    private static final String UPDATE_USER_CACHE_KEY_EXPRESSION = "#p0";

    @Test
    void shouldBuildStableCacheKeyForUserIdList() {
        Expression expression = new SpelExpressionParser().parseExpression(CACHE_KEY_EXPRESSION);
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("ids", List.of(3, 1, 2));

        String cacheKey = expression.getValue(context, String.class);

        assertThat(cacheKey).isEqualTo("ids:3,1,2");
    }

    @Test
    void shouldUseExplicitArgumentIndexForUpdateUserCacheEvictionKey() {
        Expression expression = new SpelExpressionParser().parseExpression(UPDATE_USER_CACHE_KEY_EXPRESSION);
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("p0", 1);
        context.setVariable("p1", UserVO.builder().username("Init").build());

        Integer cacheKey = expression.getValue(context, Integer.class);

        assertThat(cacheKey).isEqualTo(1);
    }
}
