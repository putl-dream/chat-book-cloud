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
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.prompt.PromptTemplateLoader;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public AiInvocationResult<String> chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return invokeForText(buildChatParams(messages, notebookSummary));
    }

    @Override
    public AiInvocationResult<String> chatStream(List<AgentMessageDO> messages,
                                                 NotebookSummary notebookSummary,
                                                 Consumer<String> chunkConsumer) {
        return invokeForTextStream(buildChatParams(messages, notebookSummary), chunkConsumer);
    }

    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        MessageCreateParams params = baseRequest(
                properties.getAnthropic().getModel().getGenerate(),
                properties.getAnthropic().getMaxTokens().getGenerate(),
                0.2,
                promptTemplateLoader.load(PromptTemplateConstants.ARTICLE_GENERATE))
                .addUserMessage(buildGeneratePrompt(messages, notebookSummary))
                .build();
        return invokeForJson(params, ArticleDraftResult.class);
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
                0.7,
                renderTemplate(PromptTemplateConstants.ARTICLE_CHAT, Map.of(
                        "notebook_json", prettyJson(normalizeNotebook(notebookSummary))
                )));
        toAnthropicMessages(messages).forEach(builder::addMessage);
        return builder.build();
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
        AiInvocationResult<String> raw = invokeForText(params);
        String payload = extractJsonPayload(raw.getData());
        T parsed = JsonUtil.parseObject(payload, targetType);
        if (parsed == null) {
            log.error("Failed to parse Anthropic JSON response. targetType={}, payload={}", targetType.getSimpleName(), payload);
            throw new IllegalStateException("Anthropic 返回的 JSON 无法解析为 " + targetType.getSimpleName());
        }
        return new AiInvocationResult<>(parsed, raw.getTokenInput(), raw.getTokenOutput(), raw.getLatencyMs(), raw.getModel());
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
