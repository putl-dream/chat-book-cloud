package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.articleservice.mapper.entity.AuthorTagSystemTagMapDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuthorTagSystemTagMapMapper extends BaseMapper<AuthorTagSystemTagMapDO> {

    @Select("""
            <script>
            SELECT DISTINCT system_tag_id
            FROM author_tag_system_tag_map
            WHERE status = 'ACTIVE'
              AND author_tag_id IN
            <foreach collection='authorTagIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<Integer> selectActiveSystemTagIdsByAuthorTagIds(@Param("authorTagIds") List<Integer> authorTagIds);

    @Delete("DELETE FROM author_tag_system_tag_map WHERE author_tag_id = #{authorTagId}")
    void deleteByAuthorTagId(@Param("authorTagId") Integer authorTagId);

    @Insert("""
            <script>
            INSERT INTO author_tag_system_tag_map (author_tag_id, system_tag_id, source, confidence, status)
            VALUES
            <foreach collection='list' item='item' separator=','>
              (#{item.authorTagId}, #{item.systemTagId}, #{item.source}, #{item.confidence}, #{item.status})
            </foreach>
            </script>
            """)
    void insertBatch(@Param("list") List<AuthorTagSystemTagMapDO> mappings);
}
