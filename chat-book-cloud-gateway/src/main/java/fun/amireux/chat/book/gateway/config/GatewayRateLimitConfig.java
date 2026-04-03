package fun.amireux.chat.book.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;

@Configuration
public class GatewayRateLimitConfig {

    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter(ReactiveStringRedisTemplate redisTemplate,
                                             @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
                                             ConfigurationService configurationService) {
        return new LoggingRedisRateLimiter(redisTemplate, redisScript, configurationService);
    }

    @Bean
    public KeyResolver userOrIpKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();

            String userId = request.getHeaders().getFirst("X-User-Id");
            if (StringUtils.hasText(userId)) {
                return Mono.just("user:" + userId);
            }

            String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
            if (StringUtils.hasText(forwardedFor)) {
                String clientIp = forwardedFor.split(",")[0].trim();
                if (StringUtils.hasText(clientIp)) {
                    return Mono.just("ip:" + clientIp);
                }
            }

            String realIp = request.getHeaders().getFirst("X-Real-IP");
            if (StringUtils.hasText(realIp)) {
                return Mono.just("ip:" + realIp.trim());
            }

            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                return Mono.just("ip:" + remoteAddress.getAddress().getHostAddress());
            }

            return Mono.just("ip:unknown");
        };
    }
}
