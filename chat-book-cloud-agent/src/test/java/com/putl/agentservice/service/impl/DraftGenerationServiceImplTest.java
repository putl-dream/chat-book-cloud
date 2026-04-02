package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.dto.GenerateDraftRequest;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftGenerationServiceImplTest {

    @Mock
    private AgentSessionMapper agentSessionMapper;

    @Mock
    private AgentMessageMapper agentMessageMapper;

    @Mock
    private ArticleAiGateway articleAiGateway;

    @Mock
    private ArticleClient articleClient;

    @Mock
    private AgentNotebookCacheService agentNotebookCacheService;

    @Mock
    private MessagePublisher messagePublisher;

    private DraftGenerationServiceImpl service;

    @BeforeEach
    void setUp() {
        Executor directExecutor = Runnable::run;
        service = new DraftGenerationServiceImpl(
                agentSessionMapper,
                agentMessageMapper,
                articleAiGateway,
                articleClient,
                agentNotebookCacheService,
                messagePublisher,
                directExecutor);

        when(agentSessionMapper.selectById(anyInt())).thenReturn(AgentSessionDO.builder()
                .id(11)
                .userId(1)
                .build());
        when(agentMessageMapper.selectList(any())).thenReturn(List.of(
                AgentMessageDO.builder().id(1).sessionId(11).content("请帮我生成一篇文章").build()
        ));
        when(agentNotebookCacheService.getNotebook(anyInt())).thenReturn(NotebookSummary.builder().build());
    }

    @Test
    void generateDraftByWebSocketShouldStreamAndPublishDoneEvent() {
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<String> chunkConsumer = invocation.getArgument(2, Consumer.class);
            chunkConsumer.accept("{\"title\":\"流式标题\"");
            chunkConsumer.accept(",\"content\":\"第一段\"}");
            return new AiInvocationResult<>(
                    ArticleDraftResult.builder()
                            .title("流式标题")
                            .summary(null)
                            .content("第一段")
                            .build(),
                    12,
                    34,
                    56,
                    "MiniMax-M2.7");
        }).when(articleAiGateway).generateDraft(anyList(), any(NotebookSummary.class), any());
        when(articleClient.createDraft(any())).thenReturn(CommonResult.success(CreateDraftResponse.builder()
                .draftId(101)
                .versionNo(1)
                .build()));

        GenerateDraftRequest request = new GenerateDraftRequest();
        request.setSessionId(11);
        service.generateDraftByWebSocket("1", request);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagePublisher, atLeastOnce()).sendToUser(org.mockito.ArgumentMatchers.eq("1"), payloadCaptor.capture());

        List<Object> allPayloads = payloadCaptor.getAllValues();
        assertTrue(allPayloads.stream().map(String::valueOf).anyMatch(payload -> payload.contains("\"type\":\"AGENT_DRAFT_GENERATE_START\"")));
        assertTrue(allPayloads.stream().map(String::valueOf).anyMatch(payload -> payload.contains("\"type\":\"AGENT_DRAFT_GENERATE_DELTA\"")));
        assertTrue(allPayloads.stream().map(String::valueOf).anyMatch(payload -> payload.contains("\"type\":\"AGENT_DRAFT_GENERATE_DONE\"")));
        assertTrue(allPayloads.stream().map(String::valueOf).anyMatch(payload -> payload.contains("\"draftId\":101")));
        assertTrue(allPayloads.stream().map(String::valueOf).anyMatch(payload -> payload.contains("\"summary\":null")));
    }

    @Test
    void generateDraftByWebSocketShouldPublishErrorEventWhenStreamFails() {
        when(articleAiGateway.generateDraft(anyList(), any(NotebookSummary.class), any()))
                .thenThrow(new IllegalStateException("Stream failed"));

        GenerateDraftRequest request = new GenerateDraftRequest();
        request.setSessionId(11);
        service.generateDraftByWebSocket("1", request);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagePublisher, atLeastOnce()).sendToUser(org.mockito.ArgumentMatchers.eq("1"), payloadCaptor.capture());

        String lastPayload = String.valueOf(payloadCaptor.getAllValues().get(payloadCaptor.getAllValues().size() - 1));
        assertTrue(lastPayload.contains("\"type\":\"AGENT_DRAFT_GENERATE_ERROR\""));
        assertTrue(lastPayload.contains("Stream failed"));
    }

    @Test
    void generateDraftShouldFailFastWhenArticleServiceReturnsEmptyPayload() {
        when(articleAiGateway.generateDraft(anyList(), any(NotebookSummary.class), any())).thenReturn(
                new AiInvocationResult<>(
                        ArticleDraftResult.builder()
                                .title("标题")
                                .summary("摘要")
                                .content("内容")
                                .build(),
                        1,
                        2,
                        3,
                        "MiniMax-M2.7"));
        when(articleClient.createDraft(any())).thenReturn(CommonResult.success(null));

        GenerateDraftRequest request = new GenerateDraftRequest();
        request.setSessionId(11);

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.generateDraft(request));
        assertEquals("文章草稿保存失败", exception.getMessage());
    }
}
