package com.putl.agentservice.client;

import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.ArticleAiExecutionEngine;
import com.putl.agentservice.client.engine.StreamingControl;
import com.putl.agentservice.client.task.ArticleEditChatTask;
import com.putl.agentservice.client.task.ArticleChatTask;
import com.putl.agentservice.client.task.ArticleDraftGenerateTask;
import com.putl.agentservice.client.task.ArticleDraftOptimizeTask;
import com.putl.agentservice.client.task.ArticleLearnTask;
import com.putl.agentservice.client.task.ArticleSummaryExtractTask;
import com.putl.agentservice.client.task.NotebookSummarizeTask;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.ArticleSummaryResponse;
import com.putl.agentservice.model.vo.NotebookSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Anthropic 文章 AI 门面。
 * <p>对外保留强类型业务接口，对内统一通过执行引擎和任务策略完成模型调用。</p>
 */
@Slf4j
@Component
public class AnthropicArticleAiGateway implements ArticleAiGateway {

    private final ArticleAiExecutionEngine executionEngine;
    private final ArticleChatTask articleDiscussTask;
    private final ArticleLearnTask articleLearnTask;
    private final ArticleEditChatTask articleEditChatTask;
    private final ArticleDraftGenerateTask articleDraftGenerateTask;
    private final ArticleDraftOptimizeTask articleDraftOptimizeTask;
    private final ArticleSummaryExtractTask articleSummaryExtractTask;
    private final NotebookSummarizeTask notebookSummarizeTask;

    /**
     * 构造 Anthropic 文章 AI 门面
     *
     * @param executionEngine           执行引擎，负责任务的统一调度和执行
     * @param articleDiscussTask        文章对话任务，处理普通讨论场景
     * @param articleLearnTask          学习模式任务，处理深度学习场景
     * @param articleEditChatTask       编辑对话任务，处理编辑修改场景
     * @param articleDraftGenerateTask  草稿生成任务，负责从对话中生成文章草稿
     * @param articleDraftOptimizeTask  草稿优化任务，负责对现有草稿进行优化改进
     * @param articleSummaryExtractTask 摘要提取任务，负责从文章内容中提取关键摘要
     * @param notebookSummarizeTask     笔记本摘要任务，负责对笔记本内容进行智能摘要
     */
    public AnthropicArticleAiGateway(ArticleAiExecutionEngine executionEngine,
                                     ArticleChatTask articleDiscussTask,
                                     ArticleLearnTask articleLearnTask,
                                     ArticleEditChatTask articleEditChatTask,
                                     ArticleDraftGenerateTask articleDraftGenerateTask,
                                     ArticleDraftOptimizeTask articleDraftOptimizeTask,
                                     ArticleSummaryExtractTask articleSummaryExtractTask,
                                     NotebookSummarizeTask notebookSummarizeTask) {
        this.executionEngine = executionEngine;
        this.articleDiscussTask = articleDiscussTask;
        this.articleLearnTask = articleLearnTask;
        this.articleEditChatTask = articleEditChatTask;
        this.articleDraftGenerateTask = articleDraftGenerateTask;
        this.articleDraftOptimizeTask = articleDraftOptimizeTask;
        this.articleSummaryExtractTask = articleSummaryExtractTask;
        this.notebookSummarizeTask = notebookSummarizeTask;
    }

