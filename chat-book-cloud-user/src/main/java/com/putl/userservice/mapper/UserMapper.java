package com.putl.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.userservice.mapper.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * [User]表数据库访问层
 *
 * @author putl
 * @since 2024-12-29 17:36:33
 */
@Mapper()
public interface UserMapper extends BaseMapper<UserDO> {

}

