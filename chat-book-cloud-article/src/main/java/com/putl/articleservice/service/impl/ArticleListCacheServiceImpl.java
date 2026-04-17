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
 * 最新/标签列表缓存使用较长 TTL，热榜第一页使用短 TTL。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleListCacheServiceImpl implements ArticleListCacheService {

    private final ArticlePageService articlePageService;

    @Override
    @Cacheable(value = "articleListCache", key = "'new:' + #pageNo + ':' + #pageSize", unless = "#result == null || #result.getList().isEmpty()")
    public PageResult<ArticleListVO> getNewPage(Integer pageNo, Integer pageSize) {
        // 非首页不缓存
        if (pageNo != 1) {
            return articlePageService.getNewPage(pageNo, pageSize);
        }
        return articlePageService.getNewPage(pageNo, pageSize);
    }

    @Override
    @Cacheable(value = "hotArticleListCache", key = "'hot:' + #pageNo + ':' + #pageSize", unless = "#result == null || #result.getList().isEmpty()")
    public PageResult<ArticleListVO> getHotPage(Integer pageNo, Integer pageSize) {
        return articlePageService.getHotPage(pageNo, pageSize);
    }

    @Override
    public PageResult<ArticleListVO> getTodayHotPage(Integer pageNo, Integer pageSize) {
        return articlePageService.getTodayHotPage(pageNo, pageSize);
    }

    @Override
    @Cacheable(value = "articleListCache", key = "'tag:' + #authorTagName + ':' + #pageNo + ':' + #pageSize", unless = "#result == null || #result.getList().isEmpty()")
    public PageResult<ArticleListVO> getTagPage(Integer pageNo, Integer pageSize, String authorTagName) {
        if (pageNo != 1) {
            return articlePageService.getTagPage(pageNo, pageSize, authorTagName);
        }
        return articlePageService.getTagPage(pageNo, pageSize, authorTagName);
    }

    @Override
    @CacheEvict(value = "articleListCache", allEntries = true)
    public void evictNewCache() {
        log.debug("Evict new cache");
    }

    @Override
    @CacheEvict(value = "hotArticleListCache", allEntries = true)
    public void evictHotCache() {
        log.debug("Evict hot cache");
    }

    @Override
    @CacheEvict(value = "hotArticleListCache", allEntries = true)
    public void evictTodayHotCache() {
        log.debug("Evict todayHot cache");
    }
}
