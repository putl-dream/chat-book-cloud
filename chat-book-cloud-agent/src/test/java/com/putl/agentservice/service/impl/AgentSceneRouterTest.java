package com.putl.agentservice.service.impl;

import com.putl.agentservice.enums.AgentAssistantAction;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.DraftReadiness;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.model.vo.SceneDecision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentSceneRouterTest {

    private final AgentSceneRouter router = new AgentSceneRouter();

    @Test
    void routeShouldEnterLearnSceneWhenLearningIntentDetected() {
        SceneDecision decision = router.route(
                AgentSessionDO.builder().sceneType(AgentSceneType.DISCUSS).build(),
                List.of(userMessage("帮我解释一下 RAG 的原理和适用场景")),
                NotebookSummary.builder()
                        .goal("理解 RAG")
                        .build(),
                false);

        assertEquals(AgentSceneType.LEARN, decision.getCurrentScene());
        assertEquals(AgentAssistantAction.TEACH, decision.getAssistantAction());
    }

    @Test
    void routeShouldStayInDiscussWhenDraftIntentButNotebookOnlyPartiallyPrepared() {
        SceneDecision decision = router.route(
                AgentSessionDO.builder().sceneType(AgentSceneType.DISCUSS).build(),
                List.of(userMessage("现在直接生成初稿")),
                NotebookSummary.builder()
                        .goal("写 agent 增强方案")
                        .draftReadiness(DraftReadiness.NOT_READY)
                        .build(),
                false);

        assertEquals(AgentSceneType.DISCUSS, decision.getCurrentScene());
        assertEquals(AgentAssistantAction.ASK, decision.getAssistantAction());
        assertEquals(DraftReadiness.PARTIAL, decision.getDraftReadiness());
    }

    @Test
    void routeShouldEnterDraftWhenNotebookReadyAndUserRequestsDraft() {
        SceneDecision decision = router.route(
                AgentSessionDO.builder().sceneType(AgentSceneType.DISCUSS).build(),
                List.of(userMessage("可以开始生成初稿了")),
                NotebookSummary.builder()
                        .goal("写 agent 增强方案")
                        .facts(List.of("现有 agent 只有单轮 prompt 约束", "已有 notebook 缓存", "需要多场景路由"))
                        .knowledgePoints(List.of("场景状态机"))
                        .outline(List.of("问题背景", "实现路径"))
                        .draftReadiness(DraftReadiness.READY)
                        .build(),
                false);

        assertEquals(AgentSceneType.DRAFT, decision.getCurrentScene());
        assertEquals(AgentAssistantAction.SUGGEST_DRAFT, decision.getAssistantAction());
        assertEquals(DraftReadiness.READY, decision.getDraftReadiness());
    }

    @Test
    void routeShouldEnterEditWhenDraftContextExistsAndEditIntentDetected() {
        SceneDecision decision = router.route(
                AgentSessionDO.builder().sceneType(AgentSceneType.DRAFT).build(),
                List.of(userMessage("把标题再润色一下，并扩写第二节")),
                NotebookSummary.builder()
                        .goal("优化现有草稿")
                        .draftReadiness(DraftReadiness.READY)
                        .build(),
                true);

        assertEquals(AgentSceneType.EDIT, decision.getCurrentScene());
        assertEquals(AgentAssistantAction.EDIT_DRAFT, decision.getAssistantAction());
    }

    private AgentMessageDO userMessage(String content) {
        return AgentMessageDO.builder()
                .role(AgentMessageRole.USER)
                .content(content)
                .build();
    }
}
