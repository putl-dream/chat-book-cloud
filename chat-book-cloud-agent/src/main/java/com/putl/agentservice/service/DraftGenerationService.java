package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.GenerateDraftRequest;
import com.putl.agentservice.model.vo.DraftGenerateResponse;

/**
 * 草稿生成服务接口
 * <p>提供基于AI的文章草稿生成功能，支持同步和WebSocket两种调用方式</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
public interface DraftGenerationService {

    /**
     * 同步生成文章草稿
     *
     * @param request 草稿生成请求参数
     * @return 生成的草稿内容
     */
    DraftGenerateResponse generateDraft(GenerateDraftRequest request);

    /**
     * 通过WebSocket生成文章草稿（流式）
     *
     * @param userId 用户ID
     * @param request 草稿生成请求参数
     */
    void generateDraftByWebSocket(String userId, GenerateDraftRequest request);

    /**
     * 取消WebSocket草稿生成
     *
     * @param userId 用户ID
     * @param request 草稿生成请求参数
     */
    void cancelDraftGenerationByWebSocket(String userId, GenerateDraftRequest request);
}
