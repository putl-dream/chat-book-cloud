package com.putl.agentservice.client.engine;

import com.putl.agentservice.constants.AgentMessageTypeConstants;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import com.putl.agentservice.model.vo.InteractiveFormPayload;
import com.putl.agentservice.model.vo.InteractiveOption;
import com.putl.agentservice.model.vo.InteractiveQuestion;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class AiResponseParser {

    public <T> T parseJsonObject(String rawText, Class<T> targetType) {
        String payload = extractJsonPayload(rawText);
        T parsed = JsonUtil.parseObject(payload, targetType);
        if (parsed == null) {
            log.error("Failed to parse Anthropic JSON response. targetType={}, payload={}", targetType.getSimpleName(), payload);
            throw new IllegalStateException("Anthropic 返回的 JSON 无法解析为 " + targetType.getSimpleName());
        }
        return parsed;
    }

    /**
     * 解析结构化对话响应
     *
     * @param rawText 原始响应文本
     * @return 解析后的助手消息对象
     */
    public AgentAssistantMessage parseStructuredChat(String rawText) {
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
     * 标准化结构化对话消息
     *
     * @param raw          原始解析结果
     * @param fallbackText 兜底文本
     * @return 标准化后的助手消息
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
     * 标准化交互表单
     *
     * @param payload 表单数据
     * @return 标准化后的表单数据，校验失败返回 null
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
     * 标准化单个问题
     *
     * @param question 问题对象
     * @param index    问题索引
     * @return 标准化后的问题，校验失败返回 null
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
     * 标准化选项列表
     *
     * @param options 选项列表
     * @return 标准化后的选项列表
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
     * 解析选项值，优先使用 primary
     *
     * @param primary  优先值
     * @param fallback 兜底值
     * @return 解析后的选项值
     */
    private String resolveOptionValue(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        return defaultText(fallback).trim();
    }

    /**
     * 解析文本内容
     *
     * @param content       内容字段
     * @param fallbackText 兜底文本
     * @return 解析后的文本
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
     * 从原始文本中提取 JSON 内容
     *
     * @param rawText 原始文本
     * @return JSON 字符串
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
     * 去除代码 fence 包装
     *
     * @param text 原始文本
     * @return 去除 fence 后的文本
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
     * 安全获取字符串值
     *
     * @param value 原始值
     * @return 字符串值，null 时返回空字符串
     */
    private String defaultText(String value) {
        return Objects.toString(value, "");
    }
}
