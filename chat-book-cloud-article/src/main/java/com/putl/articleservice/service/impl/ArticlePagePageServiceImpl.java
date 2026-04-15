package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.articleservice.controller.dto.AdminArticlePageRequestDTO;
import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.enums.ArticleStatus;
import com.putl.articleservice.mapper.ArticleTagMapper;
import com.putl.articleservice.mapper.TagMapper;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.mapper.entity.TagDO;
import com.putl.articleservice.service.ArticlePageService;
import com.putl.articleservice.service.TagService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.redis.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章列表功能实现类，提供多种文章分页查询功能。
 * 功能包括但不限于：
 * - 获取最新文章列表
 * - 获取热门文章列表
 * - 根据分类/标签获取文章列表
 * - 搜索结果文章列表
 * - 系统每日推荐文章列表
 * - 个性化推荐文章列表
 * - 用户历史阅读文章列表
 * - 用户收藏文章列表
 * - 管理员审核文章列表
 * - 用户草稿箱文章列表
 *
 * @since 2025-01-13 20:46:01
 */
@Slf4j
@Service
public class ArticlePagePageServiceImpl extends BaseAbstractArticle implements ArticlePageService {

    private static final DateTimeFormatter HOT_DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int HOT_RANK_SCAN_MULTIPLIER = 3;

