package fun.amireux.chat.book.auth.utils;

import fun.amireux.chat.book.auth.request.AuthTokenRequest;
import fun.amireux.chat.book.framework.mvc.security.config.AuthConfiguration;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    @Resource
    AuthConfiguration authConfiguration;

    private long EXPIRATION; // 1小时
    private long REFRESH_EXPIRATION; // 7天
    private SecretKey SECRET_KEY;

    @PostConstruct
    public void init() {
        EXPIRATION = authConfiguration.getJWT_ACCESS_EXPIRATION();
        REFRESH_EXPIRATION = authConfiguration.getJWT_REFRESH_EXPIRATION();
        SECRET_KEY = Keys.hmacShaKeyFor(authConfiguration.getJWT_SECRET().getBytes());
    }

    // 生成访问Token
    public String generateAccessToken(AuthTokenRequest AuthTokenRequest) {
        return generateToken(AuthTokenRequest, EXPIRATION);
    }

    // 生成刷新Token
    public String generateRefreshToken(AuthTokenRequest AuthTokenRequest) {
        return generateToken(AuthTokenRequest, REFRESH_EXPIRATION);
    }

    // 从Token中获取用户名
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // 从Token中获取角色
    public List<String> getRoleFromToken(String token) {
        return parseClaims(token).get("role", List.class);
    }

    // 检查Token是否过期
    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    // 从Token中获取Claims
    public Claims parseClaims(String token) {
        return parseToken(token).getBody();
    }

    // 验证Token
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 解析Token
    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
    }

    private String generateToken(AuthTokenRequest AuthTokenRequest, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", AuthTokenRequest.roles());
        claims.put("type", expiration == EXPIRATION ? "access" : "refresh");

        return Jwts.builder()
                .setSubject(AuthTokenRequest.userId())
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}