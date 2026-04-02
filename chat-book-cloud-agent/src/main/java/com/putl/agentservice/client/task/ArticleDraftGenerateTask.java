package com.putl.agentservice.client.task;

import com.anthropic.models.messages.MessageCreateParams;
import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.AnthropicRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.PromptTemplateService;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import org.springframework.stereotype.Component;

@Component
public class ArticleDraftGenerateTask extends AbstractAnthropicArticleAiTask<ArticleDraftResult> {

    /**
     * 构造文章草稿生成任务
     *
     * @param properties            配置属性
     * @param requestFactory        请求工厂
     * @param promptTemplateService 提示词模板服务
     * @param agentMessageAssembler 消息转换器
     * @param aiResponseParser      AI 响应解析器
     */
    public ArticleDraftGenerateTask(AnthropicProperties properties,
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
        return "article_generate";
    }

    /**
     * 当前任务支持流式响应
     *
     * @return true
     */
    @Override
    public boolean supportsStreaming() {
        return true;
    }

    /**
     * 创建草稿生成请求参数
     *
     * @param context AI 执行上下文
     * @return MessageCreateParams
     */
    @Override
    public MessageCreateParams createParams(ArticleAiContext context) {
        return newRequest(
                properties.getAnthropic().getModel().getGenerate(),
                properties.getAnthropic().getMaxTokens().getGenerate(),
                0.2,
                loadTemplate(PromptTemplateConstants.ARTICLE_GENERATE))
                .addUserMessage(buildGeneratePrompt(context))
                .build();
    }

    /**
     * 解析草稿生成响应
     *
     * @param rawText 原始响应文本
     * @return 解析后的草稿结果
     */
    @Override
    public ArticleDraftResult parseResponse(String rawText) {
        return aiResponseParser.parseJsonObject(rawText, ArticleDraftResult.class);
    }

    /**
     * 构建草稿生成提示词
     *
     * @param context AI 执行上下文
     * @return 渲染后的提示词
     */
    private String buildGeneratePrompt(ArticleAiContext context) {
        return "Notebook 摘要 JSON:\n"
                + prettyJson(normalizeNotebook(context.getNotebookSummary()))
                + "\n\n会话记录:\n"
                + agentMessageAssembler.toTranscript(context.getMessages())
                + "\n\n请基于以上内容生成一版可预览文章草稿，只返回 JSON。";
    }
}
