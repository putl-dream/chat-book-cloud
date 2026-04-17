package com.putl.agentservice.client.task;

import com.anthropic.models.messages.MessageCreateParams;
import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.AnthropicRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiContext;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.constants.PromptTemplateConstants;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ArticleChatTask extends AbstractAnthropicArticleAiTask<AgentAssistantMessage> {

    /**
     * 构造文章对话任务
     *
     * @param properties            配置属性
     * @param requestFactory        请求工厂
     * @param promptTemplateService 提示词模板服务
     * @param agentMessageAssembler 消息转换器
     * @param aiResponseParser      AI 响应解析器
     */
    public ArticleChatTask(AnthropicProperties properties,
                           AnthropicRequestFactory requestFactory,
                           com.putl.agentservice.client.engine.PromptTemplateService promptTemplateService,
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
        return "article_discuss";
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    /**
     * 创建对话请求参数
     *
     * @param context AI 执行上下文
     * @return MessageCreateParams
     */
    @Override
    public MessageCreateParams createParams(ArticleAiContext context) {
        MessageCreateParams.Builder builder = newRequest(
                properties.getAnthropic().getModel().getChat(),
                properties.getAnthropic().getMaxTokens().getChat(),
                0.2,
                renderTemplate(PromptTemplateConstants.ARTICLE_DISCUSS, Map.of(
                        "notebook_json", prettyJson(normalizeNotebook(context.getNotebookSummary()))
                )));
        agentMessageAssembler.toAnthropicMessages(context.getMessages()).forEach(builder::addMessage);
        return builder.build();
    }

    /**
     * 解析对话响应
     *
     * @param rawText 原始响应文本
     * @return 解析后的助手消息
     */
    @Override
    public AgentAssistantMessage parseResponse(String rawText) {
        return aiResponseParser.parseStructuredChat(rawText);
    }
}
