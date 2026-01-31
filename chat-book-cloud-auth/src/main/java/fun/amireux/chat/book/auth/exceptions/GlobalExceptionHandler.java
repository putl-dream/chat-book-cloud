package fun.amireux.chat.book.auth.exceptions;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public CommonResult<?> handleAuthException(AuthenticationException e) {
        return CommonResult.error(ErrorType.ERROR_401.code(), e.getMessage());
    }
}