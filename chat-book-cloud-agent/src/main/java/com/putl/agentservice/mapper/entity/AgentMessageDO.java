package com.putl.agentservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.putl.agentservice.enums.AgentMessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("agent_message")
public class AgentMessageDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer sessionId;

    private AgentMessageRole role;

    private String messageType;

    private String content;

    private String payload;

    private Integer tokenInput;

    private Integer tokenOutput;

    private Integer latencyMs;

    private LocalDateTime createTime;
}
