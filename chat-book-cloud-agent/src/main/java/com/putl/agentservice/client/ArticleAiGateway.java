package com.putl.agentservice.client;

import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.client.engine.StreamingControl;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.ArticleSummaryResponse;
import com.putl.agentservice.model.vo.NotebookSummary;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI 文章助手网关接口
 * <p>负责与 AI 模型交互，提供对话、草稿生成、草稿优化、笔记本摘要等功能</p>
 */
public interface ArticleAiGateway {

    /**
     * 与 AI 进行对话交互
     *
     * @param messages        历史消息列表
     * @param notebookSummary  当前笔记本摘要上下文
     * @return AI 助手回复结果
     */
    AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages, NotebookSummary notebookSummary);

    AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages,
                                                   NotebookSummary notebookSummary,
                                                   AgentSceneType sceneType,
                                                   String currentTitle,
                                                   String currentSummary,
                                                   String currentContent);

    default AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages,
                                                           NotebookSummary notebookSummary,
                                                           AgentSceneType sceneType,
                                                           String currentTitle,
                                                           String currentSummary,
                                                           String currentContent,
                                                           Consumer<String> chunkConsumer) {
        return chat(messages, notebookSummary, sceneType, currentTitle, currentSummary, currentContent,
                chunkConsumer, StreamingControl.noop());
    }

    AiInvocationResult<AgentAssistantMessage> chat(List<AgentMessageDO> messages,
                                                   NotebookSummary notebookSummary,
                                                   AgentSceneType sceneType,
                                                   String currentTitle,
                                                   String currentSummary,
                                                   String currentContent,
                                                   Consumer<String> chunkConsumer,
                                                   StreamingControl streamingControl);

    /**
     * 生成文章草稿（非流式）
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要上下文
     * @return 生成的草稿结果
     */
    AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages, NotebookSummary notebookSummary);

    /**
     * 生成文章草稿（流式）
     *
     * @param messages        历史消息列表
     * @param notebookSummary 当前笔记本摘要上下文
     * @param chunkConsumer   内容块回调Consumer，用于处理流式响应
     * @return 生成的草稿结果
     */
    default AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages,
                                                                 NotebookSummary notebookSummary,
                                                                 Consumer<String> chunkConsumer) {
        return generateDraft(messages, notebookSummary, chunkConsumer, StreamingControl.noop());
    }

    AiInvocationResult<ArticleDraftResult> generateDraft(List<AgentMessageDO> messages,
                                                         NotebookSummary notebookSummary,
                                                         Consumer<String> chunkConsumer,
                                                         StreamingControl streamingControl);

    /**
     * 优化现有文章草稿
     *
     * @param instruction     优化指令
     * @param currentTitle    当前标题
     * @param currentSummary  当前摘要
     * @param currentContent  当前正文内容
     * @return 优化后的草稿结果
     */
    AiInvocationResult<ArticleDraftResult> optimizeDraft(String instruction, String currentTitle, String currentSummary, String currentContent);

    /**
     * 从正文中提取文章摘要
     *
     * @param title   当前标题
     * @param content 当前正文内容
     * @return AI 提炼后的摘要
     */
    AiInvocationResult<ArticleSummaryResponse> extractSummary(String title, String content);

    /**
     * 对笔记本内容进行摘要
     *
     * @param messages        历史消息列表
     * @param currentNotebook 当前笔记本摘要
     * @return 更新后的笔记本摘要
     */
    AiInvocationResult<NotebookSummary> summarizeNotebook(List<AgentMessageDO> messages, NotebookSummary currentNotebook);
}
