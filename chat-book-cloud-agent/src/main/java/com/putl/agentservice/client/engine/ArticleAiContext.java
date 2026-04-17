package com.putl.agentservice.client.engine;

import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.NotebookSummary;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ArticleAiContext {

    private final List<AgentMessageDO> messages;

    private final NotebookSummary notebookSummary;

    private final AgentSceneType sceneType;

    private final String instruction;

    private final String currentTitle;

    private final String currentSummary;

    private final String currentContent;
}
