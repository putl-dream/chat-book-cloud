package com.putl.agentservice.client;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageDeltaUsage;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.RawContentBlockDelta;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.AgentMessageTypeConstants;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.InteractiveFormPayload;
import com.putl.agentservice.model.vo.InteractiveOption;
import com.putl.agentservice.model.vo.InteractiveQuestion;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.prompt.PromptTemplateLoader;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AnthropicArticleAiGateway implements ArticleAiGateway {

    private final AnthropicClient anthropicClient;
    private final AnthropicProperties properties;
    private final PromptTemplateLoader promptTemplateLoader;

    public AnthropicArticleAiGateway(AnthropicClient anthropicClient,
                                     AnthropicProperties properties,
                                     PromptTemplateLoader promptTemplateLoader) {
        this.anthropicClient = anthropicClient;
        this.properties = properties;
        this.promptTemplateLoader = promptTemplateLoader;
    }

    @Override
    public AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return invokeForStructuredChat(buildChatParams(messages, notebookSummary));
    }

    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return generateDraft(messages, notebookSummary, null);
    }

    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages,
                                                                NotebookSummary notebookSummary,
                                                                Consumer<String> chunkConsumer) {
        MessageCreateParams params = baseRequest(
                properties.getAnthropic().getModel().getGenerate(),
                properties.getAnthropic().getMaxTokens().getGenerate(),
                0.2,
                promptTemplateLoader.load(PromptTemplateConstants.ARTICLE_GENERATE))
                .addUserMessage(buildGeneratePrompt(messages, notebookSummary))
                .build();
        return invokeForJson(params, ArticleDraftResult.class, chunkConsumer);
    }

    @Override
    public AiInvocationResult<ArticleDraftResult> optimizeDraft(String instruction,
                                                                String currentTitle,
                                                                String currentSummary,
                                                                String currentContent) {
        MessageCreateParams params = baseRequest(
                properties.getAnthropic().getModel().getOptimize(),
                properties.getAnthropic().getMaxTokens().getOptimize(),
                0.2,
                promptTemplateLoader.load(PromptTemplateConstants.ARTICLE_OPTIMIZE))
                .addUserMessage(buildOptimizePrompt(instruction, currentTitle, currentSummary, currentContent))
                .build();
        return invokeForJson(params, ArticleDraftResult.class);
    }

    @Override
    public AiInvocationResult<NotebookSummary> summarizeNotebook(List<AgentMessageDO> messages, NotebookSummary currentNotebook) {
        MessageCreateParams params = baseRequest(
                properties.getAnthropic().getModel().getNotebook(),
                properties.getAnthropic().getMaxTokens().getNotebook(),
                0.1,
                promptTemplateLoader.load(PromptTemplateConstants.NOTEBOOK_SUMMARIZE))
                .addUserMessage(buildNotebookPrompt(messages, currentNotebook))
                .build();
        return invokeForJson(params, NotebookSummary.class);
    }

    private MessageCreateParams.Builder baseRequest(String model, Integer maxTokens, double temperature, String systemPrompt) {
        validateApiKeyConfigured();
        return MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens.longValue())
                .temperature(temperature)
                .system(systemPrompt);
    }

    private MessageCreateParams buildChatParams(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        MessageCreateParams.Builder builder = baseRequest(
                properties.getAnthropic().getModel().getChat(),
                properties.getAnthropic().getMaxTokens().getChat(),
                0.2,
                renderTemplate(PromptTemplateConstants.ARTICLE_CHAT, Map.of(
                        "notebook_json", prettyJson(normalizeNotebook(notebookSummary))
                )));
        toAnthropicMessages(messages).forEach(builder::addMessage);
        return builder.build();
    }

    private AiInvocationResult<AgentAssistantMessage> invokeForStructuredChat(MessageCreateParams params) {
        AiInvocationResult<String> raw = invokeForText(params);
        AgentAssistantMessage structured = parseStructuredChat(raw.getData());
        return new AiInvocationResult<>(
                structured,
                raw.getTokenInput(),
                raw.getTokenOutput(),
                raw.getLatencyMs(),
                raw.getModel());
    }

    private AiInvocationResult<String> invokeForText(MessageCreateParams params) {
        Instant startedAt = Instant.now();
        Message response = anthropicClient.messages().create(params);
        int latencyMs = toInt(Duration.between(startedAt, Instant.now()).toMillis());
        return new AiInvocationResult<>(
                extractText(response),
                toInt(response.usage().inputTokens()),
                toInt(response.usage().outputTokens()),
                latencyMs,
                response.model().asString());
    }

    private AiInvocationResult<String> invokeForTextStream(MessageCreateParams params, Consumer<String> chunkConsumer) {
        Instant startedAt = Instant.now();
        StringBuilder content = new StringBuilder();
        AtomicInteger tokenInput = new AtomicInteger(0);
        AtomicInteger tokenOutput = new AtomicInteger(0);
        AtomicReference<String> model = new AtomicReference<>(params.model().asString());
        Consumer<String> safeChunkConsumer = chunkConsumer == null ? ignored -> { } : chunkConsumer;
        try (StreamResponse<RawMessageStreamEvent> streamResponse = anthropicClient.messages().createStreaming(params)) {
            streamResponse.stream().forEachOrdered(event -> {
                if (event.isMessageStart()) {
                    Message message = event.asMessageStart().message();
                    tokenInput.set(toInt(message.usage().inputTokens()));
                    tokenOutput.set(toInt(message.usage().outputTokens()));
                    model.set(message.model().asString());
                    return;
                }
                if (event.isMessageDelta()) {
                    MessageDeltaUsage usage = event.asMessageDelta().usage();
                    tokenOutput.set(toInt(usage.outputTokens()));
                    if (usage.inputTokens().isPresent()) {
                        tokenInput.set(toInt(usage.inputTokens().get()));
                    }
                    return;
                }
                if (!event.isContentBlockDelta()) {
                    return;
                }
                RawContentBlockDelta delta = event.asContentBlockDelta().delta();
                if (!delta.isText()) {
                    return;
                }
                String chunk = delta.asText().text();
                if (!StringUtils.hasText(chunk)) {
                    return;
                }
                content.append(chunk);
                safeChunkConsumer.accept(chunk);
            });
        }
        int latencyMs = toInt(Duration.between(startedAt, Instant.now()).toMillis());
        if (!StringUtils.hasText(content.toString())) {
            throw new IllegalStateException("Anthropic 流式响应未返回可解析的文本内容");
        }
        return new AiInvocationResult<>(content.toString(), tokenInput.get(), tokenOutput.get(), latencyMs, model.get());
    }

    private <T> AiInvocationResult<T> invokeForJson(MessageCreateParams params, Class<T> targetType) {
        return invokeForJson(params, targetType, null);
    }

    private <T> AiInvocationResult<T> invokeForJson(MessageCreateParams params,
                                                    Class<T> targetType,
                                                    Consumer<String> chunkConsumer) {
        AiInvocationResult<String> raw = chunkConsumer == null
                ? invokeForText(params)
                : invokeForTextStream(params, chunkConsumer);
        String payload = extractJsonPayload(raw.getData());
        T parsed = JsonUtil.parseObject(payload, targetType);
        if (parsed == null) {
            log.error("Failed to parse Anthropic JSON response. targetType={}, payload={}", targetType.getSimpleName(), payload);
            throw new IllegalStateException("Anthropic 返回的 JSON 无法解析为 " + targetType.getSimpleName());
        }
        return new AiInvocationResult<>(parsed, raw.getTokenInput(), raw.getTokenOutput(), raw.getLatencyMs(), raw.getModel());
    }

    private AgentAssistantMessage parseStructuredChat(String rawText) {
        String payload = extractJsonPayload(rawText);
        AgentAssistantMessage parsed = JsonUtil.parseObject(payload, AgentAssistantMessage.class);
        if (parsed == null) {
            return AgentAssistantMessage.builder()
                    .messageType(AgentMessageTypeConstants.TEXT)
                    .content(defaultText(rawText).trim())
                    .payload(null)
                    .build();
        }
        return normalizeStructuredChat(parsed, rawText);
    }

    private AgentAssistantMessage normalizeStructuredChat(AgentAssistantMessage raw, String fallbackText) {
        String messageType = defaultText(raw.getMessageType()).trim().toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(messageType)) {
            messageType = AgentMessageTypeConstants.TEXT;
        }

        if (!AgentMessageTypeConstants.INTERACTIVE_FORM.equals(messageType)) {
            return AgentAssistantMessage.builder()
                    .messageType(AgentMessageTypeConstants.TEXT)
                    .content(resolveTextContent(raw.getContent(), fallbackText))
                    .payload(null)
                    .build();
        }

        InteractiveFormPayload normalizedPayload = normalizeInteractiveForm(raw.getPayload());
        if (normalizedPayload == null || normalizedPayload.getQuestions().isEmpty()) {
            return AgentAssistantMessage.builder()
                    .messageType(AgentMessageTypeConstants.TEXT)
                    .content(resolveTextContent(raw.getContent(), fallbackText))
                    .payload(null)
                    .build();
        }

        return AgentAssistantMessage.builder()
                .messageType(AgentMessageTypeConstants.INTERACTIVE_FORM)
                .content(defaultText(raw.getContent()).trim())
                .payload(normalizedPayload)
                .build();
    }

    private InteractiveFormPayload normalizeInteractiveForm(InteractiveFormPayload payload) {
        if (payload == null) {
            return null;
        }

        List<InteractiveQuestion> normalizedQuestions = new ArrayList<>();
        List<InteractiveQuestion> questions = payload.getQuestions();
        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                InteractiveQuestion normalized = normalizeQuestion(questions.get(i), i);
                if (normalized != null) {
                    normalizedQuestions.add(normalized);
                }
            }
        }

        if (normalizedQuestions.isEmpty()) {
            return null;
        }

        return InteractiveFormPayload.builder()
                .formId(StringUtils.hasText(payload.getFormId()) ? payload.getFormId().trim() : "form_" + UUID.randomUUID())
                .title(defaultText(payload.getTitle()).trim())
                .description(defaultText(payload.getDescription()).trim())
                .submitMode("batch")
                .questions(normalizedQuestions)
                .build();
    }

    private InteractiveQuestion normalizeQuestion(InteractiveQuestion question, int index) {
        if (question == null || !StringUtils.hasText(question.getLabel())) {
            return null;
        }

        String type = defaultText(question.getType()).trim().toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(type)) {
            type = "single_choice";
        }

        List<InteractiveOption> normalizedOptions = normalizeOptions(question.getOptions());
        if (("single_choice".equals(type) || "multi_choice".equals(type)) && normalizedOptions.isEmpty()) {
            return null;
        }

        return InteractiveQuestion.builder()
                .id(StringUtils.hasText(question.getId()) ? question.getId().trim() : "question_" + (index + 1))
                .label(question.getLabel().trim())
                .type(type)
                .required(question.getRequired() == null || question.getRequired())
                .allowCustomInput(Boolean.TRUE.equals(question.getAllowCustomInput()))
                .placeholder(defaultText(question.getPlaceholder()).trim())
                .options("text_input".equals(type) ? List.of() : normalizedOptions)
                .build();
    }

    private List<InteractiveOption> normalizeOptions(List<InteractiveOption> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        return options.stream()
                .filter(Objects::nonNull)
                .filter(option -> StringUtils.hasText(option.getLabel()) || StringUtils.hasText(option.getValue()))
                .map(option -> InteractiveOption.builder()
                        .label(resolveOptionValue(option.getLabel(), option.getValue()))
                        .value(resolveOptionValue(option.getValue(), option.getLabel()))
                        .description(defaultText(option.getDescription()).trim())
                        .build())
                .toList();
    }

    private String resolveOptionValue(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        return defaultText(fallback).trim();
    }

    private String resolveTextContent(String content, String fallbackText) {
        if (StringUtils.hasText(content)) {
            return content.trim();
        }
        String stripped = extractJsonPayload(defaultText(fallbackText)).trim();
        if (stripped.startsWith("{") && stripped.endsWith("}")) {
            return "好的，我继续为你整理写作方向。";
        }
        return defaultText(fallbackText).trim();
    }

    private List<MessageParam> toAnthropicMessages(List<AgentMessageDO> messages) {
        return messages.stream()
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> MessageParam.builder()
                        .role(toAnthropicRole(message.getRole()))
                        .content(normalizeMessageContent(message))
                        .build())
                .collect(Collectors.toList());
    }

    private MessageParam.Role toAnthropicRole(AgentMessageRole role) {
        if (role == AgentMessageRole.ASSISTANT) {
            return MessageParam.Role.ASSISTANT;
        }
        return MessageParam.Role.USER;
    }

    private String normalizeMessageContent(AgentMessageDO message) {
        if (message.getRole() == AgentMessageRole.SYSTEM) {
            return "[SYSTEM]\n" + message.getContent();
        }
        return message.getContent();
    }

    private String buildGeneratePrompt(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return "Notebook 摘要 JSON:\n"
                + prettyJson(normalizeNotebook(notebookSummary))
                + "\n\n会话记录:\n"
                + toTranscript(messages)
                + "\n\n请基于以上内容生成一版可预览文章草稿，只返回 JSON。";
    }

    private String buildOptimizePrompt(String instruction,
                                       String currentTitle,
                                       String currentSummary,
                                       String currentContent) {
        return "优化指令:\n"
                + defaultText(instruction)
                + "\n\n当前标题:\n"
                + defaultText(currentTitle)
                + "\n\n当前摘要:\n"
                + defaultText(currentSummary)
                + "\n\n当前正文 Markdown:\n"
                + defaultText(currentContent)
                + "\n\n请基于以上内容输出优化后的完整 JSON。";
    }

    private String buildNotebookPrompt(List<AgentMessageDO> messages, NotebookSummary currentNotebook) {
        return "当前 notebook JSON:\n"
                + prettyJson(normalizeNotebook(currentNotebook))
                + "\n\n会话记录:\n"
                + toTranscript(messages)
                + "\n\n请输出更新后的 notebook JSON。";
    }

    private String toTranscript(List<AgentMessageDO> messages) {
        if (messages == null || messages.isEmpty()) {
            return "(暂无会话记录)";
        }
        return messages.stream()
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> message.getRole().name() + ": " + message.getContent())
                .collect(Collectors.joining("\n\n"));
    }

    private String renderTemplate(String templateName, Map<String, String> variables) {
        String template = promptTemplateLoader.load(templateName);
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", defaultText(entry.getValue()));
        }
        return rendered;
    }

    private NotebookSummary normalizeNotebook(NotebookSummary notebookSummary) {
        if (notebookSummary != null) {
            return notebookSummary;
        }
        return NotebookSummary.builder().build();
    }

    private String extractText(Message response) {
        String text = response.content().stream()
                .filter(ContentBlock::isText)
                .map(ContentBlock::asText)
                .map(block -> block.text().trim())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"));
        if (!StringUtils.hasText(text)) {
            throw new IllegalStateException("Anthropic 未返回可解析的文本内容");
        }
        return text;
    }

    private String extractJsonPayload(String rawText) {
        String text = stripCodeFence(rawText);
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String stripCodeFence(String text) {
        String trimmed = defaultText(text).trim();
        if (trimmed.startsWith("```")) {
            int firstLineEnd = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                return trimmed.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private String prettyJson(Object value) {
        try {
            return JsonUtil.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception ex) {
            return JsonUtil.toJsonString(value);
        }
    }

    private void validateApiKeyConfigured() {
        if (!StringUtils.hasText(properties.getAnthropic().getApiKey())) {
            throw new IllegalStateException("ANTHROPIC_API_KEY 未配置，无法调用 Anthropic SDK");
        }
    }

    private int toInt(long value) {
        if (value <= 0) {
            return 0;
        }
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    private String defaultText(String value) {
        return Objects.toString(value, "");
    }
}
