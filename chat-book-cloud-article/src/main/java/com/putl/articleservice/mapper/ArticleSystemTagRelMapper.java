package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.articleservice.mapper.dto.ArticleSystemTagPair;
import com.putl.articleservice.mapper.dto.TagRelationCountDTO;
import com.putl.articleservice.mapper.entity.ArticleSystemTagRelDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface ArticleSystemTagRelMapper extends BaseMapper<ArticleSystemTagRelDO> {

    @Select("SELECT system_tag_id FROM article_system_tag_rel WHERE article_id = #{articleId}")
    List<Integer> selectSystemTagIdsByArticleId(@Param("articleId") Integer articleId);

    @Select("SELECT article_id FROM article_system_tag_rel WHERE system_tag_id = #{systemTagId}")
    List<Integer> selectArticleIdsBySystemTagId(@Param("systemTagId") Integer systemTagId);

    @Select("""
            <script>
            SELECT DISTINCT article_id
            FROM article_system_tag_rel
            WHERE system_tag_id IN
            <foreach collection='systemTagIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<Integer> selectArticleIdsBySystemTagIds(@Param("systemTagIds") List<Integer> systemTagIds);

    @Select("""
            <script>
            SELECT article_id, system_tag_id
            FROM article_system_tag_rel
            WHERE article_id IN
            <foreach collection='articleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<ArticleSystemTagPair> selectArticleSystemTagPairs(@Param("articleIds") List<Integer> articleIds);

    default Map<Integer, List<Integer>> selectSystemTagIdMapByArticleIds(List<Integer> articleIds) {
        return selectArticleSystemTagPairs(articleIds).stream().collect(Collectors.groupingBy(
                ArticleSystemTagPair::getArticleId,
                Collectors.mapping(ArticleSystemTagPair::getSystemTagId, Collectors.toList())
        ));
    }

    @Select("""
            <script>
            SELECT system_tag_id AS tag_id, COUNT(article_id) AS article_count
            FROM article_system_tag_rel
            WHERE system_tag_id IN
            <foreach collection='tagIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            GROUP BY system_tag_id
            </script>
            """)
    List<TagRelationCountDTO> countBySystemTagIds(@Param("tagIds") List<Integer> tagIds);

    @Delete("DELETE FROM article_system_tag_rel WHERE article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Integer articleId);

    @Delete("DELETE FROM article_system_tag_rel WHERE article_id = #{articleId} AND source <> 'ADMIN'")
    void deleteAutoByArticleId(@Param("articleId") Integer articleId);

    @Insert("""
            <script>
            INSERT IGNORE INTO article_system_tag_rel (article_id, system_tag_id, source, confidence)
            VALUES
            <foreach collection='list' item='item' separator=','>
              (#{item.articleId}, #{item.systemTagId}, #{item.source}, #{item.confidence})
            </foreach>
            </script>
            """)
    void insertIgnoreBatch(@Param("list") List<ArticleSystemTagRelDO> relations);
}
