package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.controller.vo.AuthorTagVO;
import com.putl.articleservice.enums.TagStatus;
import com.putl.articleservice.exception.BusinessException;
import com.putl.articleservice.mapper.ArticleAuthorTagRelMapper;
import com.putl.articleservice.mapper.AuthorTagMapper;
import com.putl.articleservice.mapper.dto.AuthorTagHotStatDTO;
import com.putl.articleservice.mapper.dto.TagRelationCountDTO;
import com.putl.articleservice.mapper.entity.ArticleAuthorTagRelDO;
import com.putl.articleservice.mapper.entity.AuthorTagDO;
import com.putl.articleservice.service.AuthorTagService;
import com.putl.articleservice.utils.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorTagServiceImpl implements AuthorTagService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_AUTHOR_TAG_COUNT = 5;
    private static final int MIN_AUTHOR_TAG_LENGTH = 2;
    private static final int MAX_AUTHOR_TAG_LENGTH = 20;

    private final AuthorTagMapper authorTagMapper;
    private final ArticleAuthorTagRelMapper articleAuthorTagRelMapper;

    @Override
    public PageResult<AuthorTagVO> getAdminPage(Integer pageNo, Integer pageSize, String keyword) {
        LambdaQueryWrapper<AuthorTagDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AuthorTagDO::getName, keyword.trim())
                    .or()
                    .like(AuthorTagDO::getNormalizedName, normalizeTagName(keyword)));
        }
        wrapper.orderByDesc(AuthorTagDO::getUpdateTime).orderByDesc(AuthorTagDO::getId);

        Page<AuthorTagDO> page = authorTagMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<AuthorTagVO> list = toAuthorTagVOs(page.getRecords());
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public List<AuthorTagVO> search(String keyword, Integer limit) {
        int effectiveLimit = limit == null || limit < 1 ? DEFAULT_LIMIT : Math.min(limit, 20);
        if (!StringUtils.hasText(keyword)) {
            return getHotTags(effectiveLimit);
        }

        LambdaQueryWrapper<AuthorTagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuthorTagDO::getStatus, TagStatus.ACTIVE.name())
                .and(w -> w.like(AuthorTagDO::getName, keyword.trim())
                        .or()
                        .like(AuthorTagDO::getNormalizedName, normalizeTagName(keyword)))
                .orderByDesc(AuthorTagDO::getUpdateTime)
                .last("limit " + effectiveLimit);
        return toAuthorTagVOs(authorTagMapper.selectList(wrapper));
    }

    @Override
    public List<AuthorTagVO> getHotTags(Integer limit) {
        int effectiveLimit = limit == null || limit < 1 ? DEFAULT_LIMIT : Math.min(limit, 20);
        List<AuthorTagHotStatDTO> hotStats = authorTagMapper.selectHotTags(effectiveLimit);
        if (hotStats == null || hotStats.isEmpty()) {
            return Collections.emptyList();
        }
        return hotStats.stream()
                .map(item -> AuthorTagVO.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .articleCount(item.getArticleCount())
                        .status(TagStatus.ACTIVE.name())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public List<Integer> replaceArticleAuthorTags(Integer articleId, Integer creatorId, List<String> tagNames) {
        List<String> cleanedTags = sanitizeAuthorTags(tagNames);
        articleAuthorTagRelMapper.deleteByArticleId(articleId);
        if (cleanedTags.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> authorTagIds = cleanedTags.stream()
                .map(tagName -> getOrCreate(tagName, creatorId).getId())
                .toList();

        List<ArticleAuthorTagRelDO> relations = authorTagIds.stream()
                .map(authorTagId -> ArticleAuthorTagRelDO.builder()
                        .articleId(articleId)
                        .authorTagId(authorTagId)
                        .build())
                .toList();
        articleAuthorTagRelMapper.insertBatch(relations);
        return authorTagIds;
    }

    @Override
    public List<String> getArticleAuthorTags(Integer articleId) {
        List<Integer> authorTagIds = articleAuthorTagRelMapper.selectAuthorTagIdsByArticleId(articleId);
        if (authorTagIds == null || authorTagIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, AuthorTagDO> tagMap = authorTagMapper.selectBatchIds(authorTagIds).stream()
                .collect(Collectors.toMap(AuthorTagDO::getId, Function.identity(), (left, right) -> left));
        return authorTagIds.stream()
                .map(tagMap::get)
                .filter(tag -> tag != null && TagStatus.ACTIVE.name().equals(tag.getStatus()))
                .map(AuthorTagDO::getName)
                .toList();
    }

    @Override
    public Map<Integer, List<String>> getArticleAuthorTagMap(List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, List<Integer>> articleTagIdMap = articleAuthorTagRelMapper.selectAuthorTagIdMapByArticleIds(articleIds);
        if (articleTagIdMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Integer> authorTagIds = articleTagIdMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Integer, AuthorTagDO> tagMap = authorTagMapper.selectBatchIds(authorTagIds).stream()
                .collect(Collectors.toMap(AuthorTagDO::getId, Function.identity(), (left, right) -> left));

        Map<Integer, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : articleTagIdMap.entrySet()) {
            List<String> names = entry.getValue().stream()
                    .map(tagMap::get)
                    .filter(tag -> tag != null && TagStatus.ACTIVE.name().equals(tag.getStatus()))
                    .map(AuthorTagDO::getName)
                    .toList();
            result.put(entry.getKey(), names);
        }
        return result;
    }

    @Override
    public List<Integer> getArticleIdsByAuthorTagName(String authorTagName) {
        String cleanedName = cleanAuthorTagName(authorTagName);
        if (!StringUtils.hasText(cleanedName)) {
            return Collections.emptyList();
        }

        AuthorTagDO authorTag = authorTagMapper.selectOne(new LambdaQueryWrapper<AuthorTagDO>()
                .eq(AuthorTagDO::getName, cleanedName)
                .eq(AuthorTagDO::getStatus, TagStatus.ACTIVE.name())
                .last("limit 1"));
        if (authorTag == null) {
            return Collections.emptyList();
        }
        return articleAuthorTagRelMapper.selectArticleIdsByAuthorTagId(authorTag.getId());
    }

    @Override
    public List<Integer> getArticleIdsByAuthorTagIds(List<Integer> authorTagIds) {
        if (authorTagIds == null || authorTagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return articleAuthorTagRelMapper.selectArticleIdsByAuthorTagIds(authorTagIds);
    }

    private List<AuthorTagVO> toAuthorTagVOs(List<AuthorTagDO> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> tagIds = tags.stream().map(AuthorTagDO::getId).toList();
        Map<Integer, Long> articleCountMap = articleAuthorTagRelMapper.countByAuthorTagIds(tagIds).stream()
                .collect(Collectors.toMap(TagRelationCountDTO::getTagId, TagRelationCountDTO::getArticleCount, (left, right) -> left));
        return tags.stream()
                .map(tag -> AuthorTagVO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .normalizedName(tag.getNormalizedName())
                        .status(tag.getStatus())
                        .articleCount(articleCountMap.getOrDefault(tag.getId(), 0L))
                        .createTime(tag.getCreateTime())
                        .build())
                .toList();
    }

    private AuthorTagDO getOrCreate(String tagName, Integer creatorId) {
        AuthorTagDO existing = authorTagMapper.selectOne(new LambdaQueryWrapper<AuthorTagDO>()
                .eq(AuthorTagDO::getName, tagName)
                .last("limit 1"));
        if (existing != null) {
            if (!TagStatus.ACTIVE.name().equals(existing.getStatus())) {
                throw new BusinessException(400, "标签已被禁用: " + tagName);
            }
            return existing;
        }

        AuthorTagDO authorTagDO = AuthorTagDO.builder()
                .name(tagName)
                .normalizedName(normalizeTagName(tagName))
                .creatorId(creatorId)
                .status(TagStatus.ACTIVE.name())
                .build();
        try {
            authorTagMapper.insert(authorTagDO);
            return authorTagDO;
        } catch (DuplicateKeyException e) {
            log.debug("author tag created concurrently, fallback to select: {}", tagName);
            AuthorTagDO duplicated = authorTagMapper.selectOne(new LambdaQueryWrapper<AuthorTagDO>()
                    .eq(AuthorTagDO::getName, tagName)
                    .last("limit 1"));
            if (duplicated == null) {
                throw e;
            }
            if (!TagStatus.ACTIVE.name().equals(duplicated.getStatus())) {
                throw new BusinessException(400, "标签已被禁用: " + tagName);
            }
            return duplicated;
        }
    }

    private List<String> sanitizeAuthorTags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        Set<String> deduplicated = new LinkedHashSet<>();
        for (String rawTag : rawTags) {
            String cleaned = cleanAuthorTagName(rawTag);
            if (!StringUtils.hasText(cleaned)) {
                continue;
            }
            if (cleaned.length() < MIN_AUTHOR_TAG_LENGTH || cleaned.length() > MAX_AUTHOR_TAG_LENGTH) {
                throw new BusinessException(400, "作者标签长度需在2到20个字符之间");
            }
            if (deduplicated.add(cleaned)) {
                result.add(cleaned);
            }
        }

        if (result.size() > MAX_AUTHOR_TAG_COUNT) {
            throw new BusinessException(400, "每篇文章最多只能设置5个作者标签");
        }
        return result;
    }

    private String cleanAuthorTagName(String rawTag) {
        if (!StringUtils.hasText(rawTag)) {
            return "";
        }
        return rawTag.trim().replaceAll("\\s+", " ");
    }

    private String normalizeTagName(String rawTag) {
        return cleanAuthorTagName(rawTag);
    }
}
