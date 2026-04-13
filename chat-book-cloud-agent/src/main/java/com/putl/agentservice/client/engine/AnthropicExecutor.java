package com.putl.agentservice.client.engine;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageDeltaUsage;
import com.anthropic.models.messages.RawContentBlockDelta;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.putl.agentservice.model.vo.AiInvocationResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class AnthropicExecutor {

    private final AnthropicClient anthropicClient;

    /**
     * 构造 Anthropic 执行器
     *
     * @param anthropicClient Anthropic SDK 客户端
     */
    public AnthropicExecutor(AnthropicClient anthropicClient) {
        this.anthropicClient = anthropicClient;
    }

    /**
     * 执行非流式 AI 调用
     *
     * @param params 请求参数
     * @return AI 调用结果，包含响应文本、token 消耗和延迟
     */
    public AiInvocationResult<String> execute(MessageCreateParams params) {
        Instant startedAt = Instant.now();
        Message response = anthropicClient.messages().create(params);
        int latencyMs = toInt(Duration.between(startedAt, Instant.now()).toMillis());
        return new AiInvocationResult<>(
                extractText(response),
                toInt(response.usage().inputTokens()),
                toInt(response.usage().outputTokens()),
                latencyMs,
                response.model().asString());
    }

    /**
     * 执行流式 AI 调用
     *
     * @param params         请求参数
     * @param chunkConsumer  内容块回调，用于处理流式响应
     * @return AI 调用结果
     */
    public AiInvocationResult<String> executeStream(MessageCreateParams params, Consumer<String> chunkConsumer) {
        return executeStream(params, chunkConsumer, StreamingControl.noop());
    }

    public AiInvocationResult<String> executeStream(MessageCreateParams params,
                                                    Consumer<String> chunkConsumer,
                                                    StreamingControl streamingControl) {
        Instant startedAt = Instant.now();
        StringBuilder content = new StringBuilder();
        AtomicInteger tokenInput = new AtomicInteger(0);
        AtomicInteger tokenOutput = new AtomicInteger(0);
        AtomicReference<String> model = new AtomicReference<>(params.model().asString());
        Consumer<String> safeChunkConsumer = chunkConsumer == null ? ignored -> { } : chunkConsumer;
        StreamingControl safeStreamingControl = streamingControl == null ? StreamingControl.noop() : streamingControl;
        safeStreamingControl.throwIfCancelled();
        try (StreamResponse<RawMessageStreamEvent> streamResponse = anthropicClient.messages().createStreaming(params)) {
            safeStreamingControl.onCancel(streamResponse::close);
            var eventIterator = streamResponse.stream().iterator();
            while (eventIterator.hasNext()) {
                safeStreamingControl.throwIfCancelled();
                RawMessageStreamEvent event = eventIterator.next();
                safeStreamingControl.throwIfCancelled();
                if (event.isMessageStart()) {
                    Message message = event.asMessageStart().message();
                    tokenInput.set(toInt(message.usage().inputTokens()));
                    tokenOutput.set(toInt(message.usage().outputTokens()));
                    model.set(message.model().asString());
                    continue;
                }
                if (event.isMessageDelta()) {
                    MessageDeltaUsage usage = event.asMessageDelta().usage();
                    tokenOutput.set(toInt(usage.outputTokens()));
                    if (usage.inputTokens().isPresent()) {
                        tokenInput.set(toInt(usage.inputTokens().get()));
                    }
                    continue;
                }
                if (!event.isContentBlockDelta()) {
                    continue;
                }
                RawContentBlockDelta delta = event.asContentBlockDelta().delta();
                if (!delta.isText()) {
                    continue;
                }
                String chunk = delta.asText().text();
                if (!StringUtils.hasText(chunk)) {
                    continue;
                }
                content.append(chunk);
                safeChunkConsumer.accept(chunk);
            }
        } catch (StreamingCancelledException ex) {
            throw ex;
        } catch (Exception ex) {
            if (safeStreamingControl.isCancelled()) {
                throw new StreamingCancelledException();
            }
            throw ex;
        }
        safeStreamingControl.throwIfCancelled();
        int latencyMs = toInt(Duration.between(startedAt, Instant.now()).toMillis());
        if (!StringUtils.hasText(content.toString())) {
            throw new IllegalStateException("Anthropic 流式响应未返回可解析的文本内容");
        }
        return new AiInvocationResult<>(content.toString(), tokenInput.get(), tokenOutput.get(), latencyMs, model.get());
    }

    /**
     * 从响应消息中提取文本内容
     *
     * @param response Anthropic 消息响应
     * @return 提取的文本内容
     */
    private String extractText(Message response) {
        String text = response.content().stream()
                .filter(ContentBlock::isText)
                .map(ContentBlock::asText)
                .map(block -> block.text().trim())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"));
        if (!StringUtils.hasText(text)) {
            throw new IllegalStateException("Anthropic 未返回可解析的文本内容");
        }
        return text;
    }

    /**
     * 将 long 类型 token 数转换为 int（安全处理溢出）
     *
     * @param value long 类型值
     * @return int 类型值
     */
    private int toInt(long value) {
        if (value <= 0) {
            return 0;
        }
        return (int) Math.min(value, Integer.MAX_VALUE);
    }
}
