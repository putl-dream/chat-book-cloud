package com.putl.agentservice.client.engine;

import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.prompt.PromptTemplateLoader;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class PromptTemplateService {

    private final PromptTemplateLoader promptTemplateLoader;

    /**
     * 构造提示词模板服务
     *
     * @param promptTemplateLoader 提示词模板加载器
     */
    public PromptTemplateService(PromptTemplateLoader promptTemplateLoader) {
        this.promptTemplateLoader = promptTemplateLoader;
    }

    /**
     * 加载指定名称的提示词模板
     *
     * @param name 模板名称
     * @return 模板内容字符串
     */
    public String load(String name) {
        return promptTemplateLoader.load(name);
    }

    /**
     * 渲染提示词模板，替换占位符
     *
     * @param templateName 模板名称
     * @param variables    变量 map，占位符格式为 {{key}}
     * @return 渲染后的字符串
     */
    public String render(String templateName, Map<String, String> variables) {
        String rendered = load(templateName);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", defaultText(entry.getValue()));
        }
        return rendered;
    }

    /**
     * 将对象格式化为带缩进的 JSON 字符串
     *
     * @param value 待格式化的对象
     * @return 美化的 JSON 字符串
     */
    public String prettyJson(Object value) {
        try {
            return JsonUtil.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception ex) {
            return JsonUtil.toJsonString(value);
        }
    }

    /**
     * 标准化笔记本摘要，空值时返回空对象
     *
     * @param notebookSummary 笔记本摘要
     * @return 标准化后的笔记本摘要
     */
    public NotebookSummary normalizeNotebook(NotebookSummary notebookSummary) {
        if (notebookSummary != null) {
            return notebookSummary;
        }
        return NotebookSummary.builder().build();
    }

    /**
     * 安全获取字符串值
     *
     * @param value 原始值
     * @return 字符串值，null 时返回空字符串
     */
    public String defaultText(String value) {
        return Objects.toString(value, "");
    }
}
