package com.putl.agentservice.client.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.model.vo.AiInvocationResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class CodexResponsesExecutor {

    private final AnthropicProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public CodexResponsesExecutor(AnthropicProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs()))
                .build();
    }

    public AiInvocationResult<String> execute(ArticleAiRequest request) {
        Instant startedAt = Instant.now();
        try {
            HttpResponse<String> response = httpClient.send(
                    httpRequest(request, false),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            ensureSuccess(response.statusCode(), response.body());
            JsonNode root = objectMapper.readTree(response.body());
            int latencyMs = toInt(Duration.between(startedAt, Instant.now()).toMillis());
            return new AiInvocationResult<>(
                    extractText(root),
                    root.path("usage").path("input_tokens").asInt(0),
                    root.path("usage").path("output_tokens").asInt(0),
                    latencyMs,
                    root.path("model").asText(request.getModel()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Codex Responses 调用被中断", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("Codex Responses 调用失败: " + ex.getMessage(), ex);
        }
    }

    public AiInvocationResult<String> executeStream(ArticleAiRequest request,
                                                    Consumer<String> chunkConsumer,
                                                    StreamingControl streamingControl) {
        Instant startedAt = Instant.now();
        StringBuilder content = new StringBuilder();
        AtomicInteger tokenInput = new AtomicInteger(0);
        AtomicInteger tokenOutput = new AtomicInteger(0);
        AtomicReference<String> model = new AtomicReference<>(request.getModel());
        Consumer<String> safeChunkConsumer = chunkConsumer == null ? ignored -> { } : chunkConsumer;
        StreamingControl safeStreamingControl = streamingControl == null ? StreamingControl.noop() : streamingControl;
        safeStreamingControl.throwIfCancelled();
        try {
            HttpResponse<InputStream> response = httpClient.send(
                    httpRequest(request, true),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                ensureSuccess(response.statusCode(), readBody(response.body()));
            }
            try (InputStream body = response.body();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
                safeStreamingControl.onCancel(() -> closeQuietly(body));
                String line;
                while ((line = reader.readLine()) != null) {
                    safeStreamingControl.throwIfCancelled();
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring("data:".length()).trim();
                    if (!StringUtils.hasText(data) || "[DONE]".equals(data)) {
                        continue;
                    }
                    JsonNode event = objectMapper.readTree(data);
                    String type = event.path("type").asText();
                    if ("response.output_text.delta".equals(type)) {
                        String delta = event.path("delta").asText("");
                        if (StringUtils.hasText(delta)) {
                            content.append(delta);
                            safeChunkConsumer.accept(delta);
                        }
                        continue;
                    }
                    if ("response.completed".equals(type)) {
                        JsonNode completed = event.path("response");
                        tokenInput.set(completed.path("usage").path("input_tokens").asInt(tokenInput.get()));
                        tokenOutput.set(completed.path("usage").path("output_tokens").asInt(tokenOutput.get()));
                        model.set(completed.path("model").asText(model.get()));
                        continue;
                    }
                    if ("error".equals(type) || "response.failed".equals(type)) {
                        throw new IllegalStateException(extractError(event));
                    }
                }
            }
            safeStreamingControl.throwIfCancelled();
            if (!StringUtils.hasText(content.toString())) {
                throw new IllegalStateException("Codex Responses 流式响应未返回可解析的文本内容");
            }
            int latencyMs = toInt(Duration.between(startedAt, Instant.now()).toMillis());
            return new AiInvocationResult<>(content.toString(), tokenInput.get(), tokenOutput.get(), latencyMs, model.get());
        } catch (StreamingCancelledException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Codex Responses 流式调用被中断", ex);
        } catch (Exception ex) {
            if (safeStreamingControl.isCancelled()) {
                throw new StreamingCancelledException();
            }
            throw new IllegalStateException("Codex Responses 流式调用失败: " + ex.getMessage(), ex);
        }
    }

    private HttpRequest httpRequest(ArticleAiRequest request, boolean stream) throws Exception {
        validateApiKeyConfigured();
        String body = objectMapper.writeValueAsString(buildPayload(request, stream));
        return HttpRequest.newBuilder(responsesUri())
                .timeout(Duration.ofMillis(timeoutMs()))
                .header("Authorization", "Bearer " + properties.getCodex().getApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", stream ? "text/event-stream" : "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
    }

    private Map<String, Object> buildPayload(ArticleAiRequest request, boolean stream) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", request.getModel());
        payload.put("instructions", request.getSystemPrompt());
        payload.put("input", buildInput(request));
        payload.put("max_output_tokens", request.getMaxOutputTokens());
        payload.put("temperature", request.getTemperature());
        payload.put("store", Boolean.TRUE.equals(properties.getCodex().getStore()));
        payload.put("stream", stream);
        if (stream) {
            payload.put("stream_options", Map.of("include_obfuscation", false));
        }
        if (StringUtils.hasText(properties.getCodex().getReasoningEffort())) {
            payload.put("reasoning", Map.of("effort", properties.getCodex().getReasoningEffort()));
        }
        return payload;
    }

    private List<Map<String, String>> buildInput(ArticleAiRequest request) {
        List<Map<String, String>> input = new ArrayList<>();
        for (ArticleAiRequest.Message message : request.getMessages()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("role", toCodexRole(message.getRole()));
            item.put("content", message.getContent());
            input.add(item);
        }
        return input;
    }

    private String toCodexRole(ArticleAiRequest.Role role) {
        return role == ArticleAiRequest.Role.ASSISTANT ? "assistant" : "user";
    }

    private String extractText(JsonNode root) {
        List<String> chunks = new ArrayList<>();
        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode outputItem : output) {
                JsonNode content = outputItem.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode contentItem : content) {
                    if ("output_text".equals(contentItem.path("type").asText())) {
                        String text = contentItem.path("text").asText("");
                        if (StringUtils.hasText(text)) {
                            chunks.add(text.trim());
                        }
                    }
                }
            }
        }
        String text = chunks.stream().collect(Collectors.joining("\n"));
        if (!StringUtils.hasText(text)) {
            throw new IllegalStateException("Codex Responses 未返回可解析的文本内容");
        }
        return text;
    }

    private String extractError(JsonNode event) {
        JsonNode error = event.path("error");
        if (error.isMissingNode()) {
            error = event.path("response").path("error");
        }
        String message = error.path("message").asText();
        return StringUtils.hasText(message) ? message : "Codex Responses 返回错误事件";
    }

    private URI responsesUri() {
        String baseUrl = properties.getCodex().getBaseUrl();
        String normalized = StringUtils.hasText(baseUrl) ? baseUrl.stripTrailing() : "https://api.openai.com";
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String path = normalized.endsWith("/v1") ? "/responses" : "/v1/responses";
        return URI.create(normalized + path);
    }

    private void validateApiKeyConfigured() {
        if (!StringUtils.hasText(properties.getCodex().getApiKey())) {
            throw new IllegalStateException("OPENAI_API_KEY 未配置，无法调用 Codex Responses API");
        }
    }

    private void ensureSuccess(int statusCode, String body) {
        if (statusCode >= 400) {
            throw new IllegalStateException("HTTP " + statusCode + ": " + body);
        }
    }

    private String readBody(InputStream body) throws Exception {
        try (InputStream input = body) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void closeQuietly(InputStream body) {
        try {
            body.close();
        } catch (Exception ignored) {
        }
    }

    private long timeoutMs() {
        Integer configured = properties.getCodex().getTimeoutMs();
        return configured == null ? 300000L : Math.max(1L, configured);
    }

    private int toInt(long value) {
        if (value <= 0) {
            return 0;
        }
        return (int) Math.min(value, Integer.MAX_VALUE);
    }
}
