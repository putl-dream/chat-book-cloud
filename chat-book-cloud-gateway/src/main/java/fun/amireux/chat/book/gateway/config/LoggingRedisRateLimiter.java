package fun.amireux.chat.book.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LoggingRedisRateLimiter extends RedisRateLimiter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RedisScript<List<Long>> redisScript;

    public LoggingRedisRateLimiter(ReactiveStringRedisTemplate redisTemplate,
                                   @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
                                   ConfigurationService configurationService) {
        super(redisTemplate, redisScript, configurationService);
        this.redisTemplate = redisTemplate;
        this.redisScript = redisScript;
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        Config routeConfig = loadRouteConfiguration(routeId);

        int replenishRate = routeConfig.getReplenishRate();
        int burstCapacity = routeConfig.getBurstCapacity();
        int requestedTokens = routeConfig.getRequestedTokens();

        try {
            List<String> keys = getKeys(id);
            List<String> scriptArgs = Arrays.asList(
                    String.valueOf(replenishRate),
                    String.valueOf(burstCapacity),
                    "",
                    String.valueOf(requestedTokens)
            );

            Flux<List<Long>> flux = this.redisTemplate.execute(this.redisScript, keys, scriptArgs);
            return flux.onErrorResume(throwable -> {
                log.error("[GatewayRateLimit] Redis rate limiter failed, routeId: {}, key: {}, keys: {}, reason: {}",
                        routeId, id, keys, throwable.getMessage(), throwable);
                return Flux.just(Arrays.asList(1L, -1L));
            }).reduce(new ArrayList<Long>(), (longs, item) -> {
                longs.addAll(item);
                return longs;
            }).map(results -> new Response(results.get(0) == 1L, getHeaders(routeConfig, results.get(1))));
        } catch (Exception ex) {
            log.error("[GatewayRateLimit] Redis rate limiter threw before execution, routeId: {}, key: {}, reason: {}",
                    routeId, id, ex.getMessage(), ex);
        }

        return Mono.just(new Response(true, getHeaders(routeConfig, -1L)));
    }

    private Config loadRouteConfiguration(String routeId) {
        Config routeConfig = getConfig().get(routeId);
        if (routeConfig == null) {
            routeConfig = getConfig().get(RouteDefinitionRouteLocator.DEFAULT_FILTERS);
        }
        if (routeConfig == null) {
            throw new IllegalArgumentException("No Configuration found for route " + routeId + " or defaultFilters");
        }
        return routeConfig;
    }

    private static List<String> getKeys(String id) {
        String prefix = "request_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }
}
