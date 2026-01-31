package com.putl.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.userservice.mapper.entity.MessageDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * [Message]表数据库访问层  消息
 *
 * @since 2025-01-16 12:26:36
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageDO> {

}

