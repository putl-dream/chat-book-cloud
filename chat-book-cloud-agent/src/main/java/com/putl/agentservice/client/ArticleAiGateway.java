package com.putl.agentservice.client;

import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.NotebookSummary;

import java.util.List;

public interface ArticleAiGateway {

    AiInvocationResult<String> chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary);

    AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary);

    AiInvocationResult<ArticleDraftResult> optimizeDraft(String instruction, String currentTitle, String currentSummary, String currentContent);

    AiInvocationResult<NotebookSummary> summarizeNotebook(List<AgentMessageDO> messages, NotebookSummary currentNotebook);
}
