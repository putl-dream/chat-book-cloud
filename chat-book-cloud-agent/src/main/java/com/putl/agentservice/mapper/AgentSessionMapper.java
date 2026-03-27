package com.putl.agentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentSessionMapper extends BaseMapper<AgentSessionDO> {
}
