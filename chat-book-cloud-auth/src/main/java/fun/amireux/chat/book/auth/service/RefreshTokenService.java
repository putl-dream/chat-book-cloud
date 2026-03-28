package fun.amireux.chat.book.auth.service;

import fun.amireux.chat.book.auth.projectobject.RefreshTokenInfo;
import fun.amireux.chat.book.framework.redis.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, Object> objectRedisTemplate;

    @Value("${spring.profiles.active:local}")
    private String env;

    /**
     * Store refresh token metadata in Redis with TTL matching the token's expiry.
     */
    public void store(String jti, RefreshTokenInfo info, Instant expiresAt) {
        String key = RedisKeyConstants.authRefreshToken(env, jti);
        long ttlSeconds = Math.max(1L, Duration.between(Instant.now(), expiresAt).getSeconds());
        objectRedisTemplate.opsForValue().set(key, info, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Stored refresh token jti={}, ttl={}s", jti, ttlSeconds);
    }

    /**
     * Retrieve refresh token metadata. Returns null if not found (revoked/expired).
     */
    public RefreshTokenInfo get(String jti) {
        String key = RedisKeyConstants.authRefreshToken(env, jti);
        Object value = objectRedisTemplate.opsForValue().get(key);
        return value instanceof RefreshTokenInfo ? (RefreshTokenInfo) value : null;
    }

    /**
     * Delete (revoke) a refresh token. Used on rotation and logout.
     */
    public void delete(String jti) {
        String key = RedisKeyConstants.authRefreshToken(env, jti);
        objectRedisTemplate.delete(key);
        log.debug("Deleted refresh token jti={}", jti);
    }
}
