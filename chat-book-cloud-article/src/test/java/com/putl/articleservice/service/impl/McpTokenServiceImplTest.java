package com.putl.articleservice.service.impl;

import com.putl.articleservice.exception.McpAuthenticationException;
import com.putl.articleservice.exception.McpAuthorizationException;
import com.putl.articleservice.mapper.McpTokenMapper;
import com.putl.articleservice.mapper.entity.McpTokenDO;
import com.putl.articleservice.model.McpPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpTokenServiceImplTest {

    @Mock
    private McpTokenMapper mcpTokenMapper;

    private McpTokenServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new McpTokenServiceImpl(mcpTokenMapper);
    }

    @Test
    void authenticateShouldReturnPrincipalWhenTokenHasScope() {
        when(mcpTokenMapper.selectOne(any())).thenReturn(McpTokenDO.builder()
                .id(5)
                .userId(7)
                .scopes("article:draft:create")
                .status("ACTIVE")
                .build());

        McpPrincipal principal = service.authenticate("Bearer cbmcp_test", "article:draft:create");

        assertThat(principal.getTokenId()).isEqualTo(5);
        assertThat(principal.getUserId()).isEqualTo(7);
        verify(mcpTokenMapper).updateById(any(McpTokenDO.class));
    }

    @Test
    void authenticateShouldRejectMissingBearerPrefix() {
        McpAuthenticationException exception = assertThrows(McpAuthenticationException.class,
                () -> service.authenticate("cbmcp_test", "article:draft:create"));

        assertThat(exception.getMessage()).isEqualTo("MCP Authorization header 必须使用 Bearer token");
    }

    @Test
    void authenticateShouldRejectMissingScope() {
        when(mcpTokenMapper.selectOne(any())).thenReturn(McpTokenDO.builder()
                .id(5)
                .userId(7)
                .scopes("article:draft:update")
                .status("ACTIVE")
                .build());

        McpAuthorizationException exception = assertThrows(McpAuthorizationException.class,
                () -> service.authenticate("Bearer cbmcp_test", "article:draft:create"));

        assertThat(exception.getMessage()).isEqualTo("MCP token 缺少权限: article:draft:create");
    }
}
