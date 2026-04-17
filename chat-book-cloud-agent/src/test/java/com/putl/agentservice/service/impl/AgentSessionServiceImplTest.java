package com.putl.agentservice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.AgentSessionStatus;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.vo.AgentSessionDetailResponse;
import com.putl.agentservice.model.vo.AgentSessionPageResponse;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.agentservice.service.AgentNotebookService;
import com.putl.articleservice.api.ArticleClient;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.context.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentSessionServiceImplTest {

    @Mock
    private AgentSessionMapper agentSessionMapper;

    @Mock
    private AgentMessageMapper agentMessageMapper;

    @Mock
    private AgentNotebookService agentNotebookService;

    @Mock
    private AgentNotebookCacheService agentNotebookCacheService;

    @Mock
    private ArticleClient articleClient;

    private AgentSessionServiceImpl service;

    @BeforeEach
    void setUp() {
        AnthropicProperties properties = new AnthropicProperties();
        service = new AgentSessionServiceImpl(
                agentSessionMapper,
                agentMessageMapper,
                agentNotebookService,
                agentNotebookCacheService,
                properties,
                articleClient);
        UserContext.setUser(UserInfo.builder().userId("7").build());
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void getSessionPageShouldReturnMappedPageData() {
        AgentSessionDO record = AgentSessionDO.builder()
                .id(101)
                .title("Java 并发写作")
                .sceneType(AgentSceneType.LEARN)
                .status(AgentSessionStatus.ACTIVE)
                .targetDraftId(12)
                .createTime(LocalDateTime.of(2026, 4, 10, 10, 0))
                .updateTime(LocalDateTime.of(2026, 4, 11, 8, 30))
                .build();
        Page<AgentSessionDO> page = new Page<>(1, 12, 1);
        page.setRecords(List.of(record));
        when(agentSessionMapper.selectPage(any(Page.class), any())).thenReturn(page);

        AgentSessionPageResponse response = service.getSessionPage(1, 12, "并发");

        assertEquals(1L, response.getTotal());
        assertEquals(1, response.getList().size());
        assertEquals(101, response.getList().get(0).getId());
        assertEquals("Java 并发写作", response.getList().get(0).getTitle());
        assertEquals(AgentSceneType.LEARN, response.getList().get(0).getSceneType());
    }

    @Test
    void getSessionDetailShouldRejectForeignSession() {
        when(agentSessionMapper.selectOne(any())).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getSessionDetail(999));

        assertEquals("会话不存在或无权限访问", exception.getMessage());
    }

    @Test
    void getSessionDetailShouldLoadOwnedSession() {
        AgentSessionDO session = AgentSessionDO.builder()
                .id(102)
                .userId(7)
                .title("系统设计复盘")
                .sceneType(AgentSceneType.DISCUSS)
                .status(AgentSessionStatus.ACTIVE)
                .build();
        NotebookSummary notebook = NotebookSummary.builder()
                .goal("系统设计复盘")
                .build();
        when(agentSessionMapper.selectOne(any())).thenReturn(session);
        when(agentNotebookCacheService.getNotebook(anyInt())).thenReturn(notebook);
        when(agentMessageMapper.selectList(any())).thenReturn(List.of());

        AgentSessionDetailResponse response = service.getSessionDetail(102);

        assertEquals(102, response.getSession().getId());
        assertEquals("系统设计复盘", response.getSession().getTitle());
        assertEquals("系统设计复盘", response.getNotebook().getGoal());
        assertEquals(0, response.getMessages().size());
    }
}
