package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.articleservice.api.dto.ArticleVO;
import com.putl.userservice.controller.vo.NotificationVO;
import com.putl.userservice.controller.vo.UserFootListVO;
import com.putl.userservice.controller.vo.UserFootVO;
import com.putl.userservice.mapper.UserFootMapper;
import com.putl.userservice.mapper.entity.UserFootDO;
import com.putl.userservice.service.UserFootService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * [UserFoot]表服务接口  用户足迹表
 *
 * @since 2025-01-17 16:00:13
 */

@Service
@RequiredArgsConstructor
public class UserFootServiceImpl extends ServiceImpl<UserFootMapper, UserFootDO> implements UserFootService {
    private final UserFootMapper userFootMapper;
    private final ArticleClient articleClient;

    @Override
    @Transactional
    public boolean addBrowse(Integer articleId, Integer userId){
        if (!dataNULL(articleId, userId)) return false;
        ArticleVO article = articleClient.queryArticle(articleId).getData();
        UserFootDO build = UserFootDO.builder().userId(userId).documentId(articleId).documentUserId(article.getUserId()).build();
        return this.save(build);
    }


    @Override
    @Transactional
    public int updateCollection(Integer articleId, Integer userId){
        // upsert: 若无足迹记录则先初始化，再切换收藏状态
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
    public int updateComment(Integer articleId, Integer userId){
        // upsert: 若无足迹记录则先初始化，再切换评论状态
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
    public int updatePraise(Integer articleId, Integer userId){
        // upsert: 若无足迹记录则先初始化，再切换点赞状态
        if (dataNULL(articleId, userId)) {
            addBrowse(articleId, userId);
        }
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        int status = foot.getPraiseStat() == 1 ? 0 : 1;
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate().set(UserFootDO::getPraiseStat, status).eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        return status;
    }

    /**
     * @param articleId
     * @param userId
     * @return
     */
    @Override
    public UserFootVO getUserFoot(Integer articleId, Integer userId){
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        long viewCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId));
        if (foot == null) foot = UserFootDO.builder().praiseStat(0).collectionStat(0).build();
        return UserFootVO.builder().articleId(articleId).userId(userId).praiseStat(foot.getPraiseStat()).collectStat(foot.getCollectionStat()).viewCount(viewCount).build();
    }

