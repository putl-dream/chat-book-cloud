package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.articleservice.exception.McpAuthenticationException;
import com.putl.articleservice.exception.McpAuthorizationException;
import com.putl.articleservice.mapper.McpTokenMapper;
import com.putl.articleservice.mapper.entity.McpTokenDO;
import com.putl.articleservice.model.McpPrincipal;
import com.putl.articleservice.service.McpTokenService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
public class McpTokenServiceImpl implements McpTokenService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final McpTokenMapper mcpTokenMapper;

    public McpTokenServiceImpl(McpTokenMapper mcpTokenMapper) {
        this.mcpTokenMapper = mcpTokenMapper;
    }

    @Override
    public McpPrincipal authenticate(String authorizationHeader, String requiredScope) {
        String token = extractBearerToken(authorizationHeader);
        String tokenHash = sha256Hex(token);
        McpTokenDO tokenDO = mcpTokenMapper.selectOne(Wrappers.<McpTokenDO>lambdaQuery()
                .eq(McpTokenDO::getTokenHash, tokenHash)
                .eq(McpTokenDO::getStatus, ACTIVE_STATUS)
                .last("limit 1"));
        if (tokenDO == null) {
            throw new McpAuthenticationException("MCP token 无效或已停用");
        }
        McpPrincipal principal = McpPrincipal.builder()
                .tokenId(tokenDO.getId())
                .userId(tokenDO.getUserId())
                .scopes(tokenDO.getScopes())
                .build();
        if (!principal.hasScope(requiredScope)) {
            throw new McpAuthorizationException("MCP token 缺少权限: " + requiredScope);
        }
        mcpTokenMapper.updateById(McpTokenDO.builder()
                .id(tokenDO.getId())
                .lastUsedTime(LocalDateTime.now())
                .build());
        return principal;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new McpAuthenticationException("缺少 MCP Authorization header");
        }
        String token = authorizationHeader.trim();
        if (!token.startsWith(BEARER_PREFIX)) {
            throw new McpAuthenticationException("MCP Authorization header 必须使用 Bearer token");
        }
        token = token.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new McpAuthenticationException("MCP token 不能为空");
        }
        return token;
    }

    private String sha256Hex(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前 JVM 不支持 SHA-256", ex);
        }
    }
}
