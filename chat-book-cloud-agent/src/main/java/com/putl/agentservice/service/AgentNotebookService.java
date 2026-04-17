package com.putl.agentservice.service;

import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.model.vo.NotebookSummary;

public interface AgentNotebookService {

    NotebookSummary initializeNotebook(String title, AgentSceneType initialScene);

    NotebookSummary refreshNotebook(Integer sessionId);
}
