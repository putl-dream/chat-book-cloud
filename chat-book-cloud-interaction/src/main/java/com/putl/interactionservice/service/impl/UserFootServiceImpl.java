package com.putl.interactionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.articleservice.api.dto.ArticleVO;
import com.putl.interactionservice.entity.ArticleStatDO;
import com.putl.interactionservice.entity.UserFootDO;
import com.putl.interactionservice.mapper.ArticleStatMapper;
import com.putl.interactionservice.mapper.ReviewMapper;
import com.putl.interactionservice.mapper.UserFootMapper;
import com.putl.interactionservice.mapper.dto.ArticleCommentCountAggregate;
import com.putl.interactionservice.mapper.dto.ArticleFootStatAggregate;
import com.putl.interactionservice.service.HotArticleRankService;
import com.putl.interactionservice.service.UserFootService;
import com.putl.interactionservice.controller.vo.NotificationVO;
import com.putl.interactionservice.controller.vo.UserFootListVO;
import com.putl.interactionservice.controller.vo.UserFootVO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFootServiceImpl extends ServiceImpl<UserFootMapper, UserFootDO> implements UserFootService {
    private final UserFootMapper userFootMapper;
    private final ArticleClient articleClient;
    private final ArticleStatMapper articleStatMapper;
    private final ReviewMapper reviewMapper;
    private final HotArticleRankService hotArticleRankService;

    @Override
    @Transactional
    public boolean addBrowse(Integer articleId, Integer userId) {
        if (articleId == null) return false;
        boolean shouldCountView = hotArticleRankService.tryAcquireViewToken(articleId, userId);
        if (userId == null || userId <= 0) {
            if (shouldCountView) {
                incrementViewCount(articleId);
                hotArticleRankService.recordView(articleId);
            }
            return true;
        }

        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getUserId, userId));

        if (foot == null) {
            ArticleVO article = getArticleDetail(articleId);
            Integer documentUserId = article != null ? article.getUserId() : null;
            UserFootDO build = UserFootDO.builder()
                .userId(userId)
                .documentId(articleId)
                .documentUserId(documentUserId)
                .collectionStat(0)
                .commentStat(0)
                .praiseStat(0)
                .readStat(1)
                .build();
            try {
                boolean saved = this.save(build);
                if (saved && shouldCountView) {
                    incrementViewCount(articleId);
                    hotArticleRankService.recordView(articleId);
                }
                return saved;
            } catch (DuplicateKeyException e) {
                log.debug("Duplicate browse foot ignored, articleId: {}, userId: {}", articleId, userId);
                if (shouldCountView) {
                    incrementViewCount(articleId);
                    hotArticleRankService.recordView(articleId);
                }
                return true;
            }
        }

        Integer readStat = foot.getReadStat() == null ? 0 : foot.getReadStat();
        if (readStat == 0) {
            userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate()
                .set(UserFootDO::getReadStat, 1)
                .set(UserFootDO::getUpdateTime, LocalDateTime.now())
                .eq(UserFootDO::getDocumentId, articleId)
                .eq(UserFootDO::getUserId, userId));
        }
        if (shouldCountView) {
            incrementViewCount(articleId);
            hotArticleRankService.recordView(articleId);
        }
        return true;
    }

    @Override
    @Transactional
    public int updateCollection(Integer articleId, Integer userId) {
        UserFootDO foot = ensureUserFoot(articleId, userId);
        int current = foot.getCollectionStat() == null ? 0 : foot.getCollectionStat();
        int status = current == 1 ? 0 : 1;
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate()
            .set(UserFootDO::getCollectionStat, status)
            .set(UserFootDO::getUpdateTime, LocalDateTime.now())
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getUserId, userId));
        changeCollectCount(articleId, status == 1 ? 1 : -1);
        hotArticleRankService.recordCollection(articleId, status == 1);
        return status;
    }

    @Override
    @Transactional
    public int updateComment(Integer articleId, Integer userId) {
        UserFootDO foot = ensureUserFoot(articleId, userId);
        int current = foot.getCommentStat() == null ? 0 : foot.getCommentStat();
        if (current == 1) {
            return 1;
        }
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate()
            .set(UserFootDO::getCommentStat, 1)
            .set(UserFootDO::getUpdateTime, LocalDateTime.now())
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getUserId, userId));
        return 1;
    }

    @Override
    @Transactional
    public void recordComment(Integer articleId, Integer userId) {
        updateComment(articleId, userId);
        changeCommentCount(articleId, 1);
        hotArticleRankService.recordComment(articleId);
    }

    @Override
    @Transactional
    public int updatePraise(Integer articleId, Integer userId) {
        UserFootDO foot = ensureUserFoot(articleId, userId);
        int current = foot.getPraiseStat() == null ? 0 : foot.getPraiseStat();
        int status = current == 1 ? 0 : 1;
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate()
            .set(UserFootDO::getPraiseStat, status)
            .set(UserFootDO::getUpdateTime, LocalDateTime.now())
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getUserId, userId));
        changePraiseCount(articleId, status == 1 ? 1 : -1);
        hotArticleRankService.recordPraise(articleId, status == 1);
        return status;
    }

    @Override
    public UserFootVO getUserFoot(Integer articleId, Integer userId) {
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        ArticleStatDO stat = getArticleStat(articleId);
        long viewCount = stat != null && stat.getViewCount() != null ? stat.getViewCount() : 0L;
        if (foot == null) {
            foot = UserFootDO.builder().praiseStat(0).collectionStat(0).build();
        }
        return UserFootVO.builder()
            .articleId(articleId)
            .userId(userId)
            .praiseStat(foot.getPraiseStat())
            .collectStat(foot.getCollectionStat())
            .viewCount(viewCount)
            .build();
    }

    @Override
    public UserFootListVO getUserFootList(Integer articleId) {
        ArticleStatDO stat = getArticleStat(articleId);
        long viewCount = stat != null && stat.getViewCount() != null ? stat.getViewCount() : 0L;
        long collectCount = stat != null && stat.getCollectCount() != null ? stat.getCollectCount() : 0L;
        long praiseCount = stat != null && stat.getPraiseCount() != null ? stat.getPraiseCount() : 0L;
        long commentCount = stat != null && stat.getCommentCount() != null ? stat.getCommentCount() : 0L;
        return UserFootListVO.builder()
            .articleId(articleId)
            .viewCount(viewCount)
            .collectCount(collectCount)
            .praiseCount(praiseCount)
            .commentCount(commentCount)
            .build();
    }

    @Override
    public List<UserFootListVO> getUserFootListByArticleIds(List<Integer> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> uniqueArticleIds = articleIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        List<ArticleStatDO> stats = uniqueArticleIds.isEmpty()
            ? Collections.emptyList()
            : articleStatMapper.selectList(
                Wrappers.<ArticleStatDO>lambdaQuery().in(ArticleStatDO::getArticleId, uniqueArticleIds));
        Map<Integer, ArticleStatDO> statMap = stats.stream()
                .collect(Collectors.toMap(ArticleStatDO::getArticleId, Function.identity()));

        List<Integer> missingArticleIds = uniqueArticleIds.stream()
            .filter(articleId -> !statMap.containsKey(articleId))
            .toList();
        if (!missingArticleIds.isEmpty()) {
            statMap.putAll(rebuildMissingArticleStats(missingArticleIds));
        }

        List<UserFootListVO> result = new ArrayList<>();
        for (Integer articleId : articleIds) {
            ArticleStatDO stat = statMap.get(articleId);
            result.add(UserFootListVO.builder()
                    .articleId(articleId)
                    .viewCount(stat != null && stat.getViewCount() != null ? stat.getViewCount() : 0L)
                    .collectCount(stat != null && stat.getCollectCount() != null ? stat.getCollectCount() : 0L)
                    .praiseCount(stat != null && stat.getPraiseCount() != null ? stat.getPraiseCount() : 0L)
                    .commentCount(stat != null && stat.getCommentCount() != null ? stat.getCommentCount() : 0L)
                    .build());
        }
        return result;
    }

    @Override
    public List<ArticleListVO> getHistory(Integer userId, Integer page, Integer size) {
        Page<UserFootDO> pages = userFootMapper.selectPage(new Page<>(page, size), Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getUserId, userId)
            .eq(UserFootDO::getReadStat, 1)
            .orderByDesc(UserFootDO::getUpdateTime, UserFootDO::getCreateTime));
        if (pages == null || pages.getRecords().isEmpty()) return null;
        List<Integer> ids = pages.getRecords().stream().map(UserFootDO::getDocumentId).toList();
        CommonResult<List<ArticleListVO>> result = articleClient.selectIds(ids);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            log.warn("Failed to fetch history articles from article service, userId: {}, articleIds: {}", userId, ids);
            return Collections.emptyList();
        }
        return result.getData();
    }

    @Override
    public List<NotificationVO> getNotifications(int userId) {
        List<UserFootDO> praiseList = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getPraiseStat, 1));
        List<UserFootDO> collectList = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getCollectionStat, 1));
        List<UserFootDO> commentList = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getCommentStat, 1));
        List<UserFootDO> browseList = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getReadStat, 1));

        List<UserFootDO> allFeet = new ArrayList<>();
        allFeet.addAll(praiseList);
        allFeet.addAll(collectList);
        allFeet.addAll(commentList);
        allFeet.addAll(browseList);

        if (allFeet.isEmpty()) return new ArrayList<>();

        List<Integer> articleIds = allFeet.stream().map(UserFootDO::getDocumentId).distinct().collect(Collectors.toList());
        CommonResult<List<ArticleListVO>> result = articleClient.selectIds(articleIds);
        List<ArticleListVO> articles = (result != null && result.isSuccess()) ? result.getData() : Collections.emptyList();
        if (result == null || !result.isSuccess() || result.getData() == null) {
            log.warn("Failed to fetch notification articles from article service, userId: {}, articleIds: {}", userId, articleIds);
        }
        Map<Integer, String> articleTitleMap = articles == null ? Map.of() : articles.stream().collect(Collectors.toMap(ArticleListVO::getId, ArticleListVO::getTitle, (a, b) -> a));

        List<NotificationVO> notifications = new ArrayList<>();
        for (UserFootDO foot : praiseList) {
            notifications.add(NotificationVO.builder().id(foot.getId()).senderId(foot.getUserId()).actionType("PRAISE").articleId(foot.getDocumentId()).articleTitle(articleTitleMap.get(foot.getDocumentId())).createTime(foot.getUpdateTime() != null ? foot.getUpdateTime() : foot.getCreateTime()).build());
        }
        for (UserFootDO foot : collectList) {
            notifications.add(NotificationVO.builder().id(foot.getId()).senderId(foot.getUserId()).actionType("COLLECT").articleId(foot.getDocumentId()).articleTitle(articleTitleMap.get(foot.getDocumentId())).createTime(foot.getUpdateTime() != null ? foot.getUpdateTime() : foot.getCreateTime()).build());
        }
        for (UserFootDO foot : commentList) {
            notifications.add(NotificationVO.builder().id(foot.getId()).senderId(foot.getUserId()).actionType("COMMENT").articleId(foot.getDocumentId()).articleTitle(articleTitleMap.get(foot.getDocumentId())).createTime(foot.getUpdateTime() != null ? foot.getUpdateTime() : foot.getCreateTime()).build());
        }
        for (UserFootDO foot : browseList) {
            notifications.add(NotificationVO.builder().id(foot.getId()).senderId(foot.getUserId()).actionType("BROWSE").articleId(foot.getDocumentId()).articleTitle(articleTitleMap.get(foot.getDocumentId())).createTime(foot.getUpdateTime() != null ? foot.getUpdateTime() : foot.getCreateTime()).build());
        }

        notifications.sort(Comparator.comparing(NotificationVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return notifications;
    }

    private boolean dataNULL(Integer articleId, Integer userId) {
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        return foot == null;
    }

    private UserFootDO ensureUserFoot(Integer articleId, Integer userId) {
        if (articleId == null || userId == null) {
            throw new IllegalArgumentException("articleId/userId is null");
        }
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getUserId, userId));
        if (foot != null) {
            return foot;
        }
        ArticleVO article = getArticleDetail(articleId);
        Integer documentUserId = article != null ? article.getUserId() : null;
        UserFootDO build = UserFootDO.builder()
            .userId(userId)
            .documentId(articleId)
            .documentUserId(documentUserId)
            .collectionStat(0)
            .commentStat(0)
            .praiseStat(0)
            .readStat(0)
            .build();
        userFootMapper.insert(build);
        return build;
    }

    private ArticleStatDO getArticleStat(Integer articleId) {
        if (articleId == null) return null;
        return articleStatMapper.selectOne(Wrappers.<ArticleStatDO>lambdaQuery().eq(ArticleStatDO::getArticleId, articleId));
    }

    private ArticleVO getArticleDetail(Integer articleId) {
        CommonResult<ArticleVO> result = articleClient.queryArticle(articleId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            log.warn("Failed to fetch article detail from article service, articleId: {}", articleId);
            return null;
        }
        return result.getData();
    }

    private ArticleStatDO ensureArticleStat(Integer articleId) {
        ArticleStatDO stat = getArticleStat(articleId);
        if (stat != null) return stat;
        long viewCount = this.count(Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getReadStat, 1));
        if (viewCount == 0) {
            viewCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId));
        }
        long praiseCount = this.count(Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getPraiseStat, 1));
        long collectCount = this.count(Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getCollectionStat, 1));
        long commentCount = reviewMapper.selectCount(Wrappers.<com.putl.interactionservice.entity.ReviewDO>lambdaQuery()
            .eq(com.putl.interactionservice.entity.ReviewDO::getTextId, articleId));
        ArticleStatDO build = ArticleStatDO.builder()
            .articleId(articleId)
            .viewCount(viewCount)
            .praiseCount(praiseCount)
            .commentCount(commentCount)
            .collectCount(collectCount)
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();
        try {
            articleStatMapper.insert(build);
        } catch (Exception ignored) {
            // ignore duplicate insert in concurrent scenarios
        }
        return build;
    }

    private Map<Integer, ArticleStatDO> rebuildMissingArticleStats(List<Integer> missingArticleIds) {
        Map<Integer, ArticleStatDO> rebuiltStats = new HashMap<>();
        List<ArticleFootStatAggregate> footAggregates = userFootMapper.aggregateArticleStats(missingArticleIds);
        if (footAggregates == null) {
            footAggregates = Collections.emptyList();
        }
        Map<Integer, ArticleFootStatAggregate> footStatMap = footAggregates.stream()
            .filter(Objects::nonNull)
            .filter(item -> item.getArticleId() != null)
            .collect(Collectors.toMap(ArticleFootStatAggregate::getArticleId, Function.identity(), (left, right) -> left));
        List<ArticleCommentCountAggregate> commentAggregates = reviewMapper.countByArticleIds(missingArticleIds);
        if (commentAggregates == null) {
            commentAggregates = Collections.emptyList();
        }
        Map<Integer, Long> commentCountMap = commentAggregates.stream()
            .filter(Objects::nonNull)
            .filter(item -> item.getArticleId() != null)
            .collect(Collectors.toMap(
                ArticleCommentCountAggregate::getArticleId,
                item -> defaultLong(item.getCommentCount()),
                Long::max
            ));

        LocalDateTime now = LocalDateTime.now();
        for (Integer articleId : missingArticleIds) {
            ArticleFootStatAggregate aggregate = footStatMap.get(articleId);
            long totalCount = aggregate == null ? 0L : defaultLong(aggregate.getTotalCount());
            long readCount = aggregate == null ? 0L : defaultLong(aggregate.getReadCount());
            ArticleStatDO rebuiltStat = ArticleStatDO.builder()
                .articleId(articleId)
                .viewCount(readCount > 0 ? readCount : totalCount)
                .praiseCount(aggregate == null ? 0L : defaultLong(aggregate.getPraiseCount()))
                .commentCount(commentCountMap.getOrDefault(articleId, 0L))
                .collectCount(aggregate == null ? 0L : defaultLong(aggregate.getCollectCount()))
                .createTime(now)
                .updateTime(now)
                .build();
            try {
                articleStatMapper.insert(rebuiltStat);
            } catch (Exception e) {
                log.debug("Concurrent articleStat insert ignored, articleId: {}", articleId, e);
            }
            rebuiltStats.put(articleId, rebuiltStat);
        }
        return rebuiltStats;
    }

    private void incrementViewCount(Integer articleId) {
        ensureArticleStat(articleId);
        articleStatMapper.update(null, new UpdateWrapper<ArticleStatDO>()
            .eq("article_id", articleId)
            .setSql("view_count = view_count + 1, update_time = NOW()"));
    }

    private void changePraiseCount(Integer articleId, int delta) {
        changeCount(articleId, "praise_count", delta);
    }

    private void changeCollectCount(Integer articleId, int delta) {
        changeCount(articleId, "collect_count", delta);
    }

    private void changeCommentCount(Integer articleId, int delta) {
        changeCount(articleId, "comment_count", delta);
    }

    private void changeCount(Integer articleId, String column, int delta) {
        if (delta == 0) return;
        ensureArticleStat(articleId);
        articleStatMapper.update(null, new UpdateWrapper<ArticleStatDO>()
            .eq("article_id", articleId)
            .setSql(column + " = GREATEST(" + column + " + (" + delta + "), 0), update_time = NOW()"));
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
