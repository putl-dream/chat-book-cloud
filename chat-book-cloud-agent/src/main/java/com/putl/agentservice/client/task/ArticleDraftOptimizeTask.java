package com.putl.agentservice.client.task;

import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.ArticleAiRequest;
import com.putl.agentservice.client.engine.ArticleAiRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.client.engine.PromptTemplateService;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import org.springframework.stereotype.Component;

@Component
public class ArticleDraftOptimizeTask extends AbstractAnthropicArticleAiTask<ArticleDraftResult> {

    /**
     * 构造文章草稿优化任务
     *
     * @param properties            配置属性
     * @param requestFactory        请求工厂
     * @param promptTemplateService 提示词模板服务
     * @param agentMessageAssembler 消息转换器
     * @param aiResponseParser      AI 响应解析器
     */
    public ArticleDraftOptimizeTask(AnthropicProperties properties,
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
        return "article_optimize";
    }

    /**
     * 创建草稿优化请求参数
     *
     * @param context AI 执行上下文
     * @return ArticleAiRequest
     */
    @Override
    public ArticleAiRequest createRequest(ArticleAiContext context) {
        return newRequest(
                optimizeModel(),
                optimizeMaxTokens(),
                0.2,
                loadTemplate(PromptTemplateConstants.ARTICLE_OPTIMIZE))
                .addUserMessage(buildOptimizePrompt(context))
                .build();
    }

    /**
     * 解析草稿优化响应
     *
     * @param rawText 原始响应文本
     * @return 解析后的草稿结果
     */
    @Override
    public ArticleDraftResult parseResponse(String rawText) {
        return aiResponseParser.parseJsonObject(rawText, ArticleDraftResult.class);
    }

    /**
     * 构建草稿优化提示词
     *
     * @param context AI 执行上下文
     * @return 渲染后的提示词
     */
    private String buildOptimizePrompt(ArticleAiContext context) {
        return "优化指令:\n"
                + text(context.getInstruction())
                + "\n\n当前标题:\n"
                + text(context.getCurrentTitle())
                + "\n\n当前摘要:\n"
                + text(context.getCurrentSummary())
                + "\n\n当前正文 Markdown:\n"
                + text(context.getCurrentContent())
                + "\n\n请基于以上内容输出优化后的完整 JSON。";
    }
}
