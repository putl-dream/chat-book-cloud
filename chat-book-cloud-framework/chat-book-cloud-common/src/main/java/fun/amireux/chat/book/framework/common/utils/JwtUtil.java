package fun.amireux.chat.book.framework.common.utils;

import cn.hutool.json.JSONUtil;
import fun.amireux.chat.book.framework.common.pojo.LoginRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "PXrQbuCwXwOZzkML/Vm2S5rSwt1iybvmKtGDzVEu+Hc="; // 换成你自己的密钥
    private static final long EXPIRATION = 1000 * 60 * 60; // 1小时

    public static String generateToken(LoginRequest loginRequest) {
        return Jwts.builder()
                .setSubject(JSONUtil.toJsonStr(loginRequest))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    public static void main(String[] args) {
        String string = generateToken(new LoginRequest("zhangsna", "lisi", "RULE_ADMIN"));
        System.out.println("string = " + string);
    }
}
