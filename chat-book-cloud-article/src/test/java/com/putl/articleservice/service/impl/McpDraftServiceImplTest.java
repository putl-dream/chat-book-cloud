package com.putl.articleservice.service.impl;

import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.controller.dto.McpCreateDraftRequest;
import com.putl.articleservice.controller.vo.McpCreateDraftResponse;
import com.putl.articleservice.model.McpPrincipal;
import com.putl.articleservice.service.DraftService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpDraftServiceImplTest {

    @Mock
    private DraftService draftService;

    private McpDraftServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new McpDraftServiceImpl(draftService);
    }

    @Test
    void createDraftShouldBindMcpUserAndSourceType() {
        when(draftService.createDraft(any())).thenReturn(CreateDraftResponse.builder()
                .draftId(101)
                .versionNo(1)
                .build());

        McpCreateDraftRequest request = new McpCreateDraftRequest();
        request.setTitle("Codex import");
        request.setSummary("Summary");
        request.setContent("Markdown body");

        McpCreateDraftResponse response = service.createDraft(McpPrincipal.builder()
                .userId(7)
                .tokenId(3)
                .scopes("article:draft:create")
                .build(), request);

        assertThat(response.getDraftId()).isEqualTo(101);
        assertThat(response.getVersionNo()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo("DRAFT");

        ArgumentCaptor<CreateDraftRequest> captor = ArgumentCaptor.forClass(CreateDraftRequest.class);
        verify(draftService).createDraft(captor.capture());
        CreateDraftRequest draftRequest = captor.getValue();
        assertThat(draftRequest.getUserId()).isEqualTo(7);
        assertThat(draftRequest.getSourceType()).isEqualTo("MCP_CREATE");
        assertThat(draftRequest.getInstruction()).isEqualTo("Imported from Codex MCP");
    }

    @Test
    void createDraftShouldRejectBlankContent() {
        McpCreateDraftRequest request = new McpCreateDraftRequest();
        request.setTitle("Codex import");
        request.setContent(" ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createDraft(McpPrincipal.builder().userId(7).build(), request));

        assertThat(exception.getMessage()).isEqualTo("正文不能为空");
    }
}
