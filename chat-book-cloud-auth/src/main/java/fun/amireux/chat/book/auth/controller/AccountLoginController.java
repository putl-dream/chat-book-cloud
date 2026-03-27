package fun.amireux.chat.book.auth.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import fun.amireux.chat.book.auth.controller.dto.RefreshRequest;
import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.projectobject.RefreshTokenInfo;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.RefreshTokenService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/account")
@RequiredArgsConstructor
public class AccountLoginController {
    private final UserService userService;
    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // =============================================
    //  Existing endpoints — updated return types
    // =============================================

    @PostMapping("/login")
    public CommonResult<LoginVO> login(@RequestBody UserDTO user) {
        return CommonResult.success(userService.login(user));
    }

    @PostMapping("/registered")
    public CommonResult<LoginVO> registered(@RequestBody UserDTO user) {
        user.setLoginMethod(LoginMethod.REGISTER);
        return CommonResult.success(userService.signIn(user));
    }

    @GetMapping("/captcha")
    public CommonResult<String> captcha(@RequestParam String email) {
        captchaService.sendCaptcha(email);
        return CommonResult.success("验证码已发送");
    }

    // =============================================
    //  NEW: Refresh endpoint
    // =============================================

    @PostMapping("/refresh")
    public CommonResult<LoginVO> refresh(@RequestBody RefreshRequest request) {
        String token = request.getRefreshToken();
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("refreshToken is required");
        }

        // Step 1: Verify JWT signature and expiry
        DecodedJWT decoded = jwtUtil.verifyToken(token);

        // Step 2: Verify type == refresh
        if (!JwtUtil.TOKEN_TYPE_REFRESH.equals(jwtUtil.getTokenType(decoded))) {
            throw new AuthenticationException("Invalid token type: expected refresh token");
        }

        // Step 3: Verify exists in Redis (not revoked)
        String jti = jwtUtil.getJti(decoded);
        RefreshTokenInfo info = refreshTokenService.get(jti);
        if (info == null) {
            throw new AuthenticationException("Refresh token has been revoked or expired");
        }

        // Step 4: Revoke old refresh token (rotation — one-time use)
        refreshTokenService.delete(jti);

        // Step 5: Build new token pair
        LoginVO newTokens = buildTokensFromRefreshClaims(decoded, info);

        return CommonResult.success(newTokens);
    }

    // =============================================
    //  NEW: Logout endpoint
    // =============================================

    @PostMapping("/logout")
    public CommonResult<Void> logout(@RequestBody RefreshRequest request) {
        String token = request.getRefreshToken();
        if (token != null && !token.isBlank()) {
            try {
                DecodedJWT decoded = jwtUtil.verifyToken(token);
                if (JwtUtil.TOKEN_TYPE_REFRESH.equals(jwtUtil.getTokenType(decoded))) {
                    refreshTokenService.delete(jwtUtil.getJti(decoded));
                }
            } catch (Exception e) {
                // Best-effort revocation — ignore verification errors
            }
        }
        return CommonResult.success();
    }

    // ---- private helpers ----

    private LoginVO buildTokensFromRefreshClaims(DecodedJWT decoded, RefreshTokenInfo info) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", info.getUserId());
        String username = decoded.getClaim("username").asString();
        if (username != null && !"null".equals(username)) {
            claims.put("username", username);
        }
        String roles = decoded.getClaim("roles").asString();
        if (roles != null) {
            claims.put("roles", roles);
        }

        String newAccess  = jwtUtil.generateAccessToken(claims);
        String newRefresh = jwtUtil.generateRefreshToken(claims);

        DecodedJWT newRefreshDecoded = jwtUtil.verifyToken(newRefresh);
        String newJti = jwtUtil.getJti(newRefreshDecoded);
        long refreshExpSeconds = newRefreshDecoded.getExpiresAt().getTime() / 1000;

        RefreshTokenInfo newInfo = new RefreshTokenInfo(
                info.getUserId(), Instant.now(), Instant.ofEpochSecond(refreshExpSeconds), null
        );
        refreshTokenService.store(newJti, newInfo, refreshExpSeconds);

        return new LoginVO(newAccess, newRefresh, jwtUtil.getAccessExpirationSeconds());
    }
}
