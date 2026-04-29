package com.putl.agentservice.client.task;

import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.ArticleAiRequest;
import com.putl.agentservice.client.engine.ArticleAiRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.PromptTemplateService;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.model.vo.ArticleSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class ArticleSummaryExtractTask extends AbstractAnthropicArticleAiTask<ArticleSummaryResponse> {

    /**
     * 构造文章摘要提取任务
     *
     * @param properties            配置属性
     * @param requestFactory        请求工厂
     * @param promptTemplateService 提示词模板服务
     * @param agentMessageAssembler 消息转换器
     * @param aiResponseParser      AI 响应解析器
     */
    public ArticleSummaryExtractTask(AnthropicProperties properties,
                                     ArticleAiRequestFactory requestFactory,
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
        return "article_summary";
    }

    /**
     * 创建摘要提取请求参数
     *
     * @param context AI 执行上下文
     * @return ArticleAiRequest
     */
    @Override
    public ArticleAiRequest createRequest(ArticleAiContext context) {
        return newRequest(
                optimizeModel(),
                optimizeMaxTokens(),
                0.1,
                loadTemplate(PromptTemplateConstants.ARTICLE_SUMMARY))
                .addUserMessage(buildSummaryPrompt(context))
                .build();
    }

    /**
     * 解析摘要提取响应
     *
     * @param rawText 原始响应文本
     * @return 解析后的摘要响应
     */
    @Override
    public ArticleSummaryResponse parseResponse(String rawText) {
        return aiResponseParser.parseJsonObject(rawText, ArticleSummaryResponse.class);
    }

    /**
     * 构建摘要提取提示词
     *
     * @param context AI 执行上下文
     * @return 渲染后的提示词
     */
    private String buildSummaryPrompt(ArticleAiContext context) {
        return "当前标题:\n"
                + text(context.getCurrentTitle())
                + "\n\n当前正文:\n"
                + text(context.getCurrentContent())
                + "\n\n请输出文章摘要 JSON。";
    }
}
