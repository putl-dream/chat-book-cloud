package fun.amireux.chat.book.auth.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.projectobject.RefreshTokenInfo;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private static final int ADMIN_ROLE_CODE = 1;

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // 接收策略层产出的认证结果，统一进入 Token 签发流程。
    public LoginVO issueTokens(AuthenticatedUser authenticatedUser) {
        return issueTokens(authenticatedUser.userId(), authenticatedUser.userInfo());
    }

    // 统一封装 claims 构建、JWT 签发和 refresh token 持久化逻辑。
    public LoginVO issueTokens(Integer userId, UserInfoDO userInfo) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", userId);

        if (userInfo != null && StringUtils.isNotBlank(userInfo.getUsername())) {
            claims.put("username", userInfo.getUsername());
        }

        claims.put("roles", resolveRoleClaim(userInfo));
        return issueTokens(claims, userId);
    }

    public LoginVO refresh(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new AuthenticationException("refreshToken is required");
        }

        DecodedJWT decoded = verifyRefreshToken(refreshToken);
        String jti = jwtUtil.getJti(decoded);
        RefreshTokenInfo info = refreshTokenService.get(jti);
        if (info == null) {
            throw new AuthenticationException("Refresh token has been revoked or expired");
        }

        // refresh token 采用一次性使用策略，轮换前先撤销旧 token。
        refreshTokenService.delete(jti);

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", info.getUserId());

        String username = decoded.getClaim("username").asString();
        if (StringUtils.isNotBlank(username) && !"null".equals(username)) {
            claims.put("username", username);
        }

        String roles = decoded.getClaim("roles").asString();
        if (StringUtils.isNotBlank(roles)) {
            claims.put("roles", roles);
        }

        return issueTokens(claims, info.getUserId());
    }

    public void revoke(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            return;
        }

        try {
            DecodedJWT decoded = verifyRefreshToken(refreshToken);
            refreshTokenService.delete(jwtUtil.getJti(decoded));
        } catch (Exception ignored) {
            // 登出只做尽力撤销，不把无效 token 当作业务错误抛出。
        }
    }

    private DecodedJWT verifyRefreshToken(String refreshToken) {
        DecodedJWT decoded = jwtUtil.verifyToken(refreshToken);
        if (!JwtUtil.TOKEN_TYPE_REFRESH.equals(jwtUtil.getTokenType(decoded))) {
            throw new AuthenticationException("Invalid token type: expected refresh token");
        }
        return decoded;
    }

    private LoginVO issueTokens(Map<String, Object> claims, Integer userId) {
        String accessToken = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        DecodedJWT refreshJwt = jwtUtil.verifyToken(refreshToken);
        Instant refreshExpiresAt = refreshJwt.getExpiresAt().toInstant();
        RefreshTokenInfo info = new RefreshTokenInfo(
                userId,
                Instant.now(),
                refreshExpiresAt,
                null
        );
        // 仅持久化 refresh token 元数据，access token 保持无状态。
        refreshTokenService.store(jwtUtil.getJti(refreshJwt), info, refreshExpiresAt);

        return new LoginVO(accessToken, refreshToken, jwtUtil.getAccessExpirationSeconds());
    }

    private String resolveRoleClaim(UserInfoDO userInfo) {
        if (userInfo != null && Integer.valueOf(ADMIN_ROLE_CODE).equals(userInfo.getRole())) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }
}
