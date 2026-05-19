package com.putl.agentservice.client.engine;

import com.anthropic.models.messages.MessageParam;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AgentMessageAssembler {

    /**
     * 将内部消息列表转换为 Anthropic 消息格式
     *
     * @param messages 内部消息列表
     * @return Anthropic MessageParam 列表
     */
    public List<MessageParam> toAnthropicMessages(List<AgentMessageDO> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        return messages.stream()
                .filter(Objects::nonNull)
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> MessageParam.builder()
                        .role(toAnthropicRole(message.getRole()))
                        .content(normalizeMessageContent(message))
                        .build())
                .collect(Collectors.toList());
    }

    public List<ArticleAiRequest.Message> toArticleMessages(List<AgentMessageDO> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        return messages.stream()
                .filter(Objects::nonNull)
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> newArticleMessage(toArticleRole(message.getRole()), normalizeMessageContent(message)))
                .collect(Collectors.toList());
    }

    /**
     * 将消息列表转换为可读会话记录格式
     *
     * @param messages 消息列表
     * @return 格式化的会话记录字符串
     */
    public String toTranscript(List<AgentMessageDO> messages) {
        if (messages == null || messages.isEmpty()) {
            return "(暂无会话记录)";
        }
        return messages.stream()
                .filter(Objects::nonNull)
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> resolveRoleName(message.getRole()) + ": " + message.getContent())
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 将内部角色转换为 Anthropic 角色
     *
     * @param role 内部消息角色
     * @return Anthropic 消息角色
     */
    private MessageParam.Role toAnthropicRole(AgentMessageRole role) {
        if (role == AgentMessageRole.ASSISTANT) {
            return MessageParam.Role.ASSISTANT;
        }
        return MessageParam.Role.USER;
    }

    private ArticleAiRequest.Role toArticleRole(AgentMessageRole role) {
        if (role == AgentMessageRole.ASSISTANT) {
            return ArticleAiRequest.Role.ASSISTANT;
        }
        return ArticleAiRequest.Role.USER;
    }

    private ArticleAiRequest.Message newArticleMessage(ArticleAiRequest.Role role, String content) {
        return ArticleAiRequest.Message.of(role, content);
    }

    /**
     * 标准化消息内容
     *
     * @param message 消息对象
     * @return 标准化后的内容字符串
     */
    private String normalizeMessageContent(AgentMessageDO message) {
        if (message.getRole() == AgentMessageRole.SYSTEM) {
            return "[SYSTEM]\n" + defaultText(message.getContent());
        }
        return defaultText(message.getContent());
    }

    /**
     * 解析角色名称
     *
     * @param role 消息角色
     * @return 角色名称字符串
     */
    private String resolveRoleName(AgentMessageRole role) {
        return role == null ? AgentMessageRole.USER.name() : role.name();
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
