package com.putl.agentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.AgentSessionStatus;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.dto.CreateAgentSessionRequest;
import com.putl.agentservice.model.vo.AgentSessionCreateResponse;
import com.putl.agentservice.model.vo.AgentSessionDetailResponse;
import com.putl.agentservice.model.vo.AgentSessionListItemResponse;
import com.putl.agentservice.model.vo.AgentSessionPageResponse;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.agentservice.service.AgentNotebookService;
import com.putl.agentservice.service.AgentSessionService;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AgentSessionServiceImpl implements AgentSessionService {

    private final AgentSessionMapper agentSessionMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final AgentNotebookService agentNotebookService;
    private final AgentNotebookCacheService agentNotebookCacheService;
    private final AnthropicProperties anthropicProperties;
    private final ArticleClient articleClient;

    public AgentSessionServiceImpl(AgentSessionMapper agentSessionMapper,
                                   AgentMessageMapper agentMessageMapper,
                                   AgentNotebookService agentNotebookService,
                                   AgentNotebookCacheService agentNotebookCacheService,
                                   AnthropicProperties anthropicProperties,
                                   ArticleClient articleClient) {
        this.agentSessionMapper = agentSessionMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.agentNotebookService = agentNotebookService;
        this.agentNotebookCacheService = agentNotebookCacheService;
        this.anthropicProperties = anthropicProperties;
        this.articleClient = articleClient;
    }

    @Override
    public AgentSessionCreateResponse createSession(CreateAgentSessionRequest request) {
        String sessionTitle = request != null && StringUtils.hasText(request.getTitle())
                ? request.getTitle().trim()
                : "新的 AI 创作会话";
        AgentSceneType initialScene = AgentSceneType.initialScene(
                request == null ? null : request.getSceneType(),
                request == null ? null : request.getTargetDraftId());
        NotebookSummary notebook = agentNotebookService.initializeNotebook(
                sessionTitle,
                initialScene);
        AgentSessionDO session = AgentSessionDO.builder()
                .userId(currentUserId())
                .sceneType(initialScene)
                .targetArticleId(request == null ? null : request.getTargetArticleId())
                .targetDraftId(request == null ? null : request.getTargetDraftId())
                .title(sessionTitle)
                .status(AgentSessionStatus.ACTIVE)
                .notebookSummary(JsonUtil.toJsonString(notebook))
                .model(anthropicProperties.activeModel().getChat())
                .promptVersion("v1")
                .build();
        agentSessionMapper.insert(session);
        agentNotebookCacheService.cacheNotebook(session.getId(), notebook);
        return AgentSessionCreateResponse.builder()
                .sessionId(session.getId())
                .build();
    }

    @Override
    public AgentSessionDetailResponse getSessionDetail(Integer sessionId) {
        AgentSessionDO session = requireOwnedSession(sessionId);
        NotebookSummary notebook = session == null ? NotebookSummary.builder().build()
                : agentNotebookCacheService.getNotebook(sessionId);
        List<AgentMessageDO> messages = agentMessageMapper.selectList(Wrappers.<AgentMessageDO>lambdaQuery()
                .eq(AgentMessageDO::getSessionId, sessionId)
                .orderByAsc(AgentMessageDO::getId));
        DraftDetailDTO draft = null;
        if (session != null && session.getTargetDraftId() != null) {
            CommonResult<DraftDetailDTO> draftResult = articleClient.getDraftDetail(session.getTargetDraftId());
            if (draftResult != null && draftResult.isSuccess()) {
                draft = draftResult.getData();
            }
        }
        return AgentSessionDetailResponse.builder()
                .session(session)
                .messages(messages)
                .draft(draft)
                .notebook(notebook)
                .build();
    }

    @Override
    public AgentSessionPageResponse getSessionPage(Integer pageNo, Integer pageSize, String keyword) {
        int normalizedPageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 12 : Math.min(pageSize, 50);
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        Page<AgentSessionDO> page = agentSessionMapper.selectPage(
                new Page<>(normalizedPageNo, normalizedPageSize),
                Wrappers.<AgentSessionDO>lambdaQuery()
                        .eq(AgentSessionDO::getUserId, currentUserId())
                        .like(StringUtils.hasText(normalizedKeyword), AgentSessionDO::getTitle, normalizedKeyword)
                        .orderByDesc(AgentSessionDO::getUpdateTime)
                        .orderByDesc(AgentSessionDO::getId));

        List<AgentSessionListItemResponse> list = page.getRecords().stream()
                .map(this::toSessionListItem)
                .toList();
        return AgentSessionPageResponse.builder()
                .list(list)
                .total(page.getTotal())
                .build();
    }

    private Integer currentUserId() {
        String userId = UserContext.getUserId();
        return (userId == null || userId.isBlank()) ? 0 : Integer.parseInt(userId);
    }

    private AgentSessionDO requireOwnedSession(Integer sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("会话不存在或已失效");
        }
        AgentSessionDO session = agentSessionMapper.selectOne(Wrappers.<AgentSessionDO>lambdaQuery()
                .eq(AgentSessionDO::getId, sessionId)
                .eq(AgentSessionDO::getUserId, currentUserId())
                .last("limit 1"));
        if (session == null) {
            throw new IllegalArgumentException("会话不存在或无权限访问");
        }
        return session;
    }

    private AgentSessionListItemResponse toSessionListItem(AgentSessionDO session) {
        return AgentSessionListItemResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .sceneType(session.getSceneType())
                .status(session.getStatus())
                .targetDraftId(session.getTargetDraftId())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }
}
