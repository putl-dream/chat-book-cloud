package com.putl.interactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.interactionservice.entity.UserFootDO;
import com.putl.interactionservice.mapper.dto.ArticleFootStatAggregate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFootMapper extends BaseMapper<UserFootDO> {

    @Select({
        "<script>",
        "SELECT",
        "  document_id AS articleId,",
        "  COUNT(*) AS totalCount,",
        "  SUM(CASE WHEN read_stat = 1 THEN 1 ELSE 0 END) AS readCount,",
        "  SUM(CASE WHEN praise_stat = 1 THEN 1 ELSE 0 END) AS praiseCount,",
        "  SUM(CASE WHEN collection_stat = 1 THEN 1 ELSE 0 END) AS collectCount",
        "FROM user_foot",
        "WHERE document_id IN",
        "<foreach collection='articleIds' item='articleId' open='(' separator=',' close=')'>",
        "  #{articleId}",
        "</foreach>",
        "GROUP BY document_id",
        "</script>"
    })
    List<ArticleFootStatAggregate> aggregateArticleStats(@Param("articleIds") List<Integer> articleIds);
}
