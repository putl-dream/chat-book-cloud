package com.putl.articleservice.controller;

import com.putl.articleservice.constants.McpScopeConstants;
import com.putl.articleservice.controller.dto.McpCreateDraftRequest;
import com.putl.articleservice.controller.vo.McpCreateDraftResponse;
import com.putl.articleservice.exception.McpAuthenticationException;
import com.putl.articleservice.exception.McpAuthorizationException;
import com.putl.articleservice.model.McpPrincipal;
import com.putl.articleservice.service.McpDraftService;
import com.putl.articleservice.service.McpTokenService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MCP 内容入口")
@RestController
@RequestMapping("/mcp/drafts")
@RequiredArgsConstructor
public class McpDraftController {

    private final McpTokenService mcpTokenService;
    private final McpDraftService mcpDraftService;

    @Operation(summary = "通过 MCP 创建文章草稿")
    @PostMapping
    public CommonResult<McpCreateDraftResponse> createDraft(@RequestBody McpCreateDraftRequest request,
                                                            HttpServletRequest servletRequest) {
        try {
            McpPrincipal principal = mcpTokenService.authenticate(
                    servletRequest.getHeader("Authorization"),
                    McpScopeConstants.ARTICLE_DRAFT_CREATE);
            return CommonResult.success(mcpDraftService.createDraft(principal, request));
        } catch (McpAuthenticationException ex) {
            return CommonResult.error(401, ex.getMessage());
        } catch (McpAuthorizationException ex) {
            return CommonResult.error(403, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return CommonResult.error(400, ex.getMessage());
        }
    }
}
