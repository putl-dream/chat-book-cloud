package com.putl.agentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AgentMessageMapper extends BaseMapper<AgentMessageDO> {

    @Select("""
            <script>
            SELECT id, session_id, role, message_type, content, payload, token_input, token_output, latency_ms, create_time
            FROM (
                SELECT id, session_id, role, message_type, content, payload, token_input, token_output, latency_ms, create_time
                FROM agent_message
                WHERE session_id = #{sessionId}
                ORDER BY id DESC
                LIMIT #{limit}
            ) recent_messages
            ORDER BY id ASC
            </script>
            """)
    List<AgentMessageDO> selectRecentMessages(@Param("sessionId") Integer sessionId, @Param("limit") int limit);
}
