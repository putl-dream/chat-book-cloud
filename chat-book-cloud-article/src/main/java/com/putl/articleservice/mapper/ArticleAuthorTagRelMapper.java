package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.articleservice.mapper.dto.ArticleAuthorTagPair;
import com.putl.articleservice.mapper.dto.TagRelationCountDTO;
import com.putl.articleservice.mapper.entity.ArticleAuthorTagRelDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface ArticleAuthorTagRelMapper extends BaseMapper<ArticleAuthorTagRelDO> {

    @Select("SELECT author_tag_id FROM article_author_tag_rel WHERE article_id = #{articleId}")
    List<Integer> selectAuthorTagIdsByArticleId(@Param("articleId") Integer articleId);

    @Select("SELECT article_id FROM article_author_tag_rel WHERE author_tag_id = #{authorTagId}")
    List<Integer> selectArticleIdsByAuthorTagId(@Param("authorTagId") Integer authorTagId);

    @Select("""
            <script>
            SELECT DISTINCT article_id
            FROM article_author_tag_rel
            WHERE author_tag_id IN
            <foreach collection='authorTagIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<Integer> selectArticleIdsByAuthorTagIds(@Param("authorTagIds") List<Integer> authorTagIds);

    @Select("""
            <script>
            SELECT article_id, author_tag_id
            FROM article_author_tag_rel
            WHERE article_id IN
            <foreach collection='articleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            </script>
            """)
    List<ArticleAuthorTagPair> selectArticleAuthorTagPairs(@Param("articleIds") List<Integer> articleIds);

    default Map<Integer, List<Integer>> selectAuthorTagIdMapByArticleIds(List<Integer> articleIds) {
        return selectArticleAuthorTagPairs(articleIds).stream().collect(Collectors.groupingBy(
                ArticleAuthorTagPair::getArticleId,
                Collectors.mapping(ArticleAuthorTagPair::getAuthorTagId, Collectors.toList())
        ));
    }

    @Select("""
            <script>
            SELECT author_tag_id AS tag_id, COUNT(article_id) AS article_count
            FROM article_author_tag_rel
            WHERE author_tag_id IN
            <foreach collection='tagIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            GROUP BY author_tag_id
            </script>
            """)
    List<TagRelationCountDTO> countByAuthorTagIds(@Param("tagIds") List<Integer> tagIds);

    @Delete("DELETE FROM article_author_tag_rel WHERE article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Integer articleId);

    @Insert("""
            <script>
            INSERT INTO article_author_tag_rel (article_id, author_tag_id) VALUES
            <foreach collection='list' item='item' separator=','>
              (#{item.articleId}, #{item.authorTagId})
            </foreach>
            </script>
            """)
    void insertBatch(@Param("list") List<ArticleAuthorTagRelDO> relations);
}