    @Resource
    private TagService tagService;

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.profiles.active:local}")
    private String env;

    /**
     * 获取最新文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getNewPage(Integer pageNo, Integer pageSize) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery().eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED).orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取热门文章列表。
     * 优先读取 Redis 全站热榜，Redis 不可用或数据不足时再降级到时间排序。
     * <p>
     * Redis 只存 articleId -> score，详情仍由数据库批量查询并按热榜顺序重排。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getHotPage(Integer pageNo, Integer pageSize) {
        return getRankedHotPage(
                RedisKeyConstants.interactionHotAll(env),
                pageNo,
                pageSize,
                this::getHotPageFallback
        );
    }

    /**
     * 获取今日热门文章列表。
     * 优先读取 Redis 日榜，不足时用 Redis 全站总榜补齐。
     * <p>
     * 日榜使用 yyyyMMdd 维度独立存储，避免老文章长期霸榜；补齐时保持日榜顺序优先，
     * 并对总榜结果去重，确保前端始终拿到稳定的热门列表。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getTodayHotPage(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 1 || pageSize == null || pageSize < 1) {
            return PageResult.empty();
        }

        String dayRankKey = RedisKeyConstants.interactionHotDay(env, LocalDate.now().format(HOT_DAY_FORMATTER));
        String allRankKey = RedisKeyConstants.interactionHotAll(env);
        long start = (long) (pageNo - 1) * pageSize;
        long requiredCount = start + pageSize;

        try {
            Long dayRankTotal = redisTemplate.opsForZSet().zCard(dayRankKey);
            Long allRankTotal = redisTemplate.opsForZSet().zCard(allRankKey);
            if ((dayRankTotal == null || dayRankTotal == 0) && (allRankTotal == null || allRankTotal == 0)) {
                return getHotPage(pageNo, pageSize);
            }

            LinkedHashMap<Integer, ArticleDO> orderedArticles = new LinkedHashMap<>();
            appendRankedArticles(dayRankKey, requiredCount, orderedArticles);
            if (orderedArticles.size() < requiredCount) {
                appendRankedArticles(allRankKey, requiredCount, orderedArticles);
            }

            List<ArticleDO> rankedArticles = sliceRankedArticles(orderedArticles, start, pageSize);
            if (rankedArticles.isEmpty()) {
                return getHotPage(pageNo, pageSize);
            }

            List<ArticleListVO> rankedList = new ArrayList<>(toBean(rankedArticles));
            Long refreshedAllRankTotal = redisTemplate.opsForZSet().zCard(allRankKey);
            Long refreshedDayRankTotal = redisTemplate.opsForZSet().zCard(dayRankKey);
            long total = refreshedAllRankTotal != null && refreshedAllRankTotal > 0
                    ? refreshedAllRankTotal
                    : refreshedDayRankTotal != null ? refreshedDayRankTotal : 0L;
            return new PageResult<>(rankedList, total);
        } catch (Exception e) {
            log.warn("Failed to load today hot page from redis, dayKey: {}, allKey: {}, pageNo: {}, pageSize: {}",
                    dayRankKey, allRankKey, pageNo, pageSize, e);
            return getHotPage(pageNo, pageSize);
        }
    }

    private PageResult<ArticleListVO> getRankedHotPage(String rankKey,
                                                       Integer pageNo,
                                                       Integer pageSize,
                                                       Function<HotFallbackRequest, PageResult<ArticleListVO>> fallbackSupplier) {
        if (pageNo == null || pageNo < 1 || pageSize == null || pageSize < 1) {
            return PageResult.empty();
        }

        HotFallbackRequest fallbackRequest = new HotFallbackRequest(pageNo, pageSize);
        try {
            Long rankTotal = redisTemplate.opsForZSet().zCard(rankKey);
            if (rankTotal == null || rankTotal == 0) {
                return fallbackSupplier.apply(fallbackRequest);
            }

            long start = (long) (pageNo - 1) * pageSize;
            List<ArticleDO> rankedArticles = loadRankedArticles(rankKey, start, pageSize);
            if (rankedArticles.isEmpty()) {
                return fallbackSupplier.apply(fallbackRequest);
            }

            List<ArticleListVO> rankedList = new ArrayList<>(toBean(rankedArticles));
            Long refreshedRankTotal = redisTemplate.opsForZSet().zCard(rankKey);
            long total = refreshedRankTotal != null ? refreshedRankTotal : rankTotal;
            return new PageResult<>(rankedList, total);
        } catch (Exception e) {
            log.warn("Failed to load ranked hot page from redis, key: {}, pageNo: {}, pageSize: {}", rankKey, pageNo, pageSize, e);
            return fallbackSupplier.apply(fallbackRequest);
        }
    }

    private List<ArticleDO> loadRankedArticles(String rankKey, long start, Integer pageSize) {
        long requiredCount = start + pageSize;
        LinkedHashMap<Integer, ArticleDO> orderedArticles = new LinkedHashMap<>();
        appendRankedArticles(rankKey, requiredCount, orderedArticles);
        return sliceRankedArticles(orderedArticles, start, pageSize);
    }

    private void appendRankedArticles(String rankKey, long targetCount, LinkedHashMap<Integer, ArticleDO> orderedArticles) {
        if (targetCount <= 0) {
            return;
        }

        long cursor = 0;
        int fetchSize = Math.max(Math.toIntExact(Math.min(targetCount * HOT_RANK_SCAN_MULTIPLIER, 200L)), 1);
        Set<Integer> currentKeySeen = new HashSet<>();

        while (orderedArticles.size() < targetCount) {
            Set<Object> rankedMembers = redisTemplate.opsForZSet().reverseRange(rankKey, cursor, cursor + fetchSize - 1);
            if (rankedMembers == null || rankedMembers.isEmpty()) {
                break;
            }

            List<RankedArticleMember> articleMembers = parseRankedArticleMembers(rankedMembers);
            if (!articleMembers.isEmpty()) {
                List<Integer> articleIds = articleMembers.stream()
                        .map(RankedArticleMember::articleId)
                        .toList();
                List<ArticleDO> articleDOS = articleMapper.selectBatchIds(articleIds);
                Map<Integer, ArticleDO> articleMap = articleDOS.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(ArticleDO::getId, Function.identity(), (left, right) -> left));

                for (RankedArticleMember articleMember : articleMembers) {
                    Integer articleId = articleMember.articleId();
                    ArticleDO article = articleMap.get(articleId);
                    if (article == null || article.getStatus() != ArticleStatus.PUBLISHED) {
                        evictInvalidRankedMember(rankKey, articleMember, article);
                        continue;
                    }
                    if (!currentKeySeen.add(articleId)) {
                        evictDuplicateRankedMember(rankKey, articleMember);
                        continue;
                    }
                    if (orderedArticles.containsKey(articleId)) {
                        continue;
                    }
                    orderedArticles.putIfAbsent(articleId, article);
                    if (orderedArticles.size() >= targetCount) {
                        break;
                    }
                }
            }

            cursor += rankedMembers.size();
            if (rankedMembers.size() < fetchSize) {
                break;
            }
        }
    }

    private List<ArticleDO> sliceRankedArticles(LinkedHashMap<Integer, ArticleDO> orderedArticles,
                                                long start,
                                                Integer pageSize) {
        if (orderedArticles.isEmpty()) {
            return Collections.emptyList();
        }
        return orderedArticles.values().stream()
                .skip(start)
                .limit(pageSize)
                .toList();
    }

    private List<RankedArticleMember> parseRankedArticleMembers(Set<Object> rankedMembers) {
        List<RankedArticleMember> articleMembers = new ArrayList<>(rankedMembers.size());
        for (Object rankedMember : rankedMembers) {
            try {
                articleMembers.add(new RankedArticleMember(Integer.valueOf(String.valueOf(rankedMember)), rankedMember));
            } catch (NumberFormatException e) {
                log.warn("Invalid article id in redis hot rank, member: {}", rankedMember);
            }
        }
        return articleMembers;
    }

    private void evictInvalidRankedMember(String rankKey, RankedArticleMember articleMember, ArticleDO article) {
        try {
            Long removed = redisTemplate.opsForZSet().remove(rankKey, articleMember.rawMember());
            if (removed != null && removed > 0) {
                String reason = article == null ? "missing" : "status=" + article.getStatus();
                log.info("Removed invalid hot rank member, key: {}, articleId: {}, reason: {}", rankKey, articleMember.articleId(), reason);
            }
        } catch (Exception e) {
            log.warn("Failed to evict invalid hot rank member, key: {}, articleId: {}", rankKey, articleMember.articleId(), e);
        }
    }

    private void evictDuplicateRankedMember(String rankKey, RankedArticleMember articleMember) {
        try {
            Long removed = redisTemplate.opsForZSet().remove(rankKey, articleMember.rawMember());
            if (removed != null && removed > 0) {
                log.info("Removed duplicate hot rank member, key: {}, articleId: {}", rankKey, articleMember.articleId());
            }
        } catch (Exception e) {
            log.warn("Failed to evict duplicate hot rank member, key: {}, articleId: {}", rankKey, articleMember.articleId(), e);
        }
    }

    private PageResult<ArticleListVO> getHotPageFallback(HotFallbackRequest request) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return toBean(request.pageNo(), request.pageSize(), Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .ge(ArticleDO::getCreateTime, thirtyDaysAgo)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    private PageResult<ArticleListVO> getTodayHotPageFallback(HotFallbackRequest request) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return toBean(request.pageNo(), request.pageSize(), Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .ge(ArticleDO::getCreateTime, todayStart)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    private record HotFallbackRequest(Integer pageNo, Integer pageSize) {
    }

    private record RankedArticleMember(Integer articleId, Object rawMember) {
    }

    /**
     * 根据分类ID获取文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param category 分类ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getCategoryPage(Integer pageNo, Integer pageSize, Integer category) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getCategory, category)
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 根据关键词搜索文章，匹配标题或摘要，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param like     搜索关键词
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getLikePage(Integer pageNo, Integer pageSize, String like) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .and(wrapper -> wrapper.like(ArticleDO::getTitle, like).or().like(ArticleDO::getAbstractText, like))
                .orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取系统推荐文章列表。
     * 返回本周内审核通过的文章，按创建时间倒序排列。
     * <p>
     * 注意：当前实现为降级策略，返回本周优质文章。
     * 完整的推荐算法应基于：
     * 1. 内容质量（点赞、收藏、阅读量综合评分）
     * 2. 发布时间衰减
     * 3. 分类多样性
     * 建议后续引入 Redis 缓存 + 定时任务计算推荐列表。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getSystemRecommendPage(Integer pageNo, Integer pageSize) {
        // 降级策略：返回所有审核通过的文章
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                        .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
//                .ge(ArticleDO::getCreateTime, weekStart)
//                .le(ArticleDO::getCreateTime, weekEnd)
                        .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取个性化推荐文章列表。
     * 基于用户兴趣推荐相关文章，当前返回最新发布的文章。
     * <p>
     * 注意：当前实现为降级策略，返回最新文章。
     * 完整的个性化推荐算法应基于：
     * 1. 用户历史阅读行为分析
     * 2. 用户兴趣标签匹配
     * 3. 协同过滤算法
     * 建议后续引入机器学习模型 + 用户画像系统。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getPersonalRecommendPage(Integer pageNo, Integer pageSize) {
        // 降级策略：返回最近7天内的已发布文章
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .ge(ArticleDO::getCreateTime, sevenDaysAgo)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取用户历史阅读文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getUserHistoryPage(Integer pageNo, Integer pageSize, Integer userId) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery().eq(ArticleDO::getUserId, userId).orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取用户收藏文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getUserCollectPage(Integer pageNo, Integer pageSize, Integer userId) {
        List<Integer> articleIdList = new ArrayList<>();
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getUserId, userId)
                .in(ArticleDO::getId, articleIdList)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取用户发布的文章列表，状态为已发布，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getUserArticlePage(Integer pageNo, Integer pageSize, Integer userId) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getUserId, userId)
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取用户草稿箱文章列表，状态为草稿，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getUserDraftArticlePage(Integer pageNo, Integer pageSize, Integer userId) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getUserId, userId)
                .eq(ArticleDO::getStatus, ArticleStatus.DRAFT)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取管理员审核文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getAdminArticlePage(Integer pageNo, Integer pageSize) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, ArticleStatus.PENDING_REVIEW)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 管理员全站文章分页查询，支持多条件筛选。
     *
     * @param request 查询条件
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getAdminFullPage(AdminArticlePageRequestDTO request) {
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
        // status: null=不限（排除已删除），有值=精确匹配
        if (request.getStatus() != null) {
            ArticleStatus targetStatus = resolveArticleStatus(request.getStatus());
            if (targetStatus != null) {
                wrapper.eq(ArticleDO::getStatus, targetStatus);
            }
        } else {
            wrapper.ne(ArticleDO::getStatus, ArticleStatus.DELETED);
        }
        wrapper.eq(request.getCategory() != null, ArticleDO::getCategory, request.getCategory());
        wrapper.eq(request.getContentType() != null, ArticleDO::getContentType, request.getContentType());
        wrapper.eq(request.getUserId() != null, ArticleDO::getUserId, request.getUserId());
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(ArticleDO::getTitle, request.getKeyword())
                    .or().like(ArticleDO::getAbstractText, request.getKeyword()));
        }
        if ("asc".equalsIgnoreCase(request.getOrderDirection())) {
            wrapper.orderByAsc(ArticleDO::getCreateTime);
        } else {
            wrapper.orderByDesc(ArticleDO::getCreateTime);
        }
        return toBean(request.getPageNo(), request.getPageSize(), wrapper);
    }

    private ArticleStatus resolveArticleStatus(Integer code) {
        for (ArticleStatus s : ArticleStatus.values()) {
            if (s.getStatus() == code) {
                return s;
            }
        }
        return null;
    }

    /**
     * 根据ID列表查询文章列表
     *
     * @param ids 文章ID列表
     * @return 文章列表
     */
    @Override
    public List<ArticleListVO> selectIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<ArticleDO> articleDOS = articleMapper.selectBatchIds(ids);
        return toBean(articleDOS);
    }

    @Override
    public PageResult<ArticleListVO> getContentTypePage(Integer pageNo, Integer pageSize, Integer contentType) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getContentType, contentType)
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    @Override
    public PageResult<ArticleListVO> getTagPage(Integer pageNo, Integer pageSize, Integer tagId) {
        List<Integer> articleIds = articleTagMapper.selectArticleIdsByTagId(tagId);
        if (articleIds == null || articleIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .in(ArticleDO::getId, articleIds)
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    @Override
    public PageResult<ArticleListVO> getMultiFilterPage(Integer pageNo, Integer pageSize, Integer contentType, Integer category, Integer tagId) {
        List<Integer> articleIds = null;

        // 如果有标签筛选，先获取标签关联的文章ID
        if (tagId != null) {
            articleIds = articleTagMapper.selectArticleIdsByTagId(tagId);
            if (articleIds == null || articleIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
        }

        // 构建查询条件
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(contentType != null, ArticleDO::getContentType, contentType)
                .eq(category != null, ArticleDO::getCategory, category)
                .in(articleIds != null, ArticleDO::getId, articleIds)
                .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    @Override
    public PageResult<ArticleListVO> relatedPage(Integer articleId, Integer pageNo, Integer pageSize) {
        // 获取当前文章
        ArticleDO currentArticle = articleMapper.selectById(articleId);
        if (currentArticle == null || currentArticle.getStatus() != ArticleStatus.PUBLISHED) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        // 获取当前文章的标签
        List<Integer> currentTagIds = tagService.getArticleTagIds(articleId);
        if (currentTagIds.isEmpty()) {
            // 无标签时，基于分类和内容类型推荐
            return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                    .eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED)
                    .ne(ArticleDO::getId, articleId)
                    .eq(currentArticle.getContentType() != null, ArticleDO::getContentType, currentArticle.getContentType())
                    .eq(currentArticle.getCategory() != null, ArticleDO::getCategory, currentArticle.getCategory())
                    .orderByDesc(ArticleDO::getCreateTime));
        }

        // 获取当前文章的标签信息（区分技术栈和学习路径）
        List<TagDO> currentTags = tagMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TagDO>()
                        .in(TagDO::getId, currentTagIds)
        );
        List<Integer> techTagIds = currentTags.stream().filter(t -> t.getType() == 1).map(TagDO::getId).toList();
        List<Integer> pathTagIds = currentTags.stream().filter(t -> t.getType() == 2).map(TagDO::getId).toList();

        // 获取有共同标签的文章ID及其标签交集
        // 先收集所有相关的文章ID
        Set<Integer> relatedArticleIds = new HashSet<>();
        for (Integer tagId : currentTagIds) {
            List<Integer> articleIds = articleTagMapper.selectArticleIdsByTagId(tagId);
            if (articleIds != null) {
                relatedArticleIds.addAll(articleIds);
            }
        }
        relatedArticleIds.remove(articleId); // 排除当前文章

        if (relatedArticleIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        // 获取这些文章的标签Map
        Map<Integer, List<Integer>> articleTagMap = articleTagMapper.selectTagIdMapByArticleIds(new ArrayList<>(relatedArticleIds));

        // 批量获取相关文章，避免 N+1 查询
        List<ArticleDO> relatedArticles = articleMapper.selectBatchIds(relatedArticleIds);
        Map<Integer, ArticleDO> articleDOMap = relatedArticles.stream()
                .collect(Collectors.toMap(ArticleDO::getId, Function.identity()));

        // 排除当前文章，计算每篇文章的推荐得分
        List<Map.Entry<Integer, Integer>> scoredArticles = new ArrayList<>(); // <articleId, score>
        for (Map.Entry<Integer, List<Integer>> entry : articleTagMap.entrySet()) {
            Integer relatedArticleId = entry.getKey();
            if (relatedArticleId.equals(articleId)) {
                continue;
            }
            List<Integer> relatedTagIds = entry.getValue();

            int score = 0;
            // 共同技术栈标签：每个 +3 分
            for (Integer techTagId : techTagIds) {
                if (relatedTagIds.contains(techTagId)) {
                    score += 3;
                }
            }
            // 共同学习路径标签：每个 +2 分
            for (Integer pathTagId : pathTagIds) {
                if (relatedTagIds.contains(pathTagId)) {
                    score += 2;
                }
            }
            // 相同内容类型：+1 分
            ArticleDO relatedArticle = articleDOMap.get(relatedArticleId);
            if (relatedArticle != null && relatedArticle.getStatus() == ArticleStatus.PUBLISHED) {
                if (currentArticle.getContentType() != null &&
                        currentArticle.getContentType().equals(relatedArticle.getContentType())) {
                    score += 1;
                }
                // 相同分类：+1 分
                if (currentArticle.getCategory() != null &&
                        currentArticle.getCategory().equals(relatedArticle.getCategory())) {
                    score += 1;
                }
                scoredArticles.add(Map.entry(relatedArticleId, score));
            }
        }

        // 按得分降序，得分相同时按创建时间降序
        scoredArticles.sort((a, b) -> {
            int cmp = b.getValue().compareTo(a.getValue());
            if (cmp != 0) return cmp;
            ArticleDO articleA = articleDOMap.get(a.getKey());
            ArticleDO articleB = articleDOMap.get(b.getKey());
            if (articleA == null || articleB == null) return 0;
            return articleB.getCreateTime().compareTo(articleA.getCreateTime());
        });

        // 取前 pageSize 条
        List<Integer> topArticleIds = scoredArticles.stream()
                .limit(pageSize)
                .map(Map.Entry::getKey)
                .toList();

        if (topArticleIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        List<ArticleListVO> result = selectIds(topArticleIds);
        // 保持排序顺序
        Map<Integer, ArticleListVO> articleMap = result.stream()
                .collect(Collectors.toMap(ArticleListVO::getId, v -> v));
        List<ArticleListVO> orderedResult = topArticleIds.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageResult<>(orderedResult, (long) orderedResult.size());
    }
}
