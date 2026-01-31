package com.putl.articleservice.exception;

/**
 * 文章不存在异常
 *
 * @since 2026-01-31
 */
public class ArticleNotFoundException extends BusinessException {

    public ArticleNotFoundException(Integer articleId) {
        super(404, String.format("文章不存在，articleId: %d", articleId));
    }

    public ArticleNotFoundException(String message) {
        super(404, message);
    }
}
