package com.putl.interactionservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.articleservice.api.dto.ArticleVO;
import com.putl.interactionservice.entity.UserFootDO;
import com.putl.interactionservice.mapper.UserFootMapper;
import com.putl.interactionservice.service.UserFootService;
import com.putl.interactionservice.vo.NotificationVO;
import com.putl.interactionservice.vo.UserFootListVO;
import com.putl.interactionservice.vo.UserFootVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFootServiceImpl extends ServiceImpl<UserFootMapper, UserFootDO> implements UserFootService {
    private final UserFootMapper userFootMapper;
    private final ArticleClient articleClient;

    @Override
    @Transactional
    public boolean addBrowse(Integer articleId, Integer userId) {
        if (!dataNULL(articleId, userId)) return false;
        ArticleVO article = articleClient.queryArticle(articleId).getData();
        UserFootDO build = UserFootDO.builder().userId(userId).documentId(articleId).documentUserId(article.getUserId()).build();
        return this.save(build);
    }

    @Override
    @Transactional
    public int updateCollection(Integer articleId, Integer userId) {
        if (dataNULL(articleId, userId)) {
            addBrowse(articleId, userId);
        }
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        int status = foot.getCollectionStat() == 1 ? 0 : 1;
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate().set(UserFootDO::getCollectionStat, status).eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        return status;
    }

    @Override
    @Transactional
    public int updateComment(Integer articleId, Integer userId) {
        if (dataNULL(articleId, userId)) {
            addBrowse(articleId, userId);
        }
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        int status = foot.getCommentStat() == 1 ? 0 : 1;
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate().set(UserFootDO::getCommentStat, status).eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        return status;
    }

    @Override
    @Transactional
    public int updatePraise(Integer articleId, Integer userId) {
        if (dataNULL(articleId, userId)) {
            addBrowse(articleId, userId);
        }
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        int status = foot.getPraiseStat() == 1 ? 0 : 1;
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate().set(UserFootDO::getPraiseStat, status).eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        return status;
    }

    @Override
    public UserFootVO getUserFoot(Integer articleId, Integer userId) {
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        long viewCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId));
        if (foot == null) foot = UserFootDO.builder().praiseStat(0).collectionStat(0).build();
        return UserFootVO.builder().articleId(articleId).userId(userId).praiseStat(foot.getPraiseStat()).collectStat(foot.getCollectionStat()).viewCount(viewCount).build();
    }

    @Override
    public UserFootListVO getUserFootList(Integer articleId) {
        long viewCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId));
        long collectCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getCollectionStat, 1));
        long praiseCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getPraiseStat, 1));
        long commentCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getCommentStat, 1));
        return UserFootListVO.builder().articleId(articleId).viewCount(viewCount).collectCount(collectCount).praiseCount(praiseCount).commentCount(commentCount).build();
    }

    @Override
    public List<ArticleListVO> getHistory(Integer userId, Integer page, Integer size) {
        Page<UserFootDO> pages = userFootMapper.selectPage(new Page<>(page, size), Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getUserId, userId));
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
}