    /**
     * 与 AI 进行对话交互（默认讨论模式）
     *
     * @param messages        历史消息列表，包含用户和助手的对话记录
     * @param notebookSummary 当前笔记本摘要上下文，提供相关背景信息
     * @return AI 助手回复结果，包含完整的响应消息
     */
    @Override
    public AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return chat(messages, notebookSummary, AgentSceneType.DISCUSS, null, null, null);
    }

    /**
     * 与 AI 进行对话交互（支持场景选择）
     *
     * @param messages        历史消息列表，包含用户和助手的对话记录
     * @param notebookSummary 当前笔记本摘要上下文，提供相关背景信息
     * @param sceneType       场景类型，决定使用哪种对话策略（讨论/学习/编辑）
     * @param currentTitle    当前文章标题，为对话提供上下文
     * @param currentSummary  当前文章摘要，为对话提供上下文
     * @param currentContent  当前文章内容，为对话提供上下文
     * @return AI 助手回复结果，包含完整的响应消息
     */
    @Override
    public AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages,
                                                          NotebookSummary notebookSummary,
                                                          AgentSceneType sceneType,
                                                          String currentTitle,
                                                          String currentSummary,
                                                          String currentContent) {
        return chat(messages, notebookSummary, sceneType, currentTitle, currentSummary, currentContent, null,
                StreamingControl.noop());
    }

    /**
     * 与 AI 进行对话交互（完整参数版本，支持流式输出）
     *
     * @param messages         历史消息列表，包含用户和助手的对话记录
     * @param notebookSummary  当前笔记本摘要上下文，提供相关背景信息
     * @param sceneType        场景类型，决定使用哪种对话策略（讨论/学习/编辑）
     * @param currentTitle     当前文章标题，为对话提供上下文
     * @param currentSummary   当前文章摘要，为对话提供上下文
     * @param currentContent   当前文章内容，为对话提供上下文
     * @param chunkConsumer    流式内容块回调函数，用于实时处理AI响应片段
     * @param streamingControl 流式控制对象，用于管理流式输出的生命周期
     * @return AI 助手回复结果，包含完整的响应消息
     */
    @Override
    public AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages,
                                                          NotebookSummary notebookSummary,
                                                          AgentSceneType sceneType,
                                                          String currentTitle,
                                                          String currentSummary,
                                                          String currentContent,
                                                          Consumer<String> chunkConsumer,
                                                          StreamingControl streamingControl) {
        AgentSceneType runtimeScene = sceneType == null ? AgentSceneType.DISCUSS : sceneType.toRuntimeScene();
        return executionEngine.execute(
                resolveChatTask(runtimeScene),
                ArticleAiContext.builder()
                        .messages(messages)
                        .notebookSummary(notebookSummary)
                        .sceneType(runtimeScene)
                        .currentTitle(currentTitle)
                        .currentSummary(currentSummary)
                        .currentContent(currentContent)
                        .build(),
                chunkConsumer,
                streamingControl);
    }

    /**
     * 生成文章草稿（非流式同步调用）
     *
     * @param messages        历史消息列表，包含用户需求和对话上下文
     * @param notebookSummary 当前笔记本摘要上下文，提供相关背景信息
     * @return 生成的草稿结果，包含标题、摘要和正文内容
     */
    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return generateDraft(messages, notebookSummary, null);
    }

    /**
     * 生成文章草稿（流式异步调用）
     *
     * @param messages        历史消息列表，包含用户需求和对话上下文
     * @param notebookSummary 当前笔记本摘要上下文，提供相关背景信息
     * @param chunkConsumer   内容块回调函数，用于实时处理AI生成的文本片段
     * @return 生成的草稿结果，包含标题、摘要和正文内容
     */
    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages,
                                                                NotebookSummary notebookSummary,
                                                                Consumer<String> chunkConsumer) {
        return generateDraft(messages, notebookSummary, chunkConsumer, StreamingControl.noop());
    }

    /**
     * 生成文章草稿（完整参数版本，支持流式控制）
     *
     * @param messages         历史消息列表，包含用户需求和对话上下文
     * @param notebookSummary  当前笔记本摘要上下文，提供相关背景信息
     * @param chunkConsumer    流式内容块回调函数，用于实时处理AI生成的文本片段
     * @param streamingControl 流式控制对象，用于管理流式输出的生命周期
     * @return 生成的草稿结果，包含标题、摘要和正文内容
     */
    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages,
                                                                NotebookSummary notebookSummary,
                                                                Consumer<String> chunkConsumer,
                                                                StreamingControl streamingControl) {
        return executionEngine.execute(
                articleDraftGenerateTask,
                ArticleAiContext.builder()
                        .messages(messages)
                        .notebookSummary(notebookSummary)
                        .build(),
                chunkConsumer,
                streamingControl);
    }

    /**
     * 优化现有文章草稿
     *
     * @param instruction    优化指令，描述需要改进的具体方向和要求
     * @param currentTitle   当前文章标题，作为优化的基础
     * @param currentSummary 当前文章摘要，作为优化的基础
     * @param currentContent 当前文章正文内容，作为优化的基础
     * @return 优化后的草稿结果，包含改进后的标题、摘要和正文
     */
    @Override
    public AiInvocationResult<ArticleDraftResult> optimizeDraft(String instruction,
                                                                String currentTitle,
                                                                String currentSummary,
                                                                String currentContent) {
        return executionEngine.execute(
                articleDraftOptimizeTask,
                ArticleAiContext.builder()
                        .instruction(instruction)
                        .currentTitle(currentTitle)
                        .currentSummary(currentSummary)
                        .currentContent(currentContent)
                        .build());
    }

    /**
     * 从正文中提取文章摘要
     *
     * @param title   文章标题，辅助理解文章主题
     * @param content 文章正文内容，从中提取关键信息
     * @return AI 提炼后的摘要结果，包含精炼的文章概要
     */
    @Override
    public AiInvocationResult<ArticleSummaryResponse> extractSummary(String title, String content) {
        return executionEngine.execute(
                articleSummaryExtractTask,
                ArticleAiContext.builder()
                        .currentTitle(title)
                        .currentContent(content)
                        .build());
    }

    /**
     * 对笔记本内容进行智能摘要
     *
     * @param messages        历史消息列表，包含笔记本相关的对话记录
     * @param currentNotebook 当前笔记本摘要状态，作为更新的基础
     * @return 更新后的笔记本摘要，反映最新的笔记内容概况
     */
    @Override
    public AiInvocationResult<NotebookSummary> summarizeNotebook(List<AgentMessageDO> messages, NotebookSummary currentNotebook) {
        return executionEngine.execute(
                notebookSummarizeTask,
                ArticleAiContext.builder()
                        .messages(messages)
                        .notebookSummary(currentNotebook)
                        .build());
    }

    /**
     * 根据场景类型解析对应的聊天任务
     *
     * @param sceneType 场景类型枚举值
     * @return 对应的文章AI任务实例
     */
    private com.putl.agentservice.client.engine.ArticleAiTask<AgentAssistantMessage> resolveChatTask(AgentSceneType sceneType) {
        return switch (sceneType) {
            case LEARN -> articleLearnTask;
            case EDIT -> articleEditChatTask;
            default -> articleDiscussTask;
        };
    }
}
