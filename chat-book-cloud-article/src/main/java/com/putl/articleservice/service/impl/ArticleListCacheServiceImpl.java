package com.putl.articleservice.service.impl;

import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.service.ArticleListCacheService;
import com.putl.articleservice.service.ArticlePageService;
import com.putl.articleservice.utils.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 文章列表首页缓存服务实现
 * Phase 1 只缓存第一页，5分钟 TTL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleListCacheServiceImpl implements ArticleListCacheService {

    private final ArticlePageService articlePageService;

    @Override
    @Cacheable(value = "articleListCache", key = "'new:' + #pageNo", unless = "#result == null || #result.getList().isEmpty()")
    public PageResult<ArticleListVO> getNewPage(Integer pageNo, Integer pageSize) {
        // 非首页不缓存
        if (pageNo != 1) {
            return articlePageService.getNewPage(pageNo, pageSize);
        }
        return articlePageService.getNewPage(pageNo, pageSize);
    }

    @Override
    @Cacheable(value = "articleListCache", key = "'hot:' + #pageNo", unless = "#result == null || #result.getList().isEmpty()")
    public PageResult<ArticleListVO> getHotPage(Integer pageNo, Integer pageSize) {
        if (pageNo != 1) {
            return articlePageService.getHotPage(pageNo, pageSize);
        }
        return articlePageService.getHotPage(pageNo, pageSize);
    }

    @Override
    @Cacheable(value = "articleListCache", key = "'todayHot:' + #pageNo", unless = "#result == null || #result.getList().isEmpty()")
    public PageResult<ArticleListVO> getTodayHotPage(Integer pageNo, Integer pageSize) {
        if (pageNo != 1) {
            return articlePageService.getTodayHotPage(pageNo, pageSize);
        }
        return articlePageService.getTodayHotPage(pageNo, pageSize);
    }

    @Override
    @Cacheable(value = "articleListCache", key = "'tag:' + #tagId + ':' + #pageNo", unless = "#result == null || #result.getList().isEmpty()")
    public PageResult<ArticleListVO> getTagPage(Integer pageNo, Integer pageSize, Integer tagId) {
        if (pageNo != 1) {
            return articlePageService.getTagPage(pageNo, pageSize, tagId);
        }
        return articlePageService.getTagPage(pageNo, pageSize, tagId);
    }

    @Override
    @CacheEvict(value = "articleListCache", key = "'new:1'")
    public void evictNewCache() {
        log.debug("Evict new cache");
    }

    @Override
    @CacheEvict(value = "articleListCache", key = "'hot:1'")
    public void evictHotCache() {
        log.debug("Evict hot cache");
    }

    @Override
    @CacheEvict(value = "articleListCache", key = "'todayHot:1'")
    public void evictTodayHotCache() {
        log.debug("Evict todayHot cache");
    }
}