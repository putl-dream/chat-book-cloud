package com.putl.agentservice.client.task;

import com.anthropic.models.messages.MessageCreateParams;
import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.AnthropicRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.PromptTemplateService;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.model.vo.NotebookSummary;
import org.springframework.stereotype.Component;

@Component
public class NotebookSummarizeTask extends AbstractAnthropicArticleAiTask<NotebookSummary> {

    /**
     * 构造笔记本摘要任务
     *
     * @param properties            配置属性
     * @param requestFactory        请求工厂
     * @param promptTemplateService 提示词模板服务
     * @param agentMessageAssembler 消息转换器
     * @param aiResponseParser      AI 响应解析器
     */
    public NotebookSummarizeTask(AnthropicProperties properties,
                                 AnthropicRequestFactory requestFactory,
                                 PromptTemplateService promptTemplateService,
                                 AgentMessageAssembler agentMessageAssembler,
                                 AiResponseParser aiResponseParser) {
        super(properties, requestFactory, promptTemplateService, agentMessageAssembler, aiResponseParser);
    }

    /**
     * 获取任务编码
     *
     * @return 任务编码
     */
    @Override
    public String taskCode() {
        return "notebook_summarize";
    }

    /**
     * 创建笔记本摘要请求参数
     *
     * @param context AI 执行上下文
     * @return MessageCreateParams
     */
    @Override
    public MessageCreateParams createParams(ArticleAiContext context) {
        return newRequest(
                properties.getAnthropic().getModel().getNotebook(),
                properties.getAnthropic().getMaxTokens().getNotebook(),
                0.1,
                loadTemplate(PromptTemplateConstants.NOTEBOOK_SUMMARIZE))
                .addUserMessage(buildNotebookPrompt(context))
                .build();
    }

    /**
     * 解析笔记本摘要响应
     *
     * @param rawText 原始响应文本
     * @return 解析后的笔记本摘要
     */
    @Override
    public NotebookSummary parseResponse(String rawText) {
        return aiResponseParser.parseJsonObject(rawText, NotebookSummary.class);
    }

    /**
     * 构建笔记本摘要提示词
     *
     * @param context AI 执行上下文
     * @return 渲染后的提示词
     */
    private String buildNotebookPrompt(ArticleAiContext context) {
        return "当前 notebook JSON:\n"
                + prettyJson(normalizeNotebook(context.getNotebookSummary()))
                + "\n\n会话记录:\n"
                + agentMessageAssembler.toTranscript(context.getMessages())
                + "\n\n请输出更新后的 notebook JSON。";
    }
}
