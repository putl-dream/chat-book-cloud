package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.client.engine.StreamingControl;
import com.putl.agentservice.config.AgentChatProperties;
import com.putl.agentservice.constants.AgentMessageTypeConstants;
import com.putl.agentservice.constants.AgentStreamEventConstants;
import com.putl.agentservice.enums.AgentAssistantAction;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.DraftReadiness;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.dto.InteractionAnswerRequest;
import com.putl.agentservice.model.dto.InteractionResponseRequest;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.AgentChatResponse;
import com.putl.agentservice.model.vo.InteractiveFormPayload;
import com.putl.agentservice.model.vo.InteractiveOption;
import com.putl.agentservice.model.vo.InteractiveQuestion;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.model.vo.SceneDecision;
import com.putl.agentservice.service.AgentConversationWindowService;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.agentservice.service.AgentNotebookService;
import com.putl.articleservice.api.ArticleClient;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentConversationServiceImplTest {

    @Mock
    private AgentMessageMapper agentMessageMapper;

    @Mock
    private AgentSessionMapper agentSessionMapper;

    @Mock
    private ArticleAiGateway articleAiGateway;

    @Mock
    private AgentConversationWindowService agentConversationWindowService;

    @Mock
    private AgentNotebookCacheService agentNotebookCacheService;

    @Mock
    private AgentNotebookService agentNotebookService;

    @Mock
    private AgentSceneRouter agentSceneRouter;

    @Mock
    private ArticleClient articleClient;

    @Mock
    private AgentChatProperties agentChatProperties;

    @Mock
    private MessagePublisher messagePublisher;

    private AgentConversationServiceImpl service;

    @BeforeEach
    void setUp() {
        Executor directExecutor = Runnable::run;
        service = new AgentConversationServiceImpl(
                agentMessageMapper,
                agentSessionMapper,
                articleAiGateway,
                agentConversationWindowService,
                agentNotebookCacheService,
                agentNotebookService,
                agentSceneRouter,
                articleClient,
                agentChatProperties,
                messagePublisher,
                directExecutor);

        when(agentConversationWindowService.getRecentMessages(anyInt())).thenReturn(new ArrayList<AgentMessageDO>());
        when(agentConversationWindowService.appendMessage(anyInt(), anyList(), any(AgentMessageDO.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<AgentMessageDO> existingWindow = (List<AgentMessageDO>) invocation.getArgument(1);
            List<AgentMessageDO> current = new ArrayList<>(existingWindow);
            current.add(invocation.getArgument(2, AgentMessageDO.class));
            return current;
        });
        when(agentNotebookCacheService.getNotebook(anyInt())).thenReturn(NotebookSummary.builder().build());
        when(agentNotebookService.refreshNotebook(anyInt())).thenReturn(NotebookSummary.builder().build());
        when(agentSessionMapper.selectById(anyInt())).thenReturn(AgentSessionDO.builder()
                .id(101)
                .sceneType(AgentSceneType.DISCUSS)
                .build());
        when(agentSceneRouter.route(any(), anyList(), any(NotebookSummary.class), anyBoolean())).thenReturn(SceneDecision.builder()
                .currentScene(AgentSceneType.DISCUSS)
                .nextScene(AgentSceneType.DRAFT)
                .switchReason("route")
                .assistantAction(AgentAssistantAction.SUGGEST_DRAFT)
                .draftReadiness(DraftReadiness.READY)
                .build());
        when(agentSceneRouter.applyDecision(any(NotebookSummary.class), any(SceneDecision.class), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(agentSceneRouter.finalizeDecision(any(SceneDecision.class), any(NotebookSummary.class), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void chatShouldPersistStructuredInteractionResponseAndInteractiveFormReply() {
        AgentChatRequest request = new AgentChatRequest();
        request.setSessionId(101);
        request.setInteractionResponse(buildInteractionResponse());

        AiInvocationResult<AgentAssistantMessage> aiReply = new AiInvocationResult<>(
                AgentAssistantMessage.builder()
                        .messageType(AgentMessageTypeConstants.INTERACTIVE_FORM)
                        .content("我还需要确认最后一个重点")
                        .payload(InteractiveFormPayload.builder()
                                .formId("followup_form")
                                .title("补充一个细节")
                                .description("再确认最后一题")
                                .submitMode("batch")
                                .questions(List.of(
                                        InteractiveQuestion.builder()
                                                .id("depth")
                                                .label("希望解读深度到什么程度？")
                                                .type("single_choice")
                                                .required(true)
                                                .options(List.of(
                                                        InteractiveOption.builder().label("全面解读").value("全面解读").build()
                                                ))
                                                .build()
                                ))
                                .build())
                        .build(),
                12,
                34,
                56,
                "claude-test");
        when(articleAiGateway.chat(anyList(), any(NotebookSummary.class), any(AgentSceneType.class), any(), any(), any(), any(), any(StreamingControl.class)))
                .thenReturn(aiReply);

        AgentChatResponse response = service.chat(request);

        ArgumentCaptor<AgentMessageDO> captor = ArgumentCaptor.forClass(AgentMessageDO.class);
        verify(agentMessageMapper, times(2)).insert(captor.capture());
        List<AgentMessageDO> savedMessages = captor.getAllValues();

        AgentMessageDO userMessage = savedMessages.get(0);
        assertEquals(AgentMessageRole.USER, userMessage.getRole());
        assertEquals(AgentMessageTypeConstants.TEXT, userMessage.getMessageType());
        assertTrue(userMessage.getContent().contains("[STRUCTURED_FORM_RESPONSE]"));
        assertTrue(userMessage.getContent().contains("目标读者"));
        assertTrue(userMessage.getPayload().contains("\"formId\":\"brief_form\""));

        AgentMessageDO assistantMessage = savedMessages.get(1);
        assertEquals(AgentMessageRole.ASSISTANT, assistantMessage.getRole());
        assertEquals(AgentMessageTypeConstants.INTERACTIVE_FORM, assistantMessage.getMessageType());
        assertTrue(assistantMessage.getPayload().contains("\"formId\":\"followup_form\""));

        assertEquals(AgentMessageTypeConstants.INTERACTIVE_FORM, response.getMessage().getMessageType());
        assertTrue(response.getMessage().getPayload().toString().contains("\"formId\":\"followup_form\""));
        assertEquals(AgentSceneType.DISCUSS, response.getCurrentScene());
        assertEquals(AgentSceneType.DRAFT, response.getNextScene());
        assertEquals(DraftReadiness.READY, response.getDraftReadiness());
    }

    @Test
    void chatByWebSocketShouldPublishRuntimeEvents() {
        AgentChatRequest request = new AgentChatRequest();
        request.setSessionId(101);
        request.setContent("帮我整理一个选题");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> consumer = invocation.getArgument(6, java.util.function.Consumer.class);
            consumer.accept("<agent_preview>先");
            consumer.accept("看受众</agent_preview><agent_final>{\"messageType\":\"text\",\"content\":\"先看受众\",\"payload\":null}</agent_final>");
            return new AiInvocationResult<>(
                    AgentAssistantMessage.builder()
                            .messageType(AgentMessageTypeConstants.TEXT)
                            .content("先看受众")
                            .build(),
                    10,
                    20,
                    30,
                    "claude-test");
        }).when(articleAiGateway).chat(anyList(), any(NotebookSummary.class), any(AgentSceneType.class), any(), any(), any(), any(), any(StreamingControl.class));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        service.chatByWebSocket("u-1", request);

        verify(messagePublisher, times(4)).sendToUser(anyString(), payloadCaptor.capture());
        List<String> payloads = payloadCaptor.getAllValues().stream().map(String::valueOf).toList();

        assertEquals(4, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains(AgentStreamEventConstants.MESSAGE_STARTED));
        Assertions.assertTrue(payloads.get(0).contains("\"statusText\":\"正在思考...\""));
        Assertions.assertTrue(payloads.get(1).contains(AgentStreamEventConstants.MESSAGE_DELTA));
        Assertions.assertTrue(payloads.get(1).contains("\"delta\":\"先\""));
        Assertions.assertTrue(payloads.get(2).contains(AgentStreamEventConstants.MESSAGE_DELTA));
        Assertions.assertTrue(payloads.get(2).contains("\"delta\":\"看受众\""));
        Assertions.assertTrue(payloads.get(3).contains(AgentStreamEventConstants.MESSAGE_COMPLETED));
        Assertions.assertTrue(payloads.get(3).contains("\"finalMessage\""));
        Assertions.assertTrue(payloads.get(3).contains("\"previewText\":\"先看受众\""));
        Assertions.assertTrue(payloads.get(3).contains("\"previewMode\":\"tagged_preview\""));
    }

    @Test
    void chatByWebSocketShouldStreamRawTextWhenEnvelopeIsMissing() {
        AgentChatRequest request = new AgentChatRequest();
        request.setSessionId(101);
        request.setContent("帮我整理一个选题");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> consumer = invocation.getArgument(6, java.util.function.Consumer.class);
            consumer.accept("先");
            consumer.accept("看受众");
            return new AiInvocationResult<>(
                    AgentAssistantMessage.builder()
                            .messageType(AgentMessageTypeConstants.TEXT)
                            .content("先看受众")
                            .build(),
                    10,
                    20,
                    30,
                    "claude-test");
        }).when(articleAiGateway).chat(anyList(), any(NotebookSummary.class), any(AgentSceneType.class), any(), any(), any(), any(), any(StreamingControl.class));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        service.chatByWebSocket("u-1", request);

        verify(messagePublisher, times(4)).sendToUser(anyString(), payloadCaptor.capture());
        List<String> payloads = payloadCaptor.getAllValues().stream().map(String::valueOf).toList();

        assertEquals(4, payloads.size());
        Assertions.assertTrue(payloads.get(1).contains("\"delta\":\"先\""));
        Assertions.assertTrue(payloads.get(2).contains("\"delta\":\"看受众\""));
        Assertions.assertTrue(payloads.get(3).contains("\"previewText\":\"先看受众\""));
        Assertions.assertTrue(payloads.get(3).contains("\"previewMode\":\"raw_text\""));
    }

    @Test
    void chatByWebSocketShouldEmitCompletionFallbackDeltaWhenOnlyFinalEnvelopeExists() {
        AgentChatRequest request = new AgentChatRequest();
        request.setSessionId(101);
        request.setContent("帮我整理一个选题");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> consumer = invocation.getArgument(6, java.util.function.Consumer.class);
            consumer.accept("<agent_final>{\"messageType\":\"text\",\"content\":\"先看受众\"");
            consumer.accept(",\"payload\":null}</agent_final>");
            return new AiInvocationResult<>(
                    AgentAssistantMessage.builder()
                            .messageType(AgentMessageTypeConstants.TEXT)
                            .content("先看受众")
                            .build(),
                    10,
                    20,
                    30,
                    "claude-test");
        }).when(articleAiGateway).chat(anyList(), any(NotebookSummary.class), any(AgentSceneType.class), any(), any(), any(), any(), any(StreamingControl.class));

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        service.chatByWebSocket("u-1", request);

        verify(messagePublisher, times(3)).sendToUser(anyString(), payloadCaptor.capture());
        List<String> payloads = payloadCaptor.getAllValues().stream().map(String::valueOf).toList();

        assertEquals(3, payloads.size());
        Assertions.assertTrue(payloads.get(1).contains(AgentStreamEventConstants.MESSAGE_DELTA));
        Assertions.assertTrue(payloads.get(1).contains("\"delta\":\"先看受众\""));
        Assertions.assertTrue(payloads.get(2).contains("\"previewText\":\"先看受众\""));
        Assertions.assertTrue(payloads.get(2).contains("\"previewMode\":\"completion_fallback\""));
    }

    @Test
    void chatShouldFallbackToPreviewWhenFinalTextIsShorterThanPreview() {
        AgentChatRequest request = new AgentChatRequest();
        request.setSessionId(101);
        request.setContent("帮我整理一个选题");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> consumer = invocation.getArgument(6, java.util.function.Consumer.class);
            consumer.accept("<agent_preview>先看受众</agent_preview>");
            return new AiInvocationResult<>(
                    AgentAssistantMessage.builder()
                            .messageType(AgentMessageTypeConstants.TEXT)
                            .content("先看")
                            .build(),
                    10,
                    20,
                    30,
                    "claude-test");
        }).when(articleAiGateway).chat(anyList(), any(NotebookSummary.class), any(AgentSceneType.class), any(), any(), any(), any(), any(StreamingControl.class));

        AgentChatResponse response = service.chat(request);

        ArgumentCaptor<AgentMessageDO> captor = ArgumentCaptor.forClass(AgentMessageDO.class);
        verify(agentMessageMapper, times(2)).insert(captor.capture());
        List<AgentMessageDO> savedMessages = captor.getAllValues();
        AgentMessageDO assistantMessage = savedMessages.get(1);

        assertEquals("先看受众", assistantMessage.getContent());
        assertEquals("先看受众", response.getReply());
    }

    private InteractionResponseRequest buildInteractionResponse() {
        InteractionAnswerRequest audience = new InteractionAnswerRequest();
        audience.setQuestionId("audience");
        audience.setQuestionLabel("目标读者");
        audience.setQuestionType("single_choice");
        audience.setValue("Java 开发者");

        InteractionAnswerRequest format = new InteractionAnswerRequest();
        format.setQuestionId("format");
        format.setQuestionLabel("内容形式");
        format.setQuestionType("single_choice");
        format.setValue("技术博客文章");

        InteractionResponseRequest response = new InteractionResponseRequest();
        response.setFormId("brief_form");
        response.setTitle("补充基础信息");
        response.setDescription("回答后我会继续完善文章建议");
        response.setAnswers(List.of(audience, format));
        return response;
    }
}
