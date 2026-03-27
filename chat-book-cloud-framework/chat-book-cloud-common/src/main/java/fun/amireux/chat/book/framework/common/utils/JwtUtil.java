package fun.amireux.chat.book.framework.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class JwtUtil {

    public static final String TOKEN_TYPE_ACCESS  = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final String ISSUER;
    private final Duration ACCESS_EXPIRATION;
    private final Duration REFRESH_EXPIRATION;
    private final Algorithm ALGORITHM;

    public JwtUtil(String SECRET, String ISSUER) {
        this(SECRET, ISSUER, Duration.ofMinutes(15), Duration.ofDays(7));
    }

    public JwtUtil(String SECRET, String ISSUER, Duration ACCESS_EXPIRATION, Duration REFRESH_EXPIRATION) {
        this.ISSUER = ISSUER;
        this.ACCESS_EXPIRATION  = ACCESS_EXPIRATION;
        this.REFRESH_EXPIRATION = REFRESH_EXPIRATION;
        this.ALGORITHM = Algorithm.HMAC256(SECRET);
    }

    /**
     * Generate an access token. Adds type="access" and a unique jti claim.
     */
    public String generateAccessToken(Map<String, ?> claims) {
        Map<String, Object> enriched = enrichClaims(claims, TOKEN_TYPE_ACCESS);
        return signToken(enriched, ACCESS_EXPIRATION);
    }

    /**
     * Generate a refresh token. Adds type="refresh" and a unique jti claim.
     */
    public String generateRefreshToken(Map<String, ?> claims) {
        Map<String, Object> enriched = enrichClaims(claims, TOKEN_TYPE_REFRESH);
        return signToken(enriched, REFRESH_EXPIRATION);
    }

    /**
     * Extract the JWT ID (jti) from a verified token.
     */
    public String getJti(DecodedJWT jwt) {
        return jwt.getClaim("jti").asString();
    }

    /**
     * Extract the token type from a verified token.
     */
    public String getTokenType(DecodedJWT jwt) {
        return jwt.getClaim("type").asString();
    }

    /**
     * Get the access token expiration duration in seconds.
     */
    public long getAccessExpirationSeconds() {
        return ACCESS_EXPIRATION.getSeconds();
    }

    private Map<String, Object> enrichClaims(Map<String, ?> base, String type) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jti", UUID.randomUUID().toString());
        result.put("type", type);
        if (base != null) {
            base.forEach(result::put);
        }
        return result;
    }

    private String signToken(Map<String, Object> claims, Duration expiration) {
        Instant now = Instant.now();
        JWTCreator.Builder builder = JWT.create()
                .withIssuer(ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(expiration));

        claims.forEach((key, value) -> {
            if (value instanceof Integer)       builder.withClaim(key, (Integer) value);
            else if (value instanceof Long)    builder.withClaim(key, (Long) value);
            else if (value instanceof Boolean) builder.withClaim(key, (Boolean) value);
            else if (value instanceof Double)  builder.withClaim(key, (Double) value);
            else                               builder.withClaim(key, String.valueOf(value));
        });

        return builder.sign(ALGORITHM);
    }

    /**
     * Generate JWT Token
     *
     * @param claims 载荷数据
     * @return 签名的 Token
     * @deprecated Use generateAccessToken() or generateRefreshToken() instead.
     *             This method creates a token with a hardcoded 24h expiration
     *             and no type/jti claim. Retained only for migration compatibility.
     */
    @Deprecated
    public String generateToken(Map<String, ?> claims) {
        Instant now = Instant.now();
        JWTCreator.Builder builder = JWT.create()
                .withIssuer(ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(Duration.ofHours(24)));

        Optional.ofNullable(claims).orElse(Collections.emptyMap()).forEach((key, value) -> {
            if (value instanceof Integer)       builder.withClaim(key, (Integer) value);
            else if (value instanceof Long)     builder.withClaim(key, (Long) value);
            else if (value instanceof Boolean)  builder.withClaim(key, (Boolean) value);
            else if (value instanceof Double)  builder.withClaim(key, (Double) value);
            else                               builder.withClaim(key, String.valueOf(value));
        });

        return builder.sign(ALGORITHM);
    }

    /**
     * 解析并验证 Token
     *
     * @param token 字符串
     * @return DecodedJWT 包装对象，包含所有声明
     * @throws JWTVerificationException 如果校验失败或过期会抛出异常
     */
    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(ALGORITHM)
                    .withIssuer(ISSUER)
                    .build();
            return verifier.verify(token);
        } catch (TokenExpiredException e) {
            log.warn("JWT Token 已过期: {}", e.getMessage());
            throw e;
        } catch (JWTVerificationException e) {
            log.error("JWT Token 签名无效或格式错误: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 安全地获取 Claims
     */
    public Map<String, Claim> getClaims(String token) {
        try {
            return verifyToken(token).getClaims();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
