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

/**
 * Agent场景路由器
 * <p>根据用户消息、会话状态和笔记本上下文，智能判断并路由到合适的AI处理场景。
 * 支持讨论、学习、草稿生成、编辑四种主要场景的自动切换。</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@Component
public class AgentSceneRouter {

    /**
     * 草稿生成相关关键词列表
     * <p>用于识别用户是否希望生成文章初稿</p>
     */
    private static final List<String> DRAFT_KEYWORDS = List.of(
            "生成初稿", "写一版", "出一版", "起草", "写成文章", "生成文章", "开始成稿", "首稿", "写篇文章");
    
    /**
     * 学习模式相关关键词列表
     * <p>用于识别用户是否处于知识学习和理解阶段</p>
     */
    private static final List<String> LEARN_KEYWORDS = List.of(
            "学习", "讲解", "解释", "理解", "是什么", "原理", "入门", "知识点", "教我", "科普", "梳理一下");
    
    /**
     * 编辑修改相关关键词列表
     * <p>用于识别用户是否希望对现有内容进行润色或修改</p>
     */
    private static final List<String> EDIT_KEYWORDS = List.of(
            "润色", "改写", "修改", "优化", "重写", "扩写", "续写", "补全", "补写", "精简", "压缩", "调整结构", "改标题", "改摘要");
    
    /**
     * 讨论交流相关关键词列表
     * <p>用于识别用户是否在进行思路探讨和观点交流</p>
     */
    private static final List<String> DISCUSS_KEYWORDS = List.of(
            "讨论", "思路", "选题", "论点", "方向", "怎么写", "想法", "观点");

    /**
     * 路由决策主方法
     * <p>综合分析会话状态、历史消息、笔记本上下文和草稿状态，决定当前应进入的场景类型</p>
     *
     * @param session 会话信息，包含场景类型等基础状态
     * @param messages 历史消息列表，用于分析最新用户意图
     * @param notebook 笔记本摘要，提供写作目标和知识积累状态
     * @param hasDraftContext 是否存在草稿上下文，影响编辑场景的判断
     * @return 场景决策结果，包含当前场景、下一场景、助手动作等信息
     */
    public SceneDecision route(AgentSessionDO session,
                               List<AgentMessageDO> messages,
                               NotebookSummary notebook,
                               boolean hasDraftContext) {
        // 解析基准场景（基于会话和笔记本状态）
        AgentSceneType baselineScene = resolveBaselineScene(session, notebook, hasDraftContext);
        // 评估草稿准备度（判断是否具备生成草稿的条件）
        DraftReadiness readiness = resolveDraftReadiness(notebook);
        // 提取最新的用户消息内容
        String latestUserMessage = latestUserMessage(messages);

        // 优先判断是否应进入编辑场景（需要草稿上下文且包含编辑关键词）
        if (shouldEnterEdit(latestUserMessage, baselineScene, hasDraftContext)) {
            return buildDecision(
                    AgentSceneType.EDIT,
                    AgentSceneType.EDIT,
                    "检测到当前请求聚焦在改写、润色或续写，切换到智能编辑场景。",
                    AgentAssistantAction.EDIT_DRAFT,
                    readiness);
        }

        // 检测用户是否明确请求生成草稿
        if (containsKeyword(latestUserMessage, DRAFT_KEYWORDS)) {
            if (readiness == DraftReadiness.READY) {
                // 草稿条件已满足，建议生成草稿
                return buildDecision(
                        AgentSceneType.DRAFT,
                        hasDraftContext ? AgentSceneType.EDIT : AgentSceneType.DRAFT,
                        "检测到用户希望开始成稿，当前写作材料已基本齐备。",
                        AgentAssistantAction.SUGGEST_DRAFT,
                        readiness);
            }
            // 草稿条件未满足，引导用户继续补充信息
            return buildDecision(
                    baselineScene == AgentSceneType.LEARN ? AgentSceneType.LEARN : AgentSceneType.DISCUSS,
                    baselineScene == AgentSceneType.LEARN ? AgentSceneType.LEARN : AgentSceneType.DISCUSS,
                    "用户希望进入首稿阶段，但当前信息仍不完整，需要继续补齐关键事实或结构。",
                    AgentAssistantAction.ASK,
                    readiness);
        }

        // 检测用户是否在学习模式下（询问概念、原理等）
        if (containsKeyword(latestUserMessage, LEARN_KEYWORDS)) {
            return buildDecision(
                    AgentSceneType.LEARN,
                    readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : AgentSceneType.LEARN,
                    "检测到当前目标更偏向知识解释和学习整理，切换到学习场景。",
                    AgentAssistantAction.TEACH,
                    readiness);
        }

        // 检测用户是否在讨论模式下（探讨思路、观点等）
        if (containsKeyword(latestUserMessage, DISCUSS_KEYWORDS)) {
            return buildDecision(
                    AgentSceneType.DISCUSS,
                    readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : AgentSceneType.DISCUSS,
                    "检测到当前目标是继续讨论主题、观点或写作方向。",
                    readiness == DraftReadiness.READY ? AgentAssistantAction.SUGGEST_DRAFT : AgentAssistantAction.ASK,
                    readiness);
        }

        // 如果基准场景是编辑且有草稿，继续保持编辑场景
        if (baselineScene == AgentSceneType.EDIT && hasDraftContext) {
            return buildDecision(
                    AgentSceneType.EDIT,
                    AgentSceneType.EDIT,
                    "沿用当前编辑上下文，继续围绕现有草稿推进修改。",
                    AgentAssistantAction.EDIT_DRAFT,
                    readiness);
        }

        // 如果基准场景是学习，继续学习场景
        if (baselineScene == AgentSceneType.LEARN) {
            return buildDecision(
                    AgentSceneType.LEARN,
                    readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : AgentSceneType.LEARN,
                    "沿用上一轮学习上下文，继续补充知识理解和例子拆解。",
                    AgentAssistantAction.TEACH,
                    readiness);
        }

        // 默认场景：讨论模式，帮助用户补齐事实和论点
        return buildDecision(
                AgentSceneType.DISCUSS,
                readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : AgentSceneType.DISCUSS,
                "默认继续围绕主题讨论，帮助用户补齐事实、论点和结构。",
                readiness == DraftReadiness.READY ? AgentAssistantAction.SUGGEST_DRAFT : AgentAssistantAction.ASK,
                readiness);
    }

    /**
     * 最终确定场景决策
     * <p>在初步决策基础上，结合草稿上下文和准备度进行最终调整，确定下一场景和助手动作</p>
     *
     * @param currentDecision 当前的场景决策结果
     * @param notebook 笔记本摘要，用于评估草稿准备度
     * @param hasDraftContext 是否存在草稿上下文
     * @return 最终确定的场景决策结果
     */
    public SceneDecision finalizeDecision(SceneDecision currentDecision,
                                          NotebookSummary notebook,
                                          boolean hasDraftContext) {
        if (currentDecision == null) {
            // 如果没有当前决策，执行完整的路由流程
            return route(null, List.of(), notebook, hasDraftContext);
        }
        // 重新评估草稿准备度
        DraftReadiness readiness = resolveDraftReadiness(notebook);
        // 根据草稿上下文和当前场景确定下一场景
        AgentSceneType nextScene = hasDraftContext || currentDecision.getCurrentScene() == AgentSceneType.DRAFT
                ? AgentSceneType.EDIT
                : readiness == DraftReadiness.READY ? AgentSceneType.DRAFT : currentDecision.getCurrentScene();
        // 根据当前场景确定助手的下一步动作
        AgentAssistantAction action = switch (currentDecision.getCurrentScene()) {
            case LEARN -> AgentAssistantAction.TEACH;  // 学习场景：教学动作
            case EDIT -> AgentAssistantAction.EDIT_DRAFT;  // 编辑场景：编辑草稿动作
            case DRAFT -> hasDraftContext ? AgentAssistantAction.EDIT_DRAFT : AgentAssistantAction.GENERATE_DRAFT;  // 草稿场景：生成或编辑
            default -> nextScene == AgentSceneType.DRAFT ? AgentAssistantAction.SUGGEST_DRAFT : AgentAssistantAction.ASK;  // 默认：建议草稿或提问
        };
        return buildDecision(
                currentDecision.getCurrentScene(),
                nextScene,
                currentDecision.getSwitchReason(),
                action,
                readiness);
    }

    /**
     * 应用场景决策到笔记本摘要
     * <p>将场景决策的结果更新到笔记本摘要中，保持状态的一致性</p>
     *
     * @param notebook 原始笔记本摘要
     * @param decision 场景决策结果
     * @param hasDraftContext 是否存在草稿上下文
     * @return 更新后的笔记本摘要
     */
    public NotebookSummary applyDecision(NotebookSummary notebook,
                                         SceneDecision decision,
                                         boolean hasDraftContext) {
        // 如果笔记本为空，创建空的笔记本摘要
        NotebookSummary source = notebook == null ? NotebookSummary.builder().build() : notebook;
        // 获取最终确定的决策结果
        SceneDecision effective = finalizeDecision(decision, source, hasDraftContext);
        // 构建更新后的笔记本摘要，保留原有内容并更新场景相关字段
        return NotebookSummary.builder()
                .goal(source.getGoal())  // 保留写作目标
                .currentScene(effective.getCurrentScene())  // 更新当前场景
                .userIntent(source.getUserIntent())  // 保留用户意图
                .facts(defaultList(source.getFacts()))  // 保留事实列表
                .knowledgePoints(defaultList(source.getKnowledgePoints()))  // 保留知识点列表
                .openQuestions(defaultList(source.getOpenQuestions()))  // 保留待解决问题列表
                .outline(defaultList(source.getOutline()))  // 保留大纲列表
                .styleRules(defaultList(source.getStyleRules()))  // 保留风格规则列表
                .editingTargets(defaultList(source.getEditingTargets()))  // 保留编辑目标列表
                .draftReadiness(effective.getDraftReadiness())  // 更新草稿准备度
                .nextSuggestedAction(effective.getAssistantAction())  // 更新下一步建议动作
                .summary(source.getSummary())  // 保留摘要内容
                .build();
    }

    /**
     * 草稿生成完成后的决策
     * <p>当首稿生成完成后，自动切换到编辑场景，准备后续的修改和完善工作</p>
     *
     * @param notebook 笔记本摘要
     * @return 草稿生成后的场景决策结果
     */
    public SceneDecision draftGeneratedDecision(NotebookSummary notebook) {
        DraftReadiness readiness = resolveDraftReadiness(notebook);
        return buildDecision(
                AgentSceneType.DRAFT,
                AgentSceneType.EDIT,
                "首稿已经生成，下一阶段切换为智能编辑和补全。",
                AgentAssistantAction.EDIT_DRAFT,
                readiness == DraftReadiness.NOT_READY ? DraftReadiness.READY : readiness);
    }
    /**
     * 解析草稿准备度
     * <p>综合启发式评估和持久化状态，确定当前是否具备生成草稿的条件</p>
     *
     * @param notebook 笔记本摘要，包含事实、知识点、大纲等信息
     * @return 草稿准备度枚举值（NOT_READY/PARTIAL/READY）
     */
    public DraftReadiness resolveDraftReadiness(NotebookSummary notebook) {
        if (notebook == null) {
            return DraftReadiness.NOT_READY;
        }
        // 基于启发式规则评估准备度
        DraftReadiness heuristic = heuristicReadiness(notebook);
        // 获取持久化的准备度状态
        DraftReadiness persisted = DraftReadiness.safeValue(notebook.getDraftReadiness());
        // 取两者中较高的准备度级别
        return heuristic.ordinal() > persisted.ordinal() ? heuristic : persisted;
    }

    /**
     * 启发式草稿准备度评估
     * <p>基于笔记本中的事实数量、知识点数量、大纲完整性等指标，评估是否具备生成草稿的条件</p>
     *
     * @param notebook 笔记本摘要
     * @return 评估得到的草稿准备度
     */
    private DraftReadiness heuristicReadiness(NotebookSummary notebook) {
        // 统计各类信息的数量
        int factCount = safeSize(notebook.getFacts());  // 事实数量
        int knowledgeCount = safeSize(notebook.getKnowledgePoints());  // 知识点数量
        int outlineCount = safeSize(notebook.getOutline());  // 大纲条目数量
        int openQuestionCount = safeSize(notebook.getOpenQuestions());  // 待解决问题数量
        // 判断是否有明确的写作目标或摘要
        boolean hasGoal = StringUtils.hasText(notebook.getGoal()) || StringUtils.hasText(notebook.getSummary());

        // 准备就绪条件：大纲至少2条，事实+知识点至少3个，待解决问题不超过1个
        if (outlineCount >= 2 && factCount + knowledgeCount >= 3 && openQuestionCount <= 1) {
            return DraftReadiness.READY;
        }
        // 部分准备条件：有明确目标或积累了部分信息
        if (hasGoal || factCount + knowledgeCount + outlineCount > 0) {
            return DraftReadiness.PARTIAL;
        }
        // 未准备：缺乏足够的信息支撑
        return DraftReadiness.NOT_READY;
    }

    /**
     * 解析基准场景类型
     * <p>从笔记本摘要或会话信息中提取基准场景，作为路由决策的基础</p>
     *
     * @param session 会话信息
     * @param notebook 笔记本摘要
     * @param hasDraftContext 是否存在草稿上下文
     * @return 基准场景类型
     */
    private AgentSceneType resolveBaselineScene(AgentSessionDO session,
                                                NotebookSummary notebook,
                                                boolean hasDraftContext) {
        // 优先使用笔记本中的场景类型
        AgentSceneType notebookScene = notebook == null ? null : notebook.getCurrentScene();
        if (notebookScene != null) {
            return notebookScene.toRuntimeScene();
        }
        // 其次使用会话中的场景类型
        if (session != null && session.getSceneType() != null) {
            return session.getSceneType().toRuntimeScene();
        }
        // 默认场景：有草稿则为编辑，否则为讨论
        return hasDraftContext ? AgentSceneType.EDIT : AgentSceneType.DISCUSS;
    }

    /**
     * 判断是否应进入编辑场景
     * <p>检查用户消息是否包含编辑关键词，或当前处于编辑场景且有延续性操作</p>
     *
     * @param latestUserMessage 最新的用户消息内容
     * @param baselineScene 基准场景类型
     * @param hasDraftContext 是否存在草稿上下文
     * @return true表示应进入编辑场景，false表示不应进入
     */
    private boolean shouldEnterEdit(String latestUserMessage,
                                    AgentSceneType baselineScene,
                                    boolean hasDraftContext) {
        // 没有草稿上下文时不能进入编辑场景
        if (!hasDraftContext) {
            return false;
        }
        // 包含编辑关键词则进入编辑场景
        if (containsKeyword(latestUserMessage, EDIT_KEYWORDS)) {
            return true;
        }
        // 非编辑场景不考虑延续性操作
        if (baselineScene != AgentSceneType.EDIT) {
            return false;
        }
        // 编辑场景下的延续性操作词（继续、再来、这段等）
        return containsKeyword(latestUserMessage, List.of("继续", "再来", "这段", "这里", "上面", "下面"));
    }

    /**
     * 检查文本是否包含关键词列表中的任意一个关键词
     * <p>使用大小写不敏感的匹配方式</p>
     *
     * @param value 待检查的文本内容
     * @param keywords 关键词列表
     * @return true表示包含至少一个关键词，false表示不包含
     */
    private boolean containsKeyword(String value, List<String> keywords) {
        if (!StringUtils.hasText(value) || CollectionUtils.isEmpty(keywords)) {
            return false;
        }
        // 转换为小写进行不区分大小写的匹配
        String normalized = value.toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(normalized::contains);
    }

    /**
     * 提取最新的用户消息内容
     * <p>从消息列表中反向查找最近一条用户角色的消息</p>
     *
     * @param messages 历史消息列表
     * @return 最新用户消息的内容，如果没有则返回空字符串
     */
    private String latestUserMessage(List<AgentMessageDO> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return "";
        }
        // 从后向前遍历，找到第一条用户消息
        for (int i = messages.size() - 1; i >= 0; i -= 1) {
            AgentMessageDO message = messages.get(i);
            if (message != null && message.getRole() == AgentMessageRole.USER) {
                return message.getContent() == null ? "" : message.getContent().trim();
            }
        }
        return "";
    }

    /**
     * 构建场景决策对象
     * <p>封装场景决策的各个要素，包括当前场景、下一场景、切换原因、助手动作和草稿准备度</p>
     *
     * @param currentScene 当前场景类型
     * @param nextScene 下一场景类型
     * @param switchReason 场景切换的原因说明
     * @param assistantAction 助手应采取的动作
     * @param draftReadiness 草稿准备度
     * @return 构建完成的场景决策对象
     */
    private SceneDecision buildDecision(AgentSceneType currentScene,
                                        AgentSceneType nextScene,
                                        String switchReason,
                                        AgentAssistantAction assistantAction,
                                        DraftReadiness draftReadiness) {
        return SceneDecision.builder()
                .currentScene(currentScene == null ? AgentSceneType.DISCUSS : currentScene)  // 默认场景为讨论
                .nextScene(nextScene == null ? currentScene : nextScene)  // 下一场景默认为当前场景
                .switchReason(switchReason)  // 切换原因
                .assistantAction(assistantAction == null ? AgentAssistantAction.ASK : assistantAction)  // 默认动作为提问
                .draftReadiness(DraftReadiness.safeValue(draftReadiness))  // 安全的草稿准备度值
                .build();
    }

    /**
     * 安全获取列表大小
     * <p>处理列表可能为null的情况，避免空指针异常</p>
     *
     * @param values 待检查的列表
     * @return 列表大小，如果列表为null则返回0
     */
    private int safeSize(List<String> values) {
        return values == null ? 0 : values.size();
    }

    /**
     * 提供默认的不可变空列表
     * <p>处理列表可能为null的情况，确保返回值不为null</p>
     *
     * @param values 原始列表
     * @return 原始列表或空列表（如果原始列表为null）
     */
    private List<String> defaultList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
