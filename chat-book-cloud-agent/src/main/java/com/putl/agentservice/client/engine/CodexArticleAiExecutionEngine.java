package com.putl.agentservice.client.engine;

import com.putl.agentservice.config.CodexProviderCondition;
import com.putl.agentservice.model.vo.AiInvocationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@Conditional(CodexProviderCondition.class)
public class CodexArticleAiExecutionEngine implements ArticleAiExecutionEngine {

    private final CodexResponsesExecutor codexResponsesExecutor;

    public CodexArticleAiExecutionEngine(CodexResponsesExecutor codexResponsesExecutor) {
        this.codexResponsesExecutor = codexResponsesExecutor;
    }

    @Override
    public <T> AiInvocationResult<T> execute(ArticleAiTask<T> task, ArticleAiContext context) {
        return execute(task, context, null, StreamingControl.noop());
    }

    @Override
    public <T> AiInvocationResult<T> execute(ArticleAiTask<T> task,
                                             ArticleAiContext context,
                                             Consumer<String> chunkConsumer,
                                             StreamingControl streamingControl) {
        if (chunkConsumer != null && !task.supportsStreaming()) {
            throw new IllegalArgumentException("Task does not support streaming: " + task.taskCode());
        }
        ArticleAiRequest request = task.createRequest(context);
        AiInvocationResult<String> raw = chunkConsumer == null
                ? codexResponsesExecutor.execute(request)
                : codexResponsesExecutor.executeStream(request, chunkConsumer, streamingControl);
        T parsed = task.parseResponse(raw.getData());
        return new AiInvocationResult<>(
                parsed,
                raw.getTokenInput(),
                raw.getTokenOutput(),
                raw.getLatencyMs(),
                raw.getModel());
    }
}
