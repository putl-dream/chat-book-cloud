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
import com.putl.interactionservice.mapper.UserFootMapper;
import com.putl.interactionservice.service.UserFootService;
import com.putl.interactionservice.vo.NotificationVO;
import com.putl.interactionservice.vo.UserFootListVO;
import com.putl.interactionservice.vo.UserFootVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFootServiceImpl extends ServiceImpl<UserFootMapper, UserFootDO> implements UserFootService {
    private final UserFootMapper userFootMapper;
    private final ArticleClient articleClient;
    private final ArticleStatMapper articleStatMapper;

    @Override
    @Transactional
    public boolean addBrowse(Integer articleId, Integer userId) {
        if (articleId == null) return false;
        if (userId == null || userId <= 0) {
            incrementViewCount(articleId);
            return true;
        }

        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getUserId, userId));

        if (foot == null) {
            ArticleVO article = articleClient.queryArticle(articleId).getData();
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
            boolean saved = this.save(build);
            if (saved) {
                incrementViewCount(articleId);
            }
            return saved;
        }

        Integer readStat = foot.getReadStat() == null ? 0 : foot.getReadStat();
        if (readStat == 0) {
            userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate()
                .set(UserFootDO::getReadStat, 1)
                .set(UserFootDO::getUpdateTime, LocalDateTime.now())
                .eq(UserFootDO::getDocumentId, articleId)
                .eq(UserFootDO::getUserId, userId));
            incrementViewCount(articleId);
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
        changeCommentCount(articleId, 1);
        return 1;
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
    public List<ArticleListVO> getHistory(Integer userId, Integer page, Integer size) {
        Page<UserFootDO> pages = userFootMapper.selectPage(new Page<>(page, size), Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getUserId, userId)
            .eq(UserFootDO::getReadStat, 1)
            .orderByDesc(UserFootDO::getUpdateTime, UserFootDO::getCreateTime));
        if (pages == null || pages.getRecords().isEmpty()) return null;
        List<Integer> ids = pages.getRecords().stream().map(UserFootDO::getDocumentId).toList();
        return articleClient.selectIds(ids).getData();
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
        List<ArticleListVO> articles = articleClient.selectIds(articleIds).getData();
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
        ArticleVO article = articleClient.queryArticle(articleId).getData();
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
        long commentCount = this.count(Wrappers.<UserFootDO>lambdaQuery()
            .eq(UserFootDO::getDocumentId, articleId)
            .eq(UserFootDO::getCommentStat, 1));
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
}
