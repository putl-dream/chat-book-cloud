package com.putl.agentservice.client.engine;

/**
 * AI文章任务接口
 * <p>定义了AI任务的基本行为规范，包括请求创建、响应解析等核心功能</p>
 *
 * @param <T> 任务返回结果的类型
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
public interface ArticleAiTask<T> {

    /**
     * 获取任务代码标识
     *
     * @return 任务的唯一标识符
     */
    String taskCode();

    /**
     * 根据上下文创建AI请求
     *
     * @param context AI执行上下文，包含所需的所有信息
     * @return 构建好的AI请求对象
     */
    ArticleAiRequest createRequest(ArticleAiContext context);

    /**
     * 解析AI原始响应文本为结构化数据
     *
     * @param rawText AI返回的原始文本内容
     * @return 解析后的结构化结果
     */
    T parseResponse(String rawText);

    /**
     * 判断当前任务是否支持流式输出
     *
     * @return true表示支持流式输出，false表示不支持
     */
    default boolean supportsStreaming() {
        return false;
    }
}
