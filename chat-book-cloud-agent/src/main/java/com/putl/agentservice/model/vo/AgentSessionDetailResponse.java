package com.putl.agentservice.model.vo;

import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话详情响应")
public class AgentSessionDetailResponse {

    @Schema(description = "会话信息")
    private AgentSessionDO session;

    @Schema(description = "消息列表")
    private List<AgentMessageDO> messages;

    @Schema(description = "当前草稿")
    private DraftDetailDTO draft;

    @Schema(description = "当前 notebook")
    private NotebookSummary notebook;
}
