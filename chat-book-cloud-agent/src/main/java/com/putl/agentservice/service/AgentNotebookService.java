package com.putl.agentservice.service;

import com.putl.agentservice.model.vo.NotebookSummary;

public interface AgentNotebookService {

    NotebookSummary initializeNotebook(String title);

    NotebookSummary refreshNotebook(Integer sessionId);
}
