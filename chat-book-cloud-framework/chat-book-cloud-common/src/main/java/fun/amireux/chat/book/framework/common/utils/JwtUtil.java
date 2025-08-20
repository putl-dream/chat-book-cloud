package fun.amireux.chat.book.framework.common.utils;

import fun.amireux.chat.book.framework.common.pojo.LoginRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "PXrQbuCwXwOZzkML/Vm2S5rSwt1iybvmKtGDzVEu+Hc="; // 换成你自己的密钥
    private static final long EXPIRATION = 1000 * 60 * 60; // 1小时


    // 生成 SecretKey 实例用于签名
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(LoginRequest loginRequest) {
        return Jwts.builder()
                .setSubject(loginRequest.getUsername())
                .claim("role", loginRequest.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static void main(String[] args) {
        String string = generateToken(new LoginRequest("zhangsna", "lisi", "RULE_ADMIN"));
        System.out.println("string = " + string);
        System.out.println("parseToken(string).getSubject() = " + parseToken(string).getSubject());
    }
}
