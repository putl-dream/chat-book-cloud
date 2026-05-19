package com.putl.agentservice.client.task;

import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.ArticleAiRequest;
import com.putl.agentservice.client.engine.ArticleAiRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ArticleLearnTask extends AbstractAnthropicArticleAiTask<AgentAssistantMessage> {

    public ArticleLearnTask(AnthropicProperties properties,
                            ArticleAiRequestFactory requestFactory,
                            com.putl.agentservice.client.engine.PromptTemplateService promptTemplateService,
                            AgentMessageAssembler agentMessageAssembler,
                            AiResponseParser aiResponseParser) {
        super(properties, requestFactory, promptTemplateService, agentMessageAssembler, aiResponseParser);
    }

    @Override
    public String taskCode() {
        return "article_learn";
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
                renderTemplate(PromptTemplateConstants.ARTICLE_LEARN, Map.of(
                        "notebook_json", prettyJson(normalizeNotebook(context.getNotebookSummary()))
                )));
        agentMessageAssembler.toArticleMessages(context.getMessages()).forEach(builder::addMessage);
        return builder.build();
    }

    @Override
    public AgentAssistantMessage parseResponse(String rawText) {
        return aiResponseParser.parseStructuredChat(rawText);
    }
}
