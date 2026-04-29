package com.putl.agentservice.client.task;

import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.ArticleAiRequest;
import com.putl.agentservice.client.engine.ArticleAiRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.PromptTemplateService;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.model.vo.EditorAssistResponse;
import org.springframework.stereotype.Component;

@Component
public class ArticleEditorAssistTask extends AbstractAnthropicArticleAiTask<EditorAssistResponse> {

    public ArticleEditorAssistTask(AnthropicProperties properties,
                                   ArticleAiRequestFactory requestFactory,
                                   PromptTemplateService promptTemplateService,
                                   AgentMessageAssembler agentMessageAssembler,
                                   AiResponseParser aiResponseParser) {
        super(properties, requestFactory, promptTemplateService, agentMessageAssembler, aiResponseParser);
    }

    @Override
    public String taskCode() {
        return "article_editor_assist";
    }

    @Override
    public ArticleAiRequest createRequest(ArticleAiContext context) {
        return newRequest(
                optimizeModel(),
                optimizeMaxTokens(),
                0.2,
                loadTemplate(PromptTemplateConstants.ARTICLE_EDITOR_ASSIST))
                .addUserMessage(buildPrompt(context))
                .build();
    }

    @Override
    public EditorAssistResponse parseResponse(String rawText) {
        return aiResponseParser.parseJsonObject(rawText, EditorAssistResponse.class);
    }

    private String buildPrompt(ArticleAiContext context) {
        return "编辑指令:\n"
                + text(context.getInstruction())
                + "\n\n当前标题:\n"
                + text(context.getCurrentTitle())
                + "\n\n当前摘要:\n"
                + text(context.getCurrentSummary())
                + "\n\n当前正文纯文本:\n"
                + text(context.getCurrentContent())
                + "\n\n请基于以上上下文输出固定 JSON。";
    }
}
