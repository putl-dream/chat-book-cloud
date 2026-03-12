package com.putl.userservice.config;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public CommonResult<Void> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        return CommonResult.error(401, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return CommonResult.error(ErrorType.ERROR_400, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public CommonResult<Void> handleRuntime(RuntimeException ex) {
        log.error("Runtime error", ex);
        return CommonResult.error(ErrorType.ERROR_500, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleException(Exception ex) {
        log.error("Unhandled error", ex);
        return CommonResult.error(ErrorType.ERROR_500);
    }
}
