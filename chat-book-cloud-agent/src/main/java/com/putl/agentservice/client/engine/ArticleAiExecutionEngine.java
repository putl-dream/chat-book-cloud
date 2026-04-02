package com.putl.agentservice.client.engine;

import com.putl.agentservice.model.vo.AiInvocationResult;

import java.util.function.Consumer;

public interface ArticleAiExecutionEngine {

    /**
     * 执行 AI 任务（非流式）
     *
     * @param task     任务策略对象
     * @param context  AI 执行上下文
     * @param <T>      返回结果类型
     * @return AI 调用结果，包含解析后的数据、token 消耗和延迟
     */
    <T> AiInvocationResult<T> execute(ArticleAiTask<T> task, ArticleAiContext context);

    /**
     * 执行 AI 任务（流式）
     *
     * @param task           任务策略对象
     * @param context        AI 执行上下文
     * @param chunkConsumer  内容块回调，用于处理流式响应
     * @param <T>            返回结果类型
     * @return AI 调用结果
     */
    <T> AiInvocationResult<T> execute(ArticleAiTask<T> task, ArticleAiContext context, Consumer<String> chunkConsumer);
}
