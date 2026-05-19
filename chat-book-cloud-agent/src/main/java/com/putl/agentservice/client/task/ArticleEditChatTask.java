package com.putl.agentservice.client.task;

import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.ArticleAiRequest;
import com.putl.agentservice.client.engine.ArticleAiRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.PromptTemplateService;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ArticleEditChatTask extends AbstractAnthropicArticleAiTask<AgentAssistantMessage> {

    public ArticleEditChatTask(AnthropicProperties properties,
                               ArticleAiRequestFactory requestFactory,
                               PromptTemplateService promptTemplateService,
                               AgentMessageAssembler agentMessageAssembler,
                               AiResponseParser aiResponseParser) {
        super(properties, requestFactory, promptTemplateService, agentMessageAssembler, aiResponseParser);
    }

    @Override
    public String taskCode() {
        return "article_edit_chat";
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public ArticleAiRequest createRequest(ArticleAiContext context) {
        ArticleAiRequest.Builder builder = newRequest(
                chatModel(),
                chatMaxTokens(),
                0.2,
                renderTemplate(PromptTemplateConstants.ARTICLE_EDIT, Map.of(
                        "notebook_json", prettyJson(normalizeNotebook(context.getNotebookSummary())),
                        "draft_json", prettyJson(buildDraftSnapshot(context))
                )));
        agentMessageAssembler.toArticleMessages(context.getMessages()).forEach(builder::addMessage);
        return builder.build();
    }

    @Override
    public AgentAssistantMessage parseResponse(String rawText) {
        return aiResponseParser.parseStructuredChat(rawText);
    }

    private Map<String, String> buildDraftSnapshot(ArticleAiContext context) {
        Map<String, String> draftSnapshot = new LinkedHashMap<>();
        draftSnapshot.put("title", text(context.getCurrentTitle()));
        draftSnapshot.put("summary", text(context.getCurrentSummary()));
        draftSnapshot.put("content", text(context.getCurrentContent()));
        return draftSnapshot;
    }
}
