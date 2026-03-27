package com.putl.agentservice.client;

import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.NotebookSummary;

import java.util.List;

public interface ArticleAiGateway {

    String chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary);

    ArticleDraftResult generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary);

    ArticleDraftResult optimizeDraft(String instruction, String currentTitle, String currentSummary, String currentContent);

    NotebookSummary summarizeNotebook(List<AgentMessageDO> messages, NotebookSummary currentNotebook);
}
