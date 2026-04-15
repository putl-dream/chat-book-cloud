package com.putl.interactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.interactionservice.entity.ReviewDO;
import com.putl.interactionservice.mapper.dto.ArticleCommentCountAggregate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<ReviewDO> {

    @Select({
        "<script>",
        "SELECT",
        "  text_id AS articleId,",
        "  COUNT(*) AS commentCount",
        "FROM review",
        "WHERE text_id IN",
        "<foreach collection='articleIds' item='articleId' open='(' separator=',' close=')'>",
        "  #{articleId}",
        "</foreach>",
        "GROUP BY text_id",
        "</script>"
    })
    List<ArticleCommentCountAggregate> countByArticleIds(@Param("articleIds") List<Integer> articleIds);
}
