package com.putl.agentservice.service.impl;

import com.putl.agentservice.enums.AgentAssistantAction;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.DraftReadiness;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.model.vo.SceneDecision;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Component
public class AgentSceneRouter {

    private static final List<String> DRAFT_KEYWORDS = List.of(
            "生成初稿", "写一版", "出一版", "起草", "写成文章", "生成文章", "开始成稿", "首稿", "写篇文章");
    private static final List<String> LEARN_KEYWORDS = List.of(
            "学习", "讲解", "解释", "理解", "是什么", "原理", "入门", "知识点", "教我", "科普", "梳理一下");
    private static final List<String> EDIT_KEYWORDS = List.of(
            "润色", "改写", "修改", "优化", "重写", "扩写", "续写", "补全", "补写", "精简", "压缩", "调整结构", "改标题", "改摘要");
    private static final List<String> DISCUSS_KEYWORDS = List.of(
            "讨论", "思路", "选题", "论点", "方向", "怎么写", "想法", "观点");

    public SceneDecision route(AgentSessionDO session,
                               List<AgentMessageDO> messages,
                               NotebookSummary notebook,
                               boolean hasDraftContext) {
        AgentSceneType baselineScene = resolveBaselineScene(session, notebook, hasDraftContext);
        DraftReadiness readiness = resolveDraftReadiness(notebook);
        String latestUserMessage = latestUserMessage(messages);

        if (shouldEnterEdit(latestUserMessage, baselineScene, hasDraftContext)) {
            return buildDecision(
                    AgentSceneType.EDIT,
                    AgentSceneType.EDIT,
                    "检测到当前请求聚焦在改写、润色或续写，切换到智能编辑场景。",
                    AgentAssistantAction.EDIT_DRAFT,
                    readiness);
        }

        if (containsKeyword(latestUserMessage, DRAFT_KEYWORDS)) {
            if (readiness == DraftReadiness.READY) {
                return buildDecision(
                        AgentSceneType.DRAFT,
                        hasDraftContext ? AgentSceneType.EDIT : AgentSceneType.DRAFT,
                        "检测到用户希望开始成稿，当前写作材料已基本齐备。",
                        AgentAssistantAction.SUGGEST_DRAFT,
                        readiness);
            }
            return buildDecision(
                    baselineScene == AgentSceneType.LEARN ? AgentSceneType.LEARN : AgentSceneType.DISCUSS,
                    baselineScene == AgentSceneType.LEARN ? AgentSceneType.LEARN : AgentSceneType.DISCUSS,
                    "用户希望进入首稿阶段，但当前信息仍不完整，需要继续补齐关键事实或结构。",
                    AgentAssistantAction.ASK,
                    readiness);
        }

        if (containsKeyword(latestUserMessage, LEARN_KEYWORDS)) {
            return buildDecision(
                    AgentSceneType.LEARN,
                    readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : AgentSceneType.LEARN,
                    "检测到当前目标更偏向知识解释和学习整理，切换到学习场景。",
                    AgentAssistantAction.TEACH,
                    readiness);
        }

        if (containsKeyword(latestUserMessage, DISCUSS_KEYWORDS)) {
            return buildDecision(
                    AgentSceneType.DISCUSS,
                    readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : AgentSceneType.DISCUSS,
                    "检测到当前目标是继续讨论主题、观点或写作方向。",
                    readiness == DraftReadiness.READY ? AgentAssistantAction.SUGGEST_DRAFT : AgentAssistantAction.ASK,
                    readiness);
        }

        if (baselineScene == AgentSceneType.EDIT && hasDraftContext) {
            return buildDecision(
                    AgentSceneType.EDIT,
                    AgentSceneType.EDIT,
                    "沿用当前编辑上下文，继续围绕现有草稿推进修改。",
                    AgentAssistantAction.EDIT_DRAFT,
                    readiness);
        }

        if (baselineScene == AgentSceneType.LEARN) {
            return buildDecision(
                    AgentSceneType.LEARN,
                    readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : AgentSceneType.LEARN,
                    "沿用上一轮学习上下文，继续补充知识理解和例子拆解。",
                    AgentAssistantAction.TEACH,
                    readiness);
        }

        return buildDecision(
                AgentSceneType.DISCUSS,
                readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : AgentSceneType.DISCUSS,
                "默认继续围绕主题讨论，帮助用户补齐事实、论点和结构。",
                readiness == DraftReadiness.READY ? AgentAssistantAction.SUGGEST_DRAFT : AgentAssistantAction.ASK,
                readiness);
    }

    public SceneDecision finalizeDecision(SceneDecision currentDecision,
                                          NotebookSummary notebook,
                                          boolean hasDraftContext) {
        if (currentDecision == null) {
            return route(null, List.of(), notebook, hasDraftContext);
        }
        DraftReadiness readiness = resolveDraftReadiness(notebook);
        AgentSceneType nextScene = hasDraftContext || currentDecision.getCurrentScene() == AgentSceneType.DRAFT
                ? AgentSceneType.EDIT
                : readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : currentDecision.getCurrentScene();
        AgentAssistantAction action = switch (currentDecision.getCurrentScene()) {
            case LEARN -> AgentAssistantAction.TEACH;
            case EDIT -> AgentAssistantAction.EDIT_DRAFT;
            case DRAFT -> hasDraftContext ? AgentAssistantAction.EDIT_DRAFT : AgentAssistantAction.GENERATE_DRAFT;
            default -> nextScene == AgentSceneType.DRAFT ? AgentAssistantAction.SUGGEST_DRAFT : AgentAssistantAction.ASK;
        };
        return buildDecision(
                currentDecision.getCurrentScene(),
                nextScene,
                currentDecision.getSwitchReason(),
                action,
                readiness);
    }

