package com.putl.agentservice.model.vo;

import com.putl.agentservice.enums.AgentAssistantAction;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.DraftReadiness;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneDecision {

    private AgentSceneType currentScene;

    private AgentSceneType nextScene;

    private String switchReason;

    private AgentAssistantAction assistantAction;

    private DraftReadiness draftReadiness;
}
