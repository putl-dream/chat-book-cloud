package com.putl.interactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.interactionservice.entity.ArticleStatDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleStatMapper extends BaseMapper<ArticleStatDO> {

    @Select("""
        SELECT id,
               article_id,
               view_count,
               praise_count,
               comment_count,
               collect_count,
               create_time,
               update_time
        FROM article_stat
        ORDER BY (COALESCE(view_count, 0)
                + COALESCE(praise_count, 0) * 3
                + COALESCE(comment_count, 0) * 4
                + COALESCE(collect_count, 0) * 5) DESC,
                 article_id DESC
        """)
    List<ArticleStatDO> selectAllForHotRank();
}
