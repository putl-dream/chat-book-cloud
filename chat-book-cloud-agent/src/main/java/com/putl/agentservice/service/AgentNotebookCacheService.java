package com.putl.agentservice.service;

import com.putl.agentservice.model.vo.NotebookSummary;

public interface AgentNotebookCacheService {

    NotebookSummary getNotebook(Integer sessionId);

    void cacheNotebook(Integer sessionId, NotebookSummary notebookSummary);

    void saveNotebook(Integer sessionId, NotebookSummary notebookSummary);
}
