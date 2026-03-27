package com.putl.agentservice.client;

import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.NotebookSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnthropicArticleAiGateway implements ArticleAiGateway {

    private final AnthropicProperties properties;

    public AnthropicArticleAiGateway(AnthropicProperties properties) {
        this.properties = properties;
    }

    @Override
    public String chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return "Anthropic chat integration is not wired yet. Provider=" + properties.getProvider();
    }

    @Override
    public ArticleDraftResult generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return ArticleDraftResult.builder()
                .title("待生成标题")
                .summary("待生成摘要")
                .content("Anthropic draft generation integration is not wired yet.")
                .build();
    }

    @Override
    public ArticleDraftResult optimizeDraft(String instruction, String currentTitle, String currentSummary, String currentContent) {
        return ArticleDraftResult.builder()
                .title(currentTitle)
                .summary(currentSummary)
                .content(currentContent)
                .build();
    }

    @Override
    public NotebookSummary summarizeNotebook(List<AgentMessageDO> messages, NotebookSummary currentNotebook) {
        return currentNotebook;
    }
}
