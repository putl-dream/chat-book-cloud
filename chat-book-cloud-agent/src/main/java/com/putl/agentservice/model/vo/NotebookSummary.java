package com.putl.agentservice.model.vo;

import com.putl.agentservice.enums.AgentAssistantAction;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.DraftReadiness;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotebookSummary {

    private String goal;

    private AgentSceneType currentScene;

    private String userIntent;

    @Builder.Default
    private List<String> facts = new ArrayList<>();

    @Builder.Default
    private List<String> knowledgePoints = new ArrayList<>();

    @Builder.Default
    private List<String> openQuestions = new ArrayList<>();

    @Builder.Default
    private List<String> outline = new ArrayList<>();

    @Builder.Default
    private List<String> styleRules = new ArrayList<>();

    @Builder.Default
    private List<String> editingTargets = new ArrayList<>();

    private DraftReadiness draftReadiness;

    private AgentAssistantAction nextSuggestedAction;

    private String summary;
}