    public NotebookSummary applyDecision(NotebookSummary notebook,
                                         SceneDecision decision,
                                         boolean hasDraftContext) {
        NotebookSummary source = notebook == null ? NotebookSummary.builder().build() : notebook;
        SceneDecision effective = finalizeDecision(decision, source, hasDraftContext);
        return NotebookSummary.builder()
                .goal(source.getGoal())
                .currentScene(effective.getCurrentScene())
                .userIntent(source.getUserIntent())
                .facts(defaultList(source.getFacts()))
                .knowledgePoints(defaultList(source.getKnowledgePoints()))
                .openQuestions(defaultList(source.getOpenQuestions()))
                .outline(defaultList(source.getOutline()))
                .styleRules(defaultList(source.getStyleRules()))
                .editingTargets(defaultList(source.getEditingTargets()))
                .draftReadiness(effective.getDraftReadiness())
                .nextSuggestedAction(effective.getAssistantAction())
                .summary(source.getSummary())
                .build();
    }

    public SceneDecision draftGeneratedDecision(NotebookSummary notebook) {
        DraftReadiness readiness = resolveDraftReadiness(notebook);
        return buildDecision(
                AgentSceneType.DRAFT,
                AgentSceneType.EDIT,
                "首稿已经生成，下一阶段切换为智能编辑和补全。",
                AgentAssistantAction.EDIT_DRAFT,
                readiness == DraftReadiness.NOT_READY ? DraftReadiness.READY : readiness);
    }

    public DraftReadiness resolveDraftReadiness(NotebookSummary notebook) {
        if (notebook == null) {
            return DraftReadiness.NOT_READY;
        }
        DraftReadiness heuristic = heuristicReadiness(notebook);
        DraftReadiness persisted = DraftReadiness.safeValue(notebook.getDraftReadiness());
        return heuristic.ordinal() > persisted.ordinal() ? heuristic : persisted;
    }

    private DraftReadiness heuristicReadiness(NotebookSummary notebook) {
        int factCount = safeSize(notebook.getFacts());
        int knowledgeCount = safeSize(notebook.getKnowledgePoints());
        int outlineCount = safeSize(notebook.getOutline());
        int openQuestionCount = safeSize(notebook.getOpenQuestions());
        boolean hasGoal = StringUtils.hasText(notebook.getGoal()) || StringUtils.hasText(notebook.getSummary());

        if (outlineCount >= 2 && factCount + knowledgeCount >= 3 && openQuestionCount <= 1) {
            return DraftReadiness.READY;
        }
        if (hasGoal || factCount + knowledgeCount + outlineCount > 0) {
            return DraftReadiness.PARTIAL;
        }
        return DraftReadiness.NOT_READY;
    }

    private AgentSceneType resolveBaselineScene(AgentSessionDO session,
                                                NotebookSummary notebook,
                                                boolean hasDraftContext) {
        AgentSceneType notebookScene = notebook == null ? null : notebook.getCurrentScene();
        if (notebookScene != null) {
            return notebookScene.toRuntimeScene();
        }
        if (session != null && session.getSceneType() != null) {
            return session.getSceneType().toRuntimeScene();
        }
        return hasDraftContext ? AgentSceneType.EDIT : AgentSceneType.DISCUSS;
    }

    private boolean shouldEnterEdit(String latestUserMessage,
                                    AgentSceneType baselineScene,
                                    boolean hasDraftContext) {
        if (!hasDraftContext) {
            return false;
        }
        if (containsKeyword(latestUserMessage, EDIT_KEYWORDS)) {
            return true;
        }
        if (baselineScene != AgentSceneType.EDIT) {
            return false;
        }
        return containsKeyword(latestUserMessage, List.of("继续", "再来", "这段", "这里", "上面", "下面"));
    }

    private boolean containsKeyword(String value, List<String> keywords) {
        if (!StringUtils.hasText(value) || CollectionUtils.isEmpty(keywords)) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(normalized::contains);
    }

    private String latestUserMessage(List<AgentMessageDO> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return "";
        }
        for (int i = messages.size() - 1; i >= 0; i -= 1) {
            AgentMessageDO message = messages.get(i);
            if (message != null && message.getRole() == AgentMessageRole.USER) {
                return message.getContent() == null ? "" : message.getContent().trim();
            }
        }
        return "";
    }

    private SceneDecision buildDecision(AgentSceneType currentScene,
                                        AgentSceneType nextScene,
                                        String switchReason,
                                        AgentAssistantAction assistantAction,
                                        DraftReadiness draftReadiness) {
        return SceneDecision.builder()
                .currentScene(currentScene == null ? AgentSceneType.DISCUSS : currentScene)
                .nextScene(nextScene == null ? currentScene : nextScene)
                .switchReason(switchReason)
                .assistantAction(assistantAction == null ? AgentAssistantAction.ASK : assistantAction)
                .draftReadiness(DraftReadiness.safeValue(draftReadiness))
                .build();
    }

    private int safeSize(List<String> values) {
        return values == null ? 0 : values.size();
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
