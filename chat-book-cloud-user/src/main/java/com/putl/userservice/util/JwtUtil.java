package com.putl.userservice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET = "your_secret_key"; // 请替换为你的密钥
    public static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24小时

    /**
     * 生成JWT Token
     *
     * @param claims 自定义声明
     * @return JWT Token
     */
    public static String generateToken(Map<String, Object> claims) {
        Instant expiresAt = LocalDateTime.now().plusHours(24).atZone(ZoneId.of("UTC")).toInstant();
        JWTCreator.Builder builder = JWT.create()
                .withExpiresAt(expiresAt);

        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            builder.withClaim(entry.getKey(), entry.getValue().toString());
        }

        return builder.sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token
     * @return 解析后的声明
     */
    public static Map<String, Claim> parseToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(SECRET))
                    .build()
                    .verify(token);
            return decodedJWT.getClaims();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public static void main(String[] args) {
        // 示例用法
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 12345);
        claims.put("username", "exampleUser");

        String token = generateToken(claims);
        System.out.println("Generated Token: " + token);

        Map<String, Claim> parsedClaims = parseToken(token);
        System.out.println("Parsed Claims: " + parsedClaims);
        System.out.println("User ID: " + parsedClaims.get("userId").asInt());
    }
}
