package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.articleservice.mapper.dto.ArticleTagPair;
import com.putl.articleservice.mapper.entity.ArticleTagDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 文章标签关联 Mapper 接口
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTagDO> {

    /**
     * 根据文章ID查询标签ID列表
     */
    @Select("SELECT tag_id FROM article_tag WHERE article_id = #{articleId}")
    List<Integer> selectTagIdsByArticleId(@Param("articleId") Integer articleId);

    /**
     * 根据标签ID查询文章ID列表
     */
    @Select("SELECT article_id FROM article_tag WHERE tag_id = #{tagId}")
    List<Integer> selectArticleIdsByTagId(@Param("tagId") Integer tagId);

    /**
     * 根据文章ID列表查询标签ID列表（扁平）
     */
    @Select("<script>SELECT DISTINCT tag_id FROM article_tag WHERE article_id IN <foreach collection='articleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Integer> selectTagIdsByArticleIds(@Param("articleIds") List<Integer> articleIds);

    /**
     * 根据文章ID列表查询文章ID和标签ID对
     */
    @Select("<script>SELECT article_id, tag_id FROM article_tag WHERE article_id IN <foreach collection='articleIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<ArticleTagPair> selectArticleTagPairs(@Param("articleIds") List<Integer> articleIds);

    /**
     * 根据文章ID列表查询 Map<articleId, List<tagId>>
     */
    default Map<Integer, List<Integer>> selectTagIdMapByArticleIds(List<Integer> articleIds) {
        List<ArticleTagPair> pairs = selectArticleTagPairs(articleIds);
        return pairs.stream().collect(java.util.stream.Collectors.groupingBy(
                p -> p.articleId,
                java.util.stream.Collectors.mapping(p -> p.tagId, java.util.stream.Collectors.toList())
        ));
    }

    /**
     * 批量插入文章标签关联
     */
    @Insert("<script>" +
            "INSERT INTO article_tag (article_id, tag_id) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.articleId}, #{item.tagId})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<ArticleTagDO> articleTagList);
}
