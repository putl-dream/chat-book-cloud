package com.putl.agentservice.client.engine;

import com.putl.agentservice.model.vo.AiInvocationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
public class AnthropicArticleAiExecutionEngine implements ArticleAiExecutionEngine {

    private final AnthropicExecutor anthropicExecutor;

    /**
     * 构造 Anthropic 执行引擎
     *
     * @param anthropicExecutor Anthropic SDK 执行器
     */
    public AnthropicArticleAiExecutionEngine(AnthropicExecutor anthropicExecutor) {
        this.anthropicExecutor = anthropicExecutor;
    }

    /**
     * 执行 AI 任务（非流式）
     *
     * @param task    任务策略对象
     * @param context AI 执行上下文
     * @param <T>     返回结果类型
     * @return AI 调用结果
     */
    @Override
    public <T> AiInvocationResult<T> execute(ArticleAiTask<T> task, ArticleAiContext context) {
        return execute(task, context, null);
    }

    /**
     * 执行 AI 任务（支持流式回调）
     *
     * @param task           任务策略对象
     * @param context        AI 执行上下文
     * @param chunkConsumer  内容块回调，用于处理流式响应
     * @param <T>            返回结果类型
     * @return AI 调用结果
     */
    @Override
    public <T> AiInvocationResult<T> execute(ArticleAiTask<T> task,
                                             ArticleAiContext context,
                                             Consumer<String> chunkConsumer) {
        return execute(task, context, chunkConsumer, StreamingControl.noop());
    }

    @Override
    public <T> AiInvocationResult<T> execute(ArticleAiTask<T> task,
                                             ArticleAiContext context,
                                             Consumer<String> chunkConsumer,
                                             StreamingControl streamingControl) {
        if (chunkConsumer != null && !task.supportsStreaming()) {
            throw new IllegalArgumentException("Task does not support streaming: " + task.taskCode());
        }
        AiInvocationResult<String> raw = chunkConsumer == null
                ? anthropicExecutor.execute(task.createParams(context))
                : anthropicExecutor.executeStream(task.createParams(context), chunkConsumer, streamingControl);
        T parsed = task.parseResponse(raw.getData());
        return new AiInvocationResult<>(
                parsed,
                raw.getTokenInput(),
                raw.getTokenOutput(),
                raw.getLatencyMs(),
                raw.getModel());
    }
}
