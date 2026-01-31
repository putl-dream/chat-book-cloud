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
import java.util.Map;
import java.util.Optional;

@Slf4j
public class JwtUtil {

    //    private static final String SECRET = "your_secure_random_secret_key";
    //    private static final String ISSUER = "amireux-chat-service";
    //    private static final Duration EXPIRATION = Duration.ofHours(24);
    //    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    private final String ISSUER;
    private final Duration EXPIRATION;
    private final Algorithm ALGORITHM;

    public JwtUtil(String SECRET, String ISSUER) {
        this.ISSUER = ISSUER;
        EXPIRATION = Duration.ofHours(24);
        ALGORITHM = Algorithm.HMAC256(SECRET);
    }

    /**
     * 生成 JWT Token
     *
     * @param claims 载荷数据
     * @return 签名的 Token
     */
    public String generateToken(Map<String, ?> claims) {
        Instant now = Instant.now();
        JWTCreator.Builder builder = JWT.create()
                .withIssuer(ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(EXPIRATION));

        // 智能处理 Claim 类型
        Optional.ofNullable(claims).orElse(Collections.emptyMap()).forEach((key, value) -> {
            if (value instanceof Integer) builder.withClaim(key, (Integer) value);
            else if (value instanceof Long) builder.withClaim(key, (Long) value);
            else if (value instanceof Boolean) builder.withClaim(key, (Boolean) value);
            else if (value instanceof Double) builder.withClaim(key, (Double) value);
            else builder.withClaim(key, String.valueOf(value));
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