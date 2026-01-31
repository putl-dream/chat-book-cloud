package fun.amireux.chat.book.gateway.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.amireux.chat.book.gateway.config.FeatureToggleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeatureToggleFilter implements GlobalFilter, Ordered {

    private final FeatureToggleProperties featureToggleProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        List<FeatureToggleProperties.FeatureToggleRule> rules = featureToggleProperties.getRules();

        if (CollectionUtils.isEmpty(rules)) {
            return chain.filter(exchange);
        }

        for (FeatureToggleProperties.FeatureToggleRule rule : rules) {
            // 如果规则启用了（enabled=true），则说明未下架，跳过检查
            if (rule.isEnabled()) {
                continue;
            }

            // 检查路径是否匹配
            if (antPathMatcher.match(rule.getPattern(), path)) {
                // 检查方法是否匹配 (如果 methods 为空则默认匹配所有方法，或者按需调整策略，这里假设非空)
                if (rule.getMethods() == null || rule.getMethods().contains(method)) {
                    log.warn("[ 接口已下架拦截 ]: path={}, method={}, rule={}", path, method, rule.getName());
                    return blockRequest(exchange, rule.getMessage());
                }
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> blockRequest(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("message", message != null ? message : "Service Unavailable");
        body.put("success", false);
        body.put("timestamp", System.currentTimeMillis());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            bytes = "{\"code\":503,\"message\":\"Service Unavailable\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 优先级非常高
    }
}
