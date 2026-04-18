package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.ArticleAiExecutionEngine;
import com.putl.agentservice.client.task.ArticleEditorAssistTask;
import com.putl.agentservice.model.dto.EditorAssistMessage;
import com.putl.agentservice.model.dto.EditorAssistRequest;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.EditorAssistResponse;
import com.putl.agentservice.service.EditorAssistService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class EditorAssistServiceImpl implements EditorAssistService {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_SUMMARY_LENGTH = 1200;
    private static final int MAX_CONTENT_LENGTH = 16000;
    private static final int MAX_SELECTED_TEXT_LENGTH = 4000;
    private static final int MAX_HISTORY_MESSAGES = 8;
    private static final int MAX_HISTORY_MESSAGE_LENGTH = 800;

    private final ArticleAiExecutionEngine executionEngine;
    private final ArticleEditorAssistTask articleEditorAssistTask;

    public EditorAssistServiceImpl(ArticleAiExecutionEngine executionEngine,
                                   ArticleEditorAssistTask articleEditorAssistTask) {
        this.executionEngine = executionEngine;
        this.articleEditorAssistTask = articleEditorAssistTask;
    }

    @Override
    public EditorAssistResponse assist(EditorAssistRequest request) {
        EditorAssistRequest safeRequest = request == null ? new EditorAssistRequest() : request;
        AiInvocationResult<EditorAssistResponse> invocation = executionEngine.execute(
                articleEditorAssistTask,
                ArticleAiContext.builder()
                        .instruction(buildInstruction(safeRequest))
                        .currentTitle(clip(safeRequest.getTitle(), MAX_TITLE_LENGTH))
                        .currentSummary(clip(safeRequest.getSummary(), MAX_SUMMARY_LENGTH))
                        .currentContent(clip(safeRequest.getContent(), MAX_CONTENT_LENGTH))
                        .build());
        if (invocation == null || invocation.getData() == null) {
            return EditorAssistResponse.builder()
                    .reply("这次没有拿到可应用的编辑结果，请换一种说法再试一次。")
                    .result("")
                    .build();
        }
        EditorAssistResponse response = invocation.getData();
        return EditorAssistResponse.builder()
                .reply(defaultText(response.getReply()).trim())
                .result(defaultText(response.getResult()).trim())
                .build();
    }

    private String buildInstruction(EditorAssistRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("任务模式: ")
                .append(normalizeMode(request.getMode()))
                .append('\n');
        builder.append("应用目标: ")
                .append(normalizeApplyTarget(request.getApplyTarget()))
                .append('\n');
        builder.append("用户要求:\n")
                .append(resolveUserInstruction(request))
                .append('\n');
        builder.append("\n当前选中文本:\n")
                .append(resolveSelectedText(request.getSelectedText()))
                .append('\n');
        builder.append("\n最近对话:\n")
                .append(buildHistory(request.getHistory()));
        return builder.toString();
    }

    private String resolveUserInstruction(EditorAssistRequest request) {
        String instruction = clip(request == null ? null : request.getInstruction(), MAX_HISTORY_MESSAGE_LENGTH);
        if (StringUtils.hasText(instruction)) {
            return instruction;
        }
        if ("CONTINUE".equals(normalizeMode(request == null ? null : request.getMode()))) {
            return "请紧接当前光标位置自然续写，延续现有语气、结构和信息密度，优先生成 1 到 3 段可直接接入正文的内容。";
        }
        return "请根据当前上下文完成一次可直接应用到正文的局部改写。";
    }

    private String resolveSelectedText(String value) {
        String selected = clip(value, MAX_SELECTED_TEXT_LENGTH);
        return StringUtils.hasText(selected) ? selected : "(当前没有选中文本)";
    }

    private String buildHistory(List<EditorAssistMessage> history) {
        if (CollectionUtils.isEmpty(history)) {
            return "(无)";
        }
        StringBuilder builder = new StringBuilder();
        int startIndex = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        for (int index = startIndex; index < history.size(); index += 1) {
            EditorAssistMessage message = history.get(index);
            if (message == null || !StringUtils.hasText(message.getContent())) {
                continue;
            }
            String role = normalizeRole(message.getRole());
            String content = clip(message.getContent(), MAX_HISTORY_MESSAGE_LENGTH);
            builder.append(role).append(": ").append(content).append('\n');
        }
        return builder.length() > 0 ? builder.toString().trim() : "(无)";
    }

    private String normalizeMode(String value) {
        String normalized = defaultText(value).trim().toUpperCase(Locale.ROOT);
        if ("CHAT".equals(normalized)) {
            return "CHAT";
        }
        return "CONTINUE".equals(normalized) ? "CONTINUE" : "EDIT";
    }

    private String normalizeApplyTarget(String value) {
        String normalized = defaultText(value).trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CHAT_ONLY" -> "CHAT_ONLY";
            case "REPLACE_SELECTION", "APPEND_TO_END" -> normalized;
            default -> "INSERT_AT_CURSOR";
        };
    }

    private String normalizeRole(String value) {
        String normalized = defaultText(value).trim().toLowerCase(Locale.ROOT);
        return "assistant".equals(normalized) ? "assistant" : "user";
    }

    private String clip(String value, int maxLength) {
        String normalized = defaultText(value).trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength)) + "...";
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
