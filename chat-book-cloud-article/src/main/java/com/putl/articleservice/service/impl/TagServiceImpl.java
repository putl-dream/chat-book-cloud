package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.controller.vo.TagVO;
import com.putl.articleservice.mapper.ArticleTagMapper;
import com.putl.articleservice.mapper.TagMapper;
import com.putl.articleservice.mapper.entity.ArticleTagDO;
import com.putl.articleservice.mapper.entity.TagDO;
import com.putl.articleservice.service.TagService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    public PageResult<TagVO> getTagPage(Integer pageNo, Integer pageSize, Integer type) {
        LambdaQueryWrapper<TagDO> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(TagDO::getType, type);
        }
        wrapper.orderByAsc(TagDO::getSort);

        Page<TagDO> page = new Page<>(pageNo, pageSize);
        Page<TagDO> result = tagMapper.selectPage(page, wrapper);

        List<TagVO> voList = BeanUtil.toBean(result.getRecords(), TagVO.class);
        return new PageResult<>(voList, result.getTotal());
    }

    @Override
    public List<TagVO> getAllTags() {
        List<TagDO> list = tagMapper.selectList(
                new LambdaQueryWrapper<TagDO>()
                        .orderByAsc(TagDO::getSort)
        );
        return BeanUtil.toBean(list, TagVO.class);
    }

    @Override
    public List<TagVO> getTagsByType(Integer type) {
        List<TagDO> list = tagMapper.selectList(
                new LambdaQueryWrapper<TagDO>()
                        .eq(TagDO::getType, type)
                        .orderByAsc(TagDO::getSort)
        );
        return BeanUtil.toBean(list, TagVO.class);
    }

    @Override
    @Transactional
    public TagVO createTag(TagVO tagVO) {
        TagDO tagDO = BeanUtil.toBean(tagVO, TagDO.class);
        tagMapper.insert(tagDO);
        return BeanUtil.toBean(tagDO, TagVO.class);
    }

    @Override
    @Transactional
    public void updateTag(TagVO tagVO) {
        TagDO tagDO = BeanUtil.toBean(tagVO, TagDO.class);
        tagMapper.updateById(tagDO);
    }

    @Override
    @Transactional
    public void deleteTag(Integer tagId) {
        tagMapper.deleteById(tagId);
    }

    @Override
    @Transactional
    public void setArticleTags(Integer articleId, List<Integer> tagIds) {
        // 先删再插
        LambdaQueryWrapper<ArticleTagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTagDO::getArticleId, articleId);
        articleTagMapper.delete(wrapper);

        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 去重
        List<Integer> distinctTagIds = tagIds.stream().distinct().toList();

        // 校验标签存在性和类型约束
        List<TagDO> existingTags = tagMapper.selectList(
                new LambdaQueryWrapper<TagDO>().in(TagDO::getId, distinctTagIds)
        );
        if (existingTags.size() != distinctTagIds.size()) {
            throw new com.putl.articleservice.exception.BusinessException(400, "部分标签不存在");
        }

        // 技术栈标签(type=1)最多3个，学习路径标签(type=2)最多1个
        long techCount = existingTags.stream().filter(t -> t.getType() == 1).count();
        long pathCount = existingTags.stream().filter(t -> t.getType() == 2).count();
        if (techCount > 3) {
            throw new com.putl.articleservice.exception.BusinessException(400, "技术栈标签最多选择3个");
        }
        if (pathCount > 1) {
            throw new com.putl.articleservice.exception.BusinessException(400, "学习路径标签最多选择1个");
        }

        // 批量新增关联，避免 N+1 插入
        List<ArticleTagDO> articleTagList = distinctTagIds.stream()
                .map(tagId -> ArticleTagDO.builder()
                        .articleId(articleId)
                        .tagId(tagId)
                        .build())
                .toList();
        articleTagMapper.insertBatch(articleTagList);
    }

    @Override
    public List<Integer> getArticleTagIds(Integer articleId) {
        return articleTagMapper.selectTagIdsByArticleId(articleId);
    }

    @Override
    public List<Integer> getArticleTagIds(List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return articleTagMapper.selectTagIdsByArticleIds(articleIds);
    }
}
