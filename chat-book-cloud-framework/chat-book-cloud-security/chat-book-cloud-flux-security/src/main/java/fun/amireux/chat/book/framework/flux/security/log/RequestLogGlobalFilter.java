package fun.amireux.chat.book.framework.flux.security.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Configuration
public class RequestLogGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        //String path = request.getPath().value();
        String path = request.getPath().pathWithinApplication().value();//打印请求路径
        String requestUrl = this.getOriginalRequestUrl(exchange);//打印请求url
        String method = request.getMethod().name();
        //cors
        HttpHeaders headers = request.getHeaders();

        log.info("-->\n method: {} url: {} header: {}", method, requestUrl, headers);
        if ("POST".equals(method)) {
            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        String bodyString = new String(bytes, StandardCharsets.UTF_8);
                        log.info("-->\n {}", bodyString);
                        exchange.getAttributes().put("POST_BODY", bodyString);
                        DataBufferUtils.release(dataBuffer);
                        Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                            return Mono.just(buffer);
                        });

                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    });
        } else if ("GET".equals(method)) {
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            log.info("请求参数：" + queryParams);
            return chain.filter(exchange);
        }
        return chain.filter(exchange);
    }

    private String getOriginalRequestUrl(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest();

        try {
            // 尝试获取原始请求URL
            LinkedHashSet<URI> uris = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);

            if (uris != null && !uris.isEmpty()) {
                URI requestUri = uris.stream().findFirst().orElse(req.getURI());
                return requestUri.toString();
            }
            // 如果没有找到原始URL，回退到当前请求URI
            return req.getURI().toString();

        } catch (Exception e) {
            // 任何异常情况下回退到当前请求URI
            log.warn("Failed to get original request URL, falling back to current URI", e);
            return req.getURI().toString();
        }
    }


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * 去掉FormData 空格,换行和制表符
     */
    private static String formatStr(String str) {
        if (str != null && str.length() > 0) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            return m.replaceAll("");
        }
        return str;
    }
}
