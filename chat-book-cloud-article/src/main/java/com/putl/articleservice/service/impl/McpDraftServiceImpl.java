package com.putl.articleservice.service.impl;

import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.controller.dto.McpCreateDraftRequest;
import com.putl.articleservice.controller.vo.McpCreateDraftResponse;
import com.putl.articleservice.model.McpPrincipal;
import com.putl.articleservice.service.DraftService;
import com.putl.articleservice.service.McpDraftService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class McpDraftServiceImpl implements McpDraftService {

    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_SUMMARY_LENGTH = 5_000;
    private static final int MAX_CONTENT_LENGTH = 100_000;
    private static final int MAX_INSTRUCTION_LENGTH = 1_000;
    private static final String SOURCE_TYPE = "MCP_CREATE";
    private static final String DEFAULT_INSTRUCTION = "Imported from Codex MCP";

    private final DraftService draftService;

    public McpDraftServiceImpl(DraftService draftService) {
        this.draftService = draftService;
    }

    @Override
    public McpCreateDraftResponse createDraft(McpPrincipal principal, McpCreateDraftRequest request) {
        if (principal == null || principal.getUserId() == null || principal.getUserId() <= 0) {
            throw new IllegalArgumentException("MCP token 未绑定有效用户");
        }
        if (request == null) {
            throw new IllegalArgumentException("创建草稿请求不能为空");
        }

        String title = normalizeRequired(request.getTitle(), "标题不能为空", MAX_TITLE_LENGTH, "标题不能超过 255 个字符");
        String content = normalizeRequired(request.getContent(), "正文不能为空", MAX_CONTENT_LENGTH, "正文不能超过 100000 个字符");
        String summary = normalizeOptional(request.getSummary(), MAX_SUMMARY_LENGTH, "摘要不能超过 5000 个字符");
        String instruction = normalizeOptional(request.getInstruction(), MAX_INSTRUCTION_LENGTH, "来源说明不能超过 1000 个字符");

        CreateDraftRequest draftRequest = new CreateDraftRequest();
        draftRequest.setUserId(principal.getUserId());
        draftRequest.setTitle(title);
        draftRequest.setSummary(summary);
        draftRequest.setContent(content);
        draftRequest.setSourceType(SOURCE_TYPE);
        draftRequest.setInstruction(StringUtils.hasText(instruction) ? instruction : DEFAULT_INSTRUCTION);

        CreateDraftResponse response = draftService.createDraft(draftRequest);
        return McpCreateDraftResponse.builder()
                .draftId(response.getDraftId())
                .versionNo(response.getVersionNo())
                .status("DRAFT")
                .build();
    }

    private String normalizeRequired(String value, String blankMessage, int maxLength, String tooLongMessage) {
        String normalized = normalizeOptional(value, maxLength, tooLongMessage);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException(blankMessage);
        }
        return normalized;
    }

    private String normalizeOptional(String value, int maxLength, String tooLongMessage) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(tooLongMessage);
        }
        return normalized;
    }
}
