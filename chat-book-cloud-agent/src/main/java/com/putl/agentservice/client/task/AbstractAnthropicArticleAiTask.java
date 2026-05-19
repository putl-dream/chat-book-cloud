package com.putl.agentservice.client.task;

import com.putl.agentservice.client.engine.AgentMessageAssembler;
import com.putl.agentservice.client.engine.AiResponseParser;
import com.putl.agentservice.client.engine.ArticleAiRequest;
import com.putl.agentservice.client.engine.ArticleAiRequestFactory;
import com.putl.agentservice.client.engine.ArticleAiTask;
import com.putl.agentservice.client.engine.PromptTemplateService;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.model.vo.NotebookSummary;

import java.util.Map;

abstract class AbstractAnthropicArticleAiTask<T> implements ArticleAiTask<T> {

    protected final AnthropicProperties properties;
    protected final ArticleAiRequestFactory requestFactory;
    protected final PromptTemplateService promptTemplateService;
    protected final AgentMessageAssembler agentMessageAssembler;
    protected final AiResponseParser aiResponseParser;

    /**
     * 构造抽象任务基类
     *
     * @param properties            配置属性
     * @param requestFactory        请求工厂
     * @param promptTemplateService 提示词模板服务
     * @param agentMessageAssembler 消息转换器
     * @param aiResponseParser      AI 响应解析器
     */
    protected AbstractAnthropicArticleAiTask(AnthropicProperties properties,
                                             ArticleAiRequestFactory requestFactory,
                                             PromptTemplateService promptTemplateService,
                                             AgentMessageAssembler agentMessageAssembler,
                                             AiResponseParser aiResponseParser) {
        this.properties = properties;
        this.requestFactory = requestFactory;
        this.promptTemplateService = promptTemplateService;
        this.agentMessageAssembler = agentMessageAssembler;
        this.aiResponseParser = aiResponseParser;
    }

    /**
     * 创建新的请求构建器
     *
     * @param model        模型名称
     * @param maxTokens    最大 token 数
     * @param temperature  温度参数
     * @param systemPrompt 系统提示词
     * @return ArticleAiRequest 构建器
     */
    protected ArticleAiRequest.Builder newRequest(String model,
                                                  Integer maxTokens,
                                                  double temperature,
                                                  String systemPrompt) {
        return requestFactory.newRequest(model, maxTokens, temperature, systemPrompt);
    }

    /**
     * 加载提示词模板
     *
     * @param name 模板名称
     * @return 模板内容
     */
    protected String loadTemplate(String name) {
        return promptTemplateService.load(name);
    }

    /**
     * 渲染提示词模板
     *
     * @param templateName 模板名称
     * @param variables    变量 map
     * @return 渲染后的字符串
     */
    protected String renderTemplate(String templateName, Map<String, String> variables) {
        return promptTemplateService.render(templateName, variables);
    }

    /**
     * 格式化对象为美化 JSON
     *
     * @param value 待格式化对象
     * @return JSON 字符串
     */
    protected String prettyJson(Object value) {
        return promptTemplateService.prettyJson(value);
    }

    /**
     * 标准化笔记本摘要
     *
     * @param notebookSummary 笔记本摘要
     * @return 标准化后的笔记本摘要
     */
    protected NotebookSummary normalizeNotebook(NotebookSummary notebookSummary) {
        return promptTemplateService.normalizeNotebook(notebookSummary);
    }

    protected String chatModel() {
        return properties.activeModel().getChat();
    }

    protected String generateModel() {
        return properties.activeModel().getGenerate();
    }

    protected String optimizeModel() {
        return properties.activeModel().getOptimize();
    }

    protected String notebookModel() {
        return properties.activeModel().getNotebook();
    }

    protected Integer chatMaxTokens() {
        return properties.activeMaxTokens().getChat();
    }

    protected Integer generateMaxTokens() {
        return properties.activeMaxTokens().getGenerate();
    }

    protected Integer optimizeMaxTokens() {
        return properties.activeMaxTokens().getOptimize();
    }

    protected Integer notebookMaxTokens() {
        return properties.activeMaxTokens().getNotebook();
    }

    /**
     * 安全获取字符串值
     *
     * @param value 原始值
     * @return 字符串值
     */
    protected String text(String value) {
        return promptTemplateService.defaultText(value);
    }
}
