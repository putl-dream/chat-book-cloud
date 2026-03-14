package fun.amireux.chat.book.framework.common.exception;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

/**
 * 统一全局异常处理器
 * 所有微服务引入此模块后自动获得统一的异常处理能力
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 认证异常 ====================

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResult<Void> handleAuthentication(AuthenticationException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return CommonResult.error(ErrorType.ERROR_401.code(), ex.getMessage());
    }

    // ==================== 参数校验异常 ====================

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult<Void> handleBadRequest(Exception ex) {
        String msg = extractMessage(ex);
        log.warn("请求参数错误: {}", msg);
        return CommonResult.error(ErrorType.ERROR_400.code(), msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("参数异常: {}", ex.getMessage());
        return CommonResult.error(ErrorType.ERROR_400.code(), ex.getMessage());
    }

    // ==================== 数据库异常 ====================

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult<Void> handleDatabaseException(DataAccessException ex) {
        log.error("数据库异常: {}", ex.getMessage(), ex);
        return CommonResult.error(ErrorType.ERROR_500.code(), "数据库操作失败，请稍后重试");
    }

    // ==================== 通用异常 ====================

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult<Void> handleException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        return CommonResult.error(ErrorType.ERROR_500);
    }

    // ==================== 私有方法 ====================

    private String extractMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manv) {
            if (manv.getBindingResult().getFieldError() != null) {
                return manv.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (ex instanceof BindException be) {
            if (be.getBindingResult().getFieldError() != null) {
                return be.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (ex instanceof MissingServletRequestParameterException msrpe) {
            return "缺少请求参数: " + msrpe.getParameterName();
        } else if (ex instanceof MethodArgumentTypeMismatchException matme) {
            return "参数类型错误: " + matme.getName();
        } else if (ex instanceof ConstraintViolationException cve) {
            return cve.getMessage();
        }

        String msg = ex.getMessage();
        return (msg != null && !msg.isBlank()) ? msg : "请求参数错误";
    }
}
