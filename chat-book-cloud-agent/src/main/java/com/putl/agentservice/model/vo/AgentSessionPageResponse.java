package com.putl.agentservice.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "历史会话分页结果")
public class AgentSessionPageResponse {

    @Builder.Default
    @Schema(description = "会话列表")
    private List<AgentSessionListItemResponse> list = new ArrayList<>();

    @Schema(description = "总数")
    private Long total;

    public static AgentSessionPageResponse empty() {
        return AgentSessionPageResponse.builder()
                .list(new ArrayList<>())
                .total(0L)
                .build();
    }
}
