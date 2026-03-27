package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.articleservice.mapper.entity.AdminOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLogDO> {
}
