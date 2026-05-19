package com.putl.agentservice.client.engine;

import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.NotebookSummary;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * AI文章执行上下文
 * <p>封装AI任务执行所需的所有上下文信息，包括消息历史、笔记本摘要、场景类型等</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@Getter
@Builder
public class ArticleAiContext {

    /**
     * 历史消息列表，包含用户和助手的对话记录
     */
    private final List<AgentMessageDO> messages;

    /**
     * 笔记本摘要信息，提供相关背景知识
     */
    private final NotebookSummary notebookSummary;

    /**
     * 代理场景类型，决定使用哪种处理策略
     */
    private final AgentSceneType sceneType;

    /**
     * 指令内容，用于优化或特定任务
     */
    private final String instruction;

    /**
     * 当前文章标题
     */
    private final String currentTitle;

    /**
     * 当前文章摘要
     */
    private final String currentSummary;

    /**
     * 当前文章内容
     */
    private final String currentContent;
}
