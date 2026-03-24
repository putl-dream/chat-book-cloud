package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.articleservice.mapper.entity.TagDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签 Mapper 接口
 */
@Mapper
public interface TagMapper extends BaseMapper<TagDO> {

}
