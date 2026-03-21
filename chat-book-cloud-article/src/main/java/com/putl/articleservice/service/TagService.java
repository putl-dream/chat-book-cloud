package com.putl.articleservice.service;

import com.putl.articleservice.controller.vo.TagVO;
import com.putl.articleservice.utils.PageResult;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 分页查询标签
     */
    PageResult<TagVO> getTagPage(Integer pageNo, Integer pageSize, Integer type);

    /**
     * 获取所有标签
     */
    List<TagVO> getAllTags();

    /**
     * 根据类型获取标签
     */
    List<TagVO> getTagsByType(Integer type);

    /**
     * 创建标签
     */
    TagVO createTag(TagVO tagVO);

    /**
     * 更新标签
     */
    void updateTag(TagVO tagVO);

    /**
     * 删除标签
     */
    void deleteTag(Integer tagId);

    /**
     * 设置文章标签
     */
    void setArticleTags(Integer articleId, List<Integer> tagIds);

    /**
     * 获取文章标签ID列表
     */
    List<Integer> getArticleTagIds(Integer articleId);

    /**
     * 批量获取文章标签ID列表
     */
    List<Integer> getArticleTagIds(List<Integer> articleIds);
}
