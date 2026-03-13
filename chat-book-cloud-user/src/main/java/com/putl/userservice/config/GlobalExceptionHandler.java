package com.putl.userservice.config;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public CommonResult<Void> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        return CommonResult.error(401, ex.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class
    })
    public CommonResult<Void> handleBadRequest(Exception ex) {
        String msg = null;
        if (ex instanceof MethodArgumentNotValidException manv) {
            if (manv.getBindingResult().getFieldError() != null) {
                msg = manv.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (ex instanceof BindException be) {
            if (be.getBindingResult().getFieldError() != null) {
                msg = be.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (ex instanceof MissingServletRequestParameterException msrpe) {
            msg = "缺少请求参数: " + msrpe.getParameterName();
        } else if (ex instanceof MethodArgumentTypeMismatchException matme) {
            msg = "参数类型错误: " + matme.getName();
        } else if (ex instanceof ConstraintViolationException cve) {
            msg = cve.getMessage();
        }

        if (msg == null || msg.isBlank()) {
            msg = ex.getMessage();
        }
        log.warn("Bad request: {}", msg);
        return CommonResult.error(ErrorType.ERROR_400, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return CommonResult.error(ErrorType.ERROR_400, ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public CommonResult<Void> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        int code = status != null ? status.value() : 500;
        String msg = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        if (code >= 400 && code < 500) {
            log.warn("Request error: status={}, msg={}", code, msg);
        } else {
            log.error("ResponseStatusException: status={}, msg={}", code, msg, ex);
        }
        return CommonResult.error(code, msg);
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
