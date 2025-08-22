package fun.amireux.chat.book.framework.flux.security.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class RequestBodyLoggingFilter implements WebFilter {

    private static final List<String> BODY_METHODS = List.of("POST", "PUT", "PATCH");
    private static final String ALREADY_LOGGED_ATTR = RequestBodyLoggingFilter.class.getName() + ".ALREADY_LOGGED";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 检查是否已经处理过
        if (exchange.getAttribute(ALREADY_LOGGED_ATTR) != null) {
            return chain.filter(exchange);
        }

        // 如果类型是文件的直接丢出去
        MediaType contentType = exchange.getRequest().getHeaders().getContentType();
        if (contentType != null && contentType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // 只对有 body 的请求处理
        if (!BODY_METHODS.contains(method)) {
            log.info("Reactive \nRequest: {} {}, \nbody: (no body)\n", method, path);
            return chain.filter(exchange);
        }

        // 标记已处理
        exchange.getAttributes().put(ALREADY_LOGGED_ATTR, true);

        // 缓存 body 并读取为字符串
        return DataBufferUtils.join(request.getBody())
                .defaultIfEmpty(exchange.getResponse().bufferFactory().allocateBuffer(0))
                .flatMap(dataBuffer -> {
                    String body;
                    boolean hasBody = dataBuffer.readableByteCount() > 0;

                    if (hasBody) {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        body = new String(bytes, StandardCharsets.UTF_8);
                    } else {
                        body = "(empty body)";
                    }

                    if (hasBody) {
                        // 保留 buffer，供后续使用
                        DataBufferUtils.retain(dataBuffer);
                        DataBuffer finalBuffer = dataBuffer;

                        // 包装 request，替换 body
                        ServerHttpRequestDecorator mutatedRequest = new ServerHttpRequestDecorator(request) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                DataBufferUtils.retain(finalBuffer);
                                return Flux.just(finalBuffer);
                            }
                        };

                        // 打印日志
                        log.info("Reactive \nRequest: {} {}, \n{}\n", method, path, body);

                        // 继续过滤链，使用包装后的 exchange
                        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                                .doFinally(signalType -> DataBufferUtils.release(finalBuffer));
                    } else {
                        // 打印日志
                        log.info("Reactive \nRequest: {} {}, Body: \n{}\n", method, path, body);
                        return chain.filter(exchange);
                    }
                })
                .onErrorResume(ex -> {
                    log.warn("Failed to read body for {} {}", method, path, ex);
                    return chain.filter(exchange);
                });
    }
}
