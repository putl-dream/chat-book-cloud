package com.putl.agentservice.client;

import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.ArticleAiExecutionEngine;
import com.putl.agentservice.client.task.ArticleChatTask;
import com.putl.agentservice.client.task.ArticleDraftGenerateTask;
import com.putl.agentservice.client.task.ArticleDraftOptimizeTask;
import com.putl.agentservice.client.task.ArticleSummaryExtractTask;
import com.putl.agentservice.client.task.NotebookSummarizeTask;
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
    private final ArticleChatTask articleChatTask;
    private final ArticleDraftGenerateTask articleDraftGenerateTask;
    private final ArticleDraftOptimizeTask articleDraftOptimizeTask;
    private final ArticleSummaryExtractTask articleSummaryExtractTask;
    private final NotebookSummarizeTask notebookSummarizeTask;

    /**
     * 构造 Anthropic 文章 AI 门面
     *
     * @param executionEngine          执行引擎
     * @param articleChatTask          文章对话任务
     * @param articleDraftGenerateTask 草稿生成任务
     * @param articleDraftOptimizeTask 草稿优化任务
     * @param articleSummaryExtractTask 摘要提取任务
     * @param notebookSummarizeTask    笔记本摘要任务
     */
    public AnthropicArticleAiGateway(ArticleAiExecutionEngine executionEngine,
                                     ArticleChatTask articleChatTask,
                                     ArticleDraftGenerateTask articleDraftGenerateTask,
                                     ArticleDraftOptimizeTask articleDraftOptimizeTask,
                                     ArticleSummaryExtractTask articleSummaryExtractTask,
                                     NotebookSummarizeTask notebookSummarizeTask) {
        this.executionEngine = executionEngine;
        this.articleChatTask = articleChatTask;
        this.articleDraftGenerateTask = articleDraftGenerateTask;
        this.articleDraftOptimizeTask = articleDraftOptimizeTask;
        this.articleSummaryExtractTask = articleSummaryExtractTask;
        this.notebookSummarizeTask = notebookSummarizeTask;
    }

    /**
     * 与 AI 进行对话交互
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要上下文
     * @return AI 助手回复结果
     */
    @Override
    public AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return executionEngine.execute(
                articleChatTask,
                ArticleAiContext.builder()
                        .messages(messages)
                        .notebookSummary(notebookSummary)
                        .build());
    }

    /**
     * 生成文章草稿（非流式）
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要上下文
     * @return 生成的草稿结果
     */
    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary) {
        return generateDraft(messages, notebookSummary, null);
    }

    /**
     * 生成文章草稿（流式）
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要上下文
     * @param chunkConsumer   内容块回调，用于处理流式响应
     * @return 生成的草稿结果
     */
    @Override
    public AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages,
                                                                NotebookSummary notebookSummary,
                                                                Consumer<String> chunkConsumer) {
        return executionEngine.execute(
                articleDraftGenerateTask,
                ArticleAiContext.builder()
                        .messages(messages)
                        .notebookSummary(notebookSummary)
                        .build(),
                chunkConsumer);
    }

    /**
     * 优化现有文章草稿
     *
     * @param instruction    优化指令
     * @param currentTitle  当前标题
     * @param currentSummary 当前摘要
     * @param currentContent 当前正文内容
     * @return 优化后的草稿结果
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
     * @param title   当前标题
     * @param content 当前正文内容
     * @return AI 提炼后的摘要
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
     * 对笔记本内容进行摘要
     *
     * @param messages       历史消息列表
     * @param currentNotebook 当前笔记本摘要
     * @return 更新后的笔记本摘要
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
}
