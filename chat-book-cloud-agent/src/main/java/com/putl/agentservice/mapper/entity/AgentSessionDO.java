package com.putl.agentservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.AgentSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_session")
public class AgentSessionDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private AgentSceneType sceneType;

    private Integer targetArticleId;

    private Integer targetDraftId;

    private String title;

    private AgentSessionStatus status;

    private String notebookSummary;

    private String model;

    private String promptVersion;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
