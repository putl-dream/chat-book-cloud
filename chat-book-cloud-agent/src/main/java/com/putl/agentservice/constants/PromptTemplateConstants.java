package com.putl.agentservice.constants;

/**
 * Prompt 模板常量，集中管理所有 AI 模板文件名。
 * 每次模板迭代时只需在此文件更新版本号。
 */
public final class PromptTemplateConstants {

    private PromptTemplateConstants() {
    }

    /**
     * 多轮对话模板，用于 chat() 方法。
     */
    public static final String ARTICLE_CHAT = "article_chat_v1.txt";

    /**
     * 草稿生成模板，用于 generateDraft() 方法。
     * 要求模型输出 JSON {title, summary, content}
     */
    public static final String ARTICLE_GENERATE = "article_generate_v1.txt";

    /**
     * 草稿优化模板，用于 optimizeDraft() 方法。
     * 要求模型输出 JSON {title, summary, content}
     */
    public static final String ARTICLE_OPTIMIZE = "article_optimize_v1.txt";

    /**
     * Notebook 摘要压缩模板，用于 summarizeNotebook() 方法。
     * 要求模型输出 JSON NotebookSummary 格式
     */
    public static final String NOTEBOOK_SUMMARIZE = "notebook_summarize_v1.txt";

    /**
     * 正文摘要提取模板，用于 extractSummary() 方法。
     * 要求模型输出 JSON {summary}
     */
    public static final String ARTICLE_SUMMARY = "article_summary_v1.txt";
}
