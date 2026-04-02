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
import com.putl.agentservice.model.vo.ArticleSummaryResponse;
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

/**
 * Anthropic AI 文章助手网关实现类
 * <p>通过 Anthropic SDK 与 Claude 模型交互，实现对话、草稿生成、草稿优化、笔记本摘要等功能</p>
 *
 * @see ArticleAiGateway
 */
@Slf4j
@Component
public class AnthropicArticleAiGateway implements ArticleAiGateway {

    private final AnthropicClient anthropicClient;
    private final AnthropicProperties properties;
    private final PromptTemplateLoader promptTemplateLoader;

    /**
     * 构造方法
     *
     * @param anthropicClient     Anthropic SDK 客户端
     * @param properties           Anthropic 配置属性
     * @param promptTemplateLoader Prompt 模板加载器
     */
    public AnthropicArticleAiGateway(AnthropicClient anthropicClient,
                                     AnthropicProperties properties,
                                     PromptTemplateLoader promptTemplateLoader) {
        this.anthropicClient = anthropicClient;
        this.properties = properties;
        this.promptTemplateLoader = promptTemplateLoader;
    }

    /**
     * 与 AI 进行对话交互
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要上下文
     * @return AI 助手回复结果
     */
    @Override
    public AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return invokeForStructuredChat(buildChatParams(messages, notebookSummary));
    }

    /**
     * 生成文章草稿（非流式）
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要上下文
     * @return 生成的草稿结果
     */
    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return generateDraft(messages, notebookSummary, null);
    }

    /**
     * 生成文章草稿（流式）
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要上下文
     * @param chunkConsumer   内容块回调Consumer，用于处理流式响应
     * @return 生成的草稿结果
     */
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

    /**
     * 优化现有文章草稿
     *
     * @param instruction     优化指令
     * @param currentTitle    当前标题
     * @param currentSummary  当前摘要
     * @param currentContent  当前正文内容
     * @return 优化后的草稿结果
     */
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

    /**
     * 从正文中提取文章摘要
     *
     * @param title   当前标题
     * @param content 当前正文内容
     * @return AI 提炼后的摘要
     */
    @Override
    public AiInvocationResult<ArticleSummaryResponse> extractSummary(String title, String content) {
        MessageCreateParams params = baseRequest(
                properties.getAnthropic().getModel().getOptimize(),
                properties.getAnthropic().getMaxTokens().getOptimize(),
                0.1,
                promptTemplateLoader.load(PromptTemplateConstants.ARTICLE_SUMMARY))
                .addUserMessage(buildSummaryPrompt(title, content))
                .build();
        return invokeForJson(params, ArticleSummaryResponse.class);
    }

    /**
     * 对笔记本内容进行摘要
     *
     * @param messages        历史消息列表
     * @param currentNotebook 当前笔记本摘要
     * @return 更新后的笔记本摘要
     */
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

    /**
     * 构建基础的请求参数构建器
     *
     * @param model        模型名称
     * @param maxTokens    最大输出 token 数
     * @param temperature  温度参数
     * @param systemPrompt 系统提示词
     * @return MessageCreateParams 构建器
     */
    private MessageCreateParams.Builder baseRequest(String model, Integer maxTokens, double temperature, String systemPrompt) {
        validateApiKeyConfigured();
        return MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens.longValue())
                .temperature(temperature)
                .system(systemPrompt);
    }

    /**
     * 构建聊天请求参数
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要
     * @return MessageCreateParams
     */
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

    /**
     * 调用 AI 并解析为结构化聊天响应
     *
     * @param params 请求参数
     * @return AI 助手消息结果
     */
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

    /**
     * 调用 AI 获取纯文本响应
     *
     * @param params 请求参数
     * @return 文本响应结果
     */
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

    /**
     * 调用 AI 获取流式文本响应
     *
     * @param params         请求参数
     * @param chunkConsumer  内容块回调Consumer
     * @return 文本响应结果
     */
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

    /**
     * 调用 AI 并将响应解析为 JSON 对象（非流式）
     *
     * @param params     请求参数
     * @param targetType 目标类型
     * @return JSON 解析后的结果
     */
    private <T> AiInvocationResult<T> invokeForJson(MessageCreateParams params, Class<T> targetType) {
        return invokeForJson(params, targetType, null);
    }

    /**
     * 调用 AI 并将响应解析为 JSON 对象（支持流式）
     *
     * @param params         请求参数
     * @param targetType     目标类型
     * @param chunkConsumer  内容块回调Consumer（可选）
     * @return JSON 解析后的结果
     */
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

    /**
     * 解析结构化聊天响应
     *
     * @param rawText 原始文本
     * @return 解析后的 AI 助手消息
     */
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

    /**
     * 规范化结构化聊天消息
     *
     * @param raw          原始解析结果
     * @param fallbackText 兜底文本
     * @return 规范化后的 AI 助手消息
     */
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

    /**
     * 规范化交互表单载荷
     *
     * @param payload 原始表单载荷
     * @return 规范化后的表单载荷
     */
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

    /**
     * 规范化交互问题
     *
     * @param question 原始问题
     * @param index    问题索引
     * @return 规范化后的问题
     */
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

    /**
     * 规范化选项列表
     *
     * @param options 原始选项列表
     * @return 规范化后的选项列表
     */
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

    /**
     * 解析选项值，优先使用 primary，否则使用 fallback
     */
    private String resolveOptionValue(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        return defaultText(fallback).trim();
    }

    /**
     * 解析文本内容，优先使用 content，否则使用 fallbackText
     */
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

    /**
     * 将消息列表转换为 Anthropic 消息格式
     */
    private List<MessageParam> toAnthropicMessages(List<AgentMessageDO> messages) {
        return messages.stream()
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> MessageParam.builder()
                        .role(toAnthropicRole(message.getRole()))
                        .content(normalizeMessageContent(message))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 将内部角色转换为 Anthropic 角色
     */
    private MessageParam.Role toAnthropicRole(AgentMessageRole role) {
        if (role == AgentMessageRole.ASSISTANT) {
            return MessageParam.Role.ASSISTANT;
        }
        return MessageParam.Role.USER;
    }

    /**
     * 规范化消息内容，系统消息添加特殊前缀
     */
    private String normalizeMessageContent(AgentMessageDO message) {
        if (message.getRole() == AgentMessageRole.SYSTEM) {
            return "[SYSTEM]\n" + message.getContent();
        }
        return message.getContent();
    }

    /**
     * 构建草稿生成的 Prompt
     */
    private String buildGeneratePrompt(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return "Notebook 摘要 JSON:\n"
                + prettyJson(normalizeNotebook(notebookSummary))
                + "\n\n会话记录:\n"
                + toTranscript(messages)
                + "\n\n请基于以上内容生成一版可预览文章草稿，只返回 JSON。";
    }

    /**
     * 构建草稿优化的 Prompt
     */
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

    /**
     * 构建笔记本摘要的 Prompt
     */
    private String buildNotebookPrompt(List<AgentMessageDO> messages, NotebookSummary currentNotebook) {
        return "当前 notebook JSON:\n"
                + prettyJson(normalizeNotebook(currentNotebook))
                + "\n\n会话记录:\n"
                + toTranscript(messages)
                + "\n\n请输出更新后的 notebook JSON。";
    }

    private String buildSummaryPrompt(String title, String content) {
        return "当前标题:\n"
                + defaultText(title)
                + "\n\n当前正文:\n"
                + defaultText(content)
                + "\n\n请输出文章摘要 JSON。";
    }

    /**
     * 将消息列表转换为对话文本
     */
    private String toTranscript(List<AgentMessageDO> messages) {
        if (messages == null || messages.isEmpty()) {
            return "(暂无会话记录)";
        }
        return messages.stream()
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> message.getRole().name() + ": " + message.getContent())
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 渲染 Prompt 模板，替换变量占位符
     *
     * @param templateName 模板名称
     * @param variables    变量占位符映射
     * @return 渲染后的模板字符串
     */
    private String renderTemplate(String templateName, Map<String, String> variables) {
        String template = promptTemplateLoader.load(templateName);
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", defaultText(entry.getValue()));
        }
        return rendered;
    }

    /**
     * 规范化笔记本摘要，null 时返回空对象
     */
    private NotebookSummary normalizeNotebook(NotebookSummary notebookSummary) {
        if (notebookSummary != null) {
            return notebookSummary;
        }
        return NotebookSummary.builder().build();
    }

    /**
     * 从 Anthropic 响应中提取纯文本内容
     */
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

    /**
     * 从原始文本中提取 JSON 载荷
     */
    private String extractJsonPayload(String rawText) {
        String text = stripCodeFence(rawText);
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /**
     * 去除代码 fences（如 ```json ... ```）
     */
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

    /**
     * 格式化对象为美化 JSON 字符串
     */
    private String prettyJson(Object value) {
        try {
            return JsonUtil.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception ex) {
            return JsonUtil.toJsonString(value);
        }
    }

    /**
     * 验证 API Key 是否已配置
     */
    private void validateApiKeyConfigured() {
        if (!StringUtils.hasText(properties.getAnthropic().getApiKey())) {
            throw new IllegalStateException("ANTHROPIC_API_KEY 未配置，无法调用 Anthropic SDK");
        }
    }

    /**
     * 将 long 值安全转换为 int
     */
    private int toInt(long value) {
        if (value <= 0) {
            return 0;
        }
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    /**
     * 安全获取字符串值，null 时返回空字符串
     */
    private String defaultText(String value) {
        return Objects.toString(value, "");
    }
}
