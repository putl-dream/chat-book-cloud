package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.controller.dto.SystemTagSaveRequestDTO;
import com.putl.articleservice.controller.vo.SystemTagVO;
import com.putl.articleservice.enums.TagSource;
import com.putl.articleservice.enums.TagStatus;
import com.putl.articleservice.exception.BusinessException;
import com.putl.articleservice.mapper.ArticleSystemTagRelMapper;
import com.putl.articleservice.mapper.AuthorTagSystemTagMapMapper;
import com.putl.articleservice.mapper.SystemTagMapper;
import com.putl.articleservice.mapper.dto.TagRelationCountDTO;
import com.putl.articleservice.mapper.entity.ArticleSystemTagRelDO;
import com.putl.articleservice.mapper.entity.SystemTagDO;
import com.putl.articleservice.service.SystemTagService;
import com.putl.articleservice.utils.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemTagServiceImpl implements SystemTagService {

    private static final BigDecimal DEFAULT_CONFIDENCE = new BigDecimal("1.00");

    private final SystemTagMapper systemTagMapper;
    private final ArticleSystemTagRelMapper articleSystemTagRelMapper;
    private final AuthorTagSystemTagMapMapper authorTagSystemTagMapMapper;

    @Override
    public PageResult<SystemTagVO> getPage(Integer pageNo, Integer pageSize, String keyword, String dimension, String status) {
        LambdaQueryWrapper<SystemTagDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SystemTagDO::getName, keyword.trim())
                    .or()
                    .like(SystemTagDO::getCode, keyword.trim())
                    .or()
                    .like(SystemTagDO::getDescription, keyword.trim()));
        }
        wrapper.eq(StringUtils.hasText(dimension), SystemTagDO::getDimension, dimension == null ? null : dimension.trim());
        wrapper.eq(StringUtils.hasText(status), SystemTagDO::getStatus, status == null ? null : status.trim().toUpperCase());
        wrapper.orderByAsc(SystemTagDO::getSort).orderByDesc(SystemTagDO::getId);

        Page<SystemTagDO> page = systemTagMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return new PageResult<>(toSystemTagVOs(page.getRecords()), page.getTotal());
    }

    @Override
    public List<SystemTagVO> getAllActiveTags() {
        return toSystemTagVOs(systemTagMapper.selectList(new LambdaQueryWrapper<SystemTagDO>()
                .eq(SystemTagDO::getStatus, TagStatus.ACTIVE.name())
                .orderByAsc(SystemTagDO::getSort)
                .orderByDesc(SystemTagDO::getId)));
    }

    @Override
    @Transactional
    public SystemTagVO create(SystemTagSaveRequestDTO request) {
        validateSaveRequest(request, false);
        SystemTagDO entity = SystemTagDO.builder()
                .name(request.getName().trim())
                .code(request.getCode().trim())
                .dimension(request.getDimension().trim())
                .description(defaultString(request.getDescription()))
                .status(normalizeStatus(request.getStatus()))
                .sort(request.getSort() == null ? 0 : request.getSort())
                .recommendWeight(request.getRecommendWeight() == null ? BigDecimal.ONE : request.getRecommendWeight())
                .build();
        systemTagMapper.insert(entity);
        return toSystemTagVOs(List.of(entity)).get(0);
    }

    @Override
    @Transactional
    public void update(SystemTagSaveRequestDTO request) {
        validateSaveRequest(request, true);
        SystemTagDO existing = systemTagMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(404, "系统标签不存在");
        }
        systemTagMapper.updateById(SystemTagDO.builder()
                .id(existing.getId())
                .name(request.getName().trim())
                .code(request.getCode().trim())
                .dimension(request.getDimension().trim())
                .description(defaultString(request.getDescription()))
                .status(normalizeStatus(request.getStatus()))
                .sort(request.getSort() == null ? 0 : request.getSort())
                .recommendWeight(request.getRecommendWeight() == null ? BigDecimal.ONE : request.getRecommendWeight())
                .build());
    }

    @Override
    @Transactional
    public void delete(Integer systemTagId) {
        if (systemTagId == null) {
            throw new BusinessException(400, "systemTagId 不能为空");
        }
        systemTagMapper.deleteById(systemTagId);
    }

    @Override
    public List<String> getArticleSystemTags(Integer articleId) {
        List<Integer> systemTagIds = articleSystemTagRelMapper.selectSystemTagIdsByArticleId(articleId);
        if (systemTagIds == null || systemTagIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, SystemTagDO> tagMap = systemTagMapper.selectBatchIds(systemTagIds).stream()
                .collect(Collectors.toMap(SystemTagDO::getId, Function.identity(), (left, right) -> left));
        return systemTagIds.stream()
                .map(tagMap::get)
                .filter(tag -> tag != null && TagStatus.ACTIVE.name().equals(tag.getStatus()))
                .map(SystemTagDO::getName)
                .toList();
    }

    @Override
    public Map<Integer, List<String>> getArticleSystemTagMap(List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, List<Integer>> systemTagIdMap = articleSystemTagRelMapper.selectSystemTagIdMapByArticleIds(articleIds);
        if (systemTagIdMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Integer> systemTagIds = systemTagIdMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Integer, SystemTagDO> tagMap = systemTagMapper.selectBatchIds(systemTagIds).stream()
                .collect(Collectors.toMap(SystemTagDO::getId, Function.identity(), (left, right) -> left));
        return systemTagIdMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(tagMap::get)
                                .filter(tag -> tag != null && TagStatus.ACTIVE.name().equals(tag.getStatus()))
                                .map(SystemTagDO::getName)
                                .toList()
                ));
    }

    @Override
    public List<Integer> getArticleIdsBySystemTagNames(List<String> systemTagNames) {
        if (systemTagNames == null || systemTagNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> cleanedNames = systemTagNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (cleanedNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<SystemTagDO> activeTags = systemTagMapper.selectList(new LambdaQueryWrapper<SystemTagDO>()
                .in(SystemTagDO::getName, cleanedNames)
                .eq(SystemTagDO::getStatus, TagStatus.ACTIVE.name()));
        if (activeTags.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> systemTagIds = activeTags.stream()
                .map(SystemTagDO::getId)
                .distinct()
                .toList();
        return articleSystemTagRelMapper.selectArticleIdsBySystemTagIds(systemTagIds);
    }

    @Override
    @Transactional
    public void syncAutoTags(Integer articleId, List<Integer> authorTagIds) {
        articleSystemTagRelMapper.deleteAutoByArticleId(articleId);
        if (authorTagIds == null || authorTagIds.isEmpty()) {
            return;
        }

        List<Integer> systemTagIds = authorTagSystemTagMapMapper.selectActiveSystemTagIdsByAuthorTagIds(authorTagIds);
        if (systemTagIds == null || systemTagIds.isEmpty()) {
            return;
        }

        List<SystemTagDO> activeSystemTags = systemTagMapper.selectList(new LambdaQueryWrapper<SystemTagDO>()
                .in(SystemTagDO::getId, systemTagIds)
                .eq(SystemTagDO::getStatus, TagStatus.ACTIVE.name()));
        if (activeSystemTags.isEmpty()) {
            return;
        }

        List<ArticleSystemTagRelDO> relations = activeSystemTags.stream()
                .map(tag -> ArticleSystemTagRelDO.builder()
                        .articleId(articleId)
                        .systemTagId(tag.getId())
                        .source(TagSource.RULE.name())
                        .confidence(DEFAULT_CONFIDENCE)
                        .build())
                .toList();
        articleSystemTagRelMapper.insertIgnoreBatch(relations);
    }

    @Override
    @Transactional
    public void updateArticleSystemTagsByAdmin(Integer articleId, List<Integer> systemTagIds) {
        if (articleId == null) {
            throw new BusinessException(400, "articleId 不能为空");
        }
        articleSystemTagRelMapper.deleteByArticleId(articleId);
        if (systemTagIds == null || systemTagIds.isEmpty()) {
            return;
        }

        List<Integer> distinctIds = systemTagIds.stream().distinct().toList();
        List<SystemTagDO> activeTags = systemTagMapper.selectList(new LambdaQueryWrapper<SystemTagDO>()
                .in(SystemTagDO::getId, distinctIds)
                .eq(SystemTagDO::getStatus, TagStatus.ACTIVE.name()));
        if (activeTags.size() != distinctIds.size()) {
            throw new BusinessException(400, "部分系统标签不存在或已禁用");
        }

        List<ArticleSystemTagRelDO> relations = distinctIds.stream()
                .map(systemTagId -> ArticleSystemTagRelDO.builder()
                        .articleId(articleId)
                        .systemTagId(systemTagId)
                        .source(TagSource.ADMIN.name())
                        .confidence(DEFAULT_CONFIDENCE)
                        .build())
                .toList();
        articleSystemTagRelMapper.insertIgnoreBatch(relations);
    }

    private List<SystemTagVO> toSystemTagVOs(List<SystemTagDO> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> tagIds = tags.stream().map(SystemTagDO::getId).toList();
        Map<Integer, Long> articleCountMap = articleSystemTagRelMapper.countBySystemTagIds(tagIds).stream()
                .collect(Collectors.toMap(TagRelationCountDTO::getTagId, TagRelationCountDTO::getArticleCount, (left, right) -> left));
        return tags.stream()
                .map(tag -> SystemTagVO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .code(tag.getCode())
                        .dimension(tag.getDimension())
                        .description(tag.getDescription())
                        .status(tag.getStatus())
                        .sort(tag.getSort())
                        .recommendWeight(tag.getRecommendWeight())
                        .articleCount(articleCountMap.getOrDefault(tag.getId(), 0L))
                        .build())
                .toList();
    }

    private void validateSaveRequest(SystemTagSaveRequestDTO request, boolean requireId) {
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }
        if (requireId && request.getId() == null) {
            throw new BusinessException(400, "id 不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new BusinessException(400, "系统标签名称不能为空");
        }
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException(400, "系统标签编码不能为空");
        }
        if (!StringUtils.hasText(request.getDimension())) {
            throw new BusinessException(400, "系统标签维度不能为空");
        }

        LambdaQueryWrapper<SystemTagDO> duplicateWrapper = new LambdaQueryWrapper<SystemTagDO>()
                .eq(SystemTagDO::getCode, request.getCode().trim());
        if (requireId) {
            duplicateWrapper.ne(SystemTagDO::getId, request.getId());
        }
        Long duplicateCount = systemTagMapper.selectCount(duplicateWrapper);
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException(400, "系统标签编码已存在");
        }
    }

    private String normalizeStatus(String rawStatus) {
        return StringUtils.hasText(rawStatus) ? rawStatus.trim().toUpperCase() : TagStatus.ACTIVE.name();
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }
}
