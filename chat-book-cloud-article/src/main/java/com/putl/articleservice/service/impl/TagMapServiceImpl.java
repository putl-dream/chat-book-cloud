package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.controller.vo.SystemTagVO;
import com.putl.articleservice.controller.vo.TagMapVO;
import com.putl.articleservice.enums.TagSource;
import com.putl.articleservice.enums.TagStatus;
import com.putl.articleservice.exception.BusinessException;
import com.putl.articleservice.mapper.ArticleAuthorTagRelMapper;
import com.putl.articleservice.mapper.AuthorTagMapper;
import com.putl.articleservice.mapper.AuthorTagSystemTagMapMapper;
import com.putl.articleservice.mapper.SystemTagMapper;
import com.putl.articleservice.mapper.dto.TagRelationCountDTO;
import com.putl.articleservice.mapper.entity.AuthorTagDO;
import com.putl.articleservice.mapper.entity.AuthorTagSystemTagMapDO;
import com.putl.articleservice.mapper.entity.SystemTagDO;
import com.putl.articleservice.service.SystemTagService;
import com.putl.articleservice.service.TagMapService;
import com.putl.articleservice.utils.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagMapServiceImpl implements TagMapService {

    private static final BigDecimal DEFAULT_CONFIDENCE = new BigDecimal("1.00");

    private final AuthorTagMapper authorTagMapper;
    private final SystemTagMapper systemTagMapper;
    private final AuthorTagSystemTagMapMapper authorTagSystemTagMapMapper;
    private final ArticleAuthorTagRelMapper articleAuthorTagRelMapper;
    private final SystemTagService systemTagService;

    @Override
    public PageResult<TagMapVO> getPage(Integer pageNo, Integer pageSize, String keyword, Boolean mappedOnly) {
        List<Integer> mappedAuthorTagIds = Collections.emptyList();
        if (Boolean.TRUE.equals(mappedOnly)) {
            mappedAuthorTagIds = authorTagSystemTagMapMapper.selectList(new LambdaQueryWrapper<AuthorTagSystemTagMapDO>()
                            .eq(AuthorTagSystemTagMapDO::getStatus, TagStatus.ACTIVE.name()))
                    .stream()
                    .map(AuthorTagSystemTagMapDO::getAuthorTagId)
                    .distinct()
                    .toList();
            if (mappedAuthorTagIds.isEmpty()) {
                return PageResult.empty();
            }
        }

        LambdaQueryWrapper<AuthorTagDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AuthorTagDO::getName, keyword.trim())
                    .or()
                    .like(AuthorTagDO::getNormalizedName, keyword.trim()));
        }
        if (!mappedAuthorTagIds.isEmpty()) {
            wrapper.in(AuthorTagDO::getId, mappedAuthorTagIds);
        }
        wrapper.eq(AuthorTagDO::getStatus, TagStatus.ACTIVE.name())
                .orderByDesc(AuthorTagDO::getUpdateTime)
                .orderByDesc(AuthorTagDO::getId);

        Page<AuthorTagDO> page = authorTagMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return new PageResult<>(toTagMapVOs(page.getRecords()), page.getTotal());
    }

    @Override
    @Transactional
    public void updateMappings(Integer authorTagId, List<Integer> systemTagIds) {
        if (authorTagId == null) {
            throw new BusinessException(400, "authorTagId 不能为空");
        }
        AuthorTagDO authorTag = authorTagMapper.selectById(authorTagId);
        if (authorTag == null) {
            throw new BusinessException(404, "作者标签不存在");
        }

        List<Integer> distinctSystemTagIds = systemTagIds == null ? Collections.emptyList() : systemTagIds.stream().distinct().toList();
        if (!distinctSystemTagIds.isEmpty()) {
            List<SystemTagDO> activeTags = systemTagMapper.selectList(new LambdaQueryWrapper<SystemTagDO>()
                    .in(SystemTagDO::getId, distinctSystemTagIds)
                    .eq(SystemTagDO::getStatus, TagStatus.ACTIVE.name()));
            if (activeTags.size() != distinctSystemTagIds.size()) {
                throw new BusinessException(400, "部分系统标签不存在或已禁用");
            }
        }

        authorTagSystemTagMapMapper.deleteByAuthorTagId(authorTagId);
        if (distinctSystemTagIds.isEmpty()) {
            return;
        }

        List<AuthorTagSystemTagMapDO> mappings = distinctSystemTagIds.stream()
                .map(systemTagId -> AuthorTagSystemTagMapDO.builder()
                        .authorTagId(authorTagId)
                        .systemTagId(systemTagId)
                        .source(TagSource.ADMIN.name())
                        .confidence(DEFAULT_CONFIDENCE)
                        .status(TagStatus.ACTIVE.name())
                        .build())
                .toList();
        authorTagSystemTagMapMapper.insertBatch(mappings);
    }

    @Override
    @Transactional
    public void batchApply(List<Integer> authorTagIds) {
        if (authorTagIds == null || authorTagIds.isEmpty()) {
            return;
        }
        List<Integer> articleIds = articleAuthorTagRelMapper.selectArticleIdsByAuthorTagIds(authorTagIds.stream().distinct().toList());
        if (articleIds == null || articleIds.isEmpty()) {
            return;
        }
        for (Integer articleId : articleIds.stream().distinct().toList()) {
            List<Integer> fullAuthorTagIds = articleAuthorTagRelMapper.selectAuthorTagIdsByArticleId(articleId);
            systemTagService.syncAutoTags(articleId, fullAuthorTagIds);
        }
    }

    private List<TagMapVO> toTagMapVOs(List<AuthorTagDO> authorTags) {
        if (authorTags == null || authorTags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> authorTagIds = authorTags.stream().map(AuthorTagDO::getId).toList();
        Map<Integer, Long> articleCountMap = articleAuthorTagRelMapper.countByAuthorTagIds(authorTagIds).stream()
                .collect(Collectors.toMap(TagRelationCountDTO::getTagId, TagRelationCountDTO::getArticleCount, (left, right) -> left));

        List<AuthorTagSystemTagMapDO> mappings = authorTagSystemTagMapMapper.selectList(new LambdaQueryWrapper<AuthorTagSystemTagMapDO>()
                .in(AuthorTagSystemTagMapDO::getAuthorTagId, authorTagIds)
                .eq(AuthorTagSystemTagMapDO::getStatus, TagStatus.ACTIVE.name()));
        Map<Integer, List<Integer>> mappingIdsByAuthorTagId = mappings.stream().collect(Collectors.groupingBy(
                AuthorTagSystemTagMapDO::getAuthorTagId,
                Collectors.mapping(AuthorTagSystemTagMapDO::getSystemTagId, Collectors.toList())
        ));

        Set<Integer> systemTagIds = mappings.stream()
                .map(AuthorTagSystemTagMapDO::getSystemTagId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Integer, SystemTagVO> systemTagMap = systemTagIds.isEmpty()
                ? Collections.emptyMap()
                : systemTagMapper.selectBatchIds(systemTagIds).stream()
                .collect(Collectors.toMap(
                        SystemTagDO::getId,
                        tag -> SystemTagVO.builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .code(tag.getCode())
                                .dimension(tag.getDimension())
                                .description(tag.getDescription())
                                .status(tag.getStatus())
                                .sort(tag.getSort())
                                .recommendWeight(tag.getRecommendWeight())
                                .build(),
                        (left, right) -> left
                ));

        return authorTags.stream()
                .map(authorTag -> TagMapVO.builder()
                        .authorTagId(authorTag.getId())
                        .authorTagName(authorTag.getName())
                        .articleCount(articleCountMap.getOrDefault(authorTag.getId(), 0L))
                        .systemTags(mappingIdsByAuthorTagId.getOrDefault(authorTag.getId(), Collections.emptyList()).stream()
                                .map(systemTagMap::get)
                                .filter(java.util.Objects::nonNull)
                                .toList())
                        .build())
                .toList();
    }
}
