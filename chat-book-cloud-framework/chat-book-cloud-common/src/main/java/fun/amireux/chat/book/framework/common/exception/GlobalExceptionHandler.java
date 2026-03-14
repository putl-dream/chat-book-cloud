package fun.amireux.chat.book.framework.common.exception;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一全局异常处理器 - 兜底处理
 * 各微服务可自行定义更细粒度的异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public CommonResult<Void> handleAuthentication(AuthenticationException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return CommonResult.error(ErrorType.ERROR_401.code(), ex.getMessage());
    }


    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        return CommonResult.error(ErrorType.ERROR_500);
    }
}
