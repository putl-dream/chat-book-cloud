package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.articleservice.mapper.dto.AuthorTagHotStatDTO;
import com.putl.articleservice.mapper.entity.AuthorTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuthorTagMapper extends BaseMapper<AuthorTagDO> {

    @Select("""
            <script>
            SELECT t.id, t.name, COUNT(r.article_id) AS article_count
            FROM author_tag t
            LEFT JOIN article_author_tag_rel r ON r.author_tag_id = t.id
            WHERE t.status = 'ACTIVE'
            GROUP BY t.id, t.name
            ORDER BY article_count DESC, t.update_time DESC, t.id DESC
            LIMIT #{limit}
            </script>
            """)
    List<AuthorTagHotStatDTO> selectHotTags(@Param("limit") int limit);
}
