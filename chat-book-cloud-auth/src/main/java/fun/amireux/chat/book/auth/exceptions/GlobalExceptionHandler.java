package fun.amireux.chat.book.auth.exceptions;

import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import jakarta.security.auth.message.AuthException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public CommonResult<?> handleAuthException(AuthException e) {
        return CommonResult.error(ErrorType.ERROR_401);
    }
}