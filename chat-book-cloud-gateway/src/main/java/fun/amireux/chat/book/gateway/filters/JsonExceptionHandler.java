package fun.amireux.chat.book.gateway.filters;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

// 位于 common 模块
@Component
@Order(-1)
@Slf4j
public class JsonExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String msg = "Internal Server Error";
        if (ex instanceof AuthenticationException) {
            code = HttpStatus.UNAUTHORIZED.value();
            msg = ex.getMessage();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn("[ 认证异常 ] {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getPath(), ex);
        } else if (ex instanceof ErrorResponse errorResponse) {
            HttpStatusCode statusCode = errorResponse.getStatusCode();
            code = statusCode.value();
            msg = errorResponse.getBody().getDetail();
            if (msg == null || msg.isBlank()) {
                msg = statusCode.toString();
            }
            response.setStatusCode(statusCode);
            if (code >= 500) {
                log.error("[ 系统异常 ]: ", ex);
            } else {
                log.warn("[ 请求异常 ] {} {} -> {}", exchange.getRequest().getMethod(),
                        exchange.getRequest().getPath(), msg);
            }
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            log.error("[ 系统异常 ]: ", ex);
        }

        String result = String.format("{\"code\": %d, \"msg\": \"%s\"}", code, msg);
        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