    /**
     * @param articleId
     * @return
     */
    @Override
    public UserFootListVO getUserFootList(Integer articleId){
        long viewCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId));
        long collectCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getCollectionStat, 1));
        long PraiseCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getPraiseStat, 1));
        long commentCount = this.count(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getCommentStat, 1));
        return UserFootListVO.builder().articleId(articleId).viewCount(viewCount).collectCount(collectCount).praiseCount(PraiseCount).commentCount(commentCount).build();
    }

    @Override
    public List<ArticleListVO> getHistory(Integer userId, Integer page, Integer size){
        Page<UserFootDO> pages = userFootMapper.selectPage(new Page<>(page, size), Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getUserId, userId));
        if (pages == null || pages.getRecords().isEmpty()) return null;
        // 获取文章id
        List<Integer> ids = pages.getRecords().stream().map(UserFootDO::getDocumentId).toList();
        return articleClient.selectIds(ids).getData();
    }

    /**
     * @deprecated 使用 {@link #getNotifications(int)} 替代
     */
    @Deprecated
    @Override
    public List<ArticleListVO> getMessage(int userId){
        List<UserFootDO> pages1 = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getPraiseStat, 1));
        List<UserFootDO> pages2 = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getCollectionStat, 1));
        List<UserFootDO> pages3 = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getReadStat, 1));
        List<UserFootDO> list = new ArrayList<>(Stream.of(pages1, pages2, pages3).flatMap(Collection::stream).toList());
        list.sort(Comparator.comparing(UserFootDO::getCreateTime));
        return articleClient.selectIds(list.stream().map(UserFootDO::getDocumentId).toList()).getData();
    }

    /**
     * P0 Fix: 获取用户收到的互动通知（点赞/收藏/评论/浏览）
     * <p>原 getMessage() 只返回文章列表，丢失了操作人、操作类型、时间等关键信息。</p>
     * <p>本方法按足迹记录中的行为字段分别查询，组装成带语义的 NotificationVO 列表。</p>
     *
     * @param userId 文章作者 ID（即接收通知的人）
     * @return 按时间倒序的通知列表
     */
    @Override
    public List<NotificationVO> getNotifications(int userId) {
        // 1. 分别查询对该作者文章产生点赞/收藏/评论/浏览的足迹记录
        List<UserFootDO> praiseList = userFootMapper.selectList(
                Wrappers.<UserFootDO>lambdaQuery()
                        .eq(UserFootDO::getDocumentUserId, userId)
                        .eq(UserFootDO::getPraiseStat, 1));
        List<UserFootDO> collectList = userFootMapper.selectList(
                Wrappers.<UserFootDO>lambdaQuery()
                        .eq(UserFootDO::getDocumentUserId, userId)
                        .eq(UserFootDO::getCollectionStat, 1));
        List<UserFootDO> commentList = userFootMapper.selectList(
                Wrappers.<UserFootDO>lambdaQuery()
                        .eq(UserFootDO::getDocumentUserId, userId)
                        .eq(UserFootDO::getCommentStat, 1));
        List<UserFootDO> browseList = userFootMapper.selectList(
                Wrappers.<UserFootDO>lambdaQuery()
                        .eq(UserFootDO::getDocumentUserId, userId)
                        .eq(UserFootDO::getReadStat, 1));

        // 2. 收集所有涉及的文章 ID，批量 Feign 查询文章信息，避免 N+1 问题
        List<UserFootDO> allFeet = new ArrayList<>();
        allFeet.addAll(praiseList);
        allFeet.addAll(collectList);
        allFeet.addAll(commentList);
        allFeet.addAll(browseList);

        if (allFeet.isEmpty()) return new ArrayList<>();

        List<Integer> articleIds = allFeet.stream()
                .map(UserFootDO::getDocumentId)
                .distinct()
                .collect(Collectors.toList());

        List<ArticleListVO> articles = articleClient.selectIds(articleIds).getData();
        // 以文章 ID 建立 Map，方便后续 O(1) 查标题
        Map<Integer, String> articleTitleMap = articles == null ? Map.of() :
                articles.stream().collect(Collectors.toMap(ArticleListVO::getId, ArticleListVO::getTitle, (a, b) -> a));

        // 3. 按足迹类型分别组装 NotificationVO
        List<NotificationVO> notifications = new ArrayList<>();

        for (UserFootDO foot : praiseList) {
            notifications.add(NotificationVO.builder()
                    .id(foot.getId())
                    .senderId(foot.getUserId())
                    .actionType("PRAISE")
                    .articleId(foot.getDocumentId())
                    .articleTitle(articleTitleMap.get(foot.getDocumentId()))
                    .createTime(foot.getUpdateTime() != null ? foot.getUpdateTime() : foot.getCreateTime())
                    .build());
        }
        for (UserFootDO foot : collectList) {
            notifications.add(NotificationVO.builder()
                    .id(foot.getId())
                    .senderId(foot.getUserId())
                    .actionType("COLLECT")
                    .articleId(foot.getDocumentId())
                    .articleTitle(articleTitleMap.get(foot.getDocumentId()))
                    .createTime(foot.getUpdateTime() != null ? foot.getUpdateTime() : foot.getCreateTime())
                    .build());
        }
        for (UserFootDO foot : commentList) {
            notifications.add(NotificationVO.builder()
                    .id(foot.getId())
                    .senderId(foot.getUserId())
                    .actionType("COMMENT")
                    .articleId(foot.getDocumentId())
                    .articleTitle(articleTitleMap.get(foot.getDocumentId()))
                    .createTime(foot.getUpdateTime() != null ? foot.getUpdateTime() : foot.getCreateTime())
                    .build());
        }
        for (UserFootDO foot : browseList) {
            notifications.add(NotificationVO.builder()
                    .id(foot.getId())
                    .senderId(foot.getUserId())
                    .actionType("BROWSE")
                    .articleId(foot.getDocumentId())
                    .articleTitle(articleTitleMap.get(foot.getDocumentId()))
                    .createTime(foot.getUpdateTime() != null ? foot.getUpdateTime() : foot.getCreateTime())
                    .build());
        }

        // 4. 按时间倒序（最新的通知排最前）
        notifications.sort(Comparator.comparing(NotificationVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return notifications;
    }

    /**
     * 判断用户足迹记录是否不存在
     * @return true = 记录不存在; false = 记录已存在
     */
    private boolean dataNULL(Integer articleId, Integer userId){
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        return foot == null;
    }
}

