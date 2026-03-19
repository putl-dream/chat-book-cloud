package com.putl.articleservice.controller;

import com.putl.articleservice.exception.BusinessException;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.putl.articleservice")
public class ArticleExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public CommonResult<Void> handleBusinessException(BusinessException ex) {
        log.warn("文章业务异常: {}", ex.getMessage());
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }
}
