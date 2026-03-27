package com.putl.agentservice.service.impl;

import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookService;
import org.springframework.stereotype.Service;

@Service
public class AgentNotebookServiceImpl implements AgentNotebookService {

    @Override
    public NotebookSummary initializeNotebook(String title) {
        return NotebookSummary.builder()
                .goal(title)
                .summary("会话刚创建，等待收集写作目标和上下文。")
                .build();
    }

    @Override
    public NotebookSummary refreshNotebook(Integer sessionId) {
        return NotebookSummary.builder()
                .summary("Notebook refresh is not wired yet for session " + sessionId)
                .build();
    }
}
