package fun.amireux.chat.book.gateway.filters;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

// 位于 common 模块
@Component
@Order(-1)
public class JsonExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // 判断是否是 AuthException
        int code = 401;
        String msg = "Internal Server Error";
        if (ex instanceof AuthenticationException) {
            msg = ex.getMessage();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        }

        String result = String.format("{\"code\": %d, \"msg\": \"%s\"}", code, msg);
        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}