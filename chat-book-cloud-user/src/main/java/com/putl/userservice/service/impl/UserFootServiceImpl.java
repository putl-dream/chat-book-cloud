package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.articleservice.api.dto.ArticleVO;
import com.putl.userservice.controller.vo.UserFootListVO;
import com.putl.userservice.controller.vo.UserFootVO;
import com.putl.userservice.mapper.UserFootMapper;
import com.putl.userservice.mapper.entity.UserFootDO;
import com.putl.userservice.service.UserFootService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
    public boolean addBrowse(Integer articleId, Integer userId){
        if (!dataNULL(articleId, userId)) return false;
        ArticleVO article = articleClient.queryArticle(articleId).getData();
        UserFootDO build = UserFootDO.builder().userId(userId).documentId(articleId).documentUserId(article.getUserId()).build();
        return this.save(build);
    }


    @Override
    public int updateCollection(Integer articleId, Integer userId){
        if (dataNULL(articleId, userId)) return -1;
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        int status = foot.getCollectionStat() == 1 ? 0 : 1;
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate().set(UserFootDO::getCollectionStat, status).eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        return status;
    }

    @Override
    public int updateComment(Integer articleId, Integer userId){
        if (dataNULL(articleId, userId)) return -1;
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        if (foot == null) return -1;
        int status = foot.getCommentStat() == 1 ? 0 : 1;
        userFootMapper.update(Wrappers.<UserFootDO>lambdaUpdate().set(UserFootDO::getCommentStat, status).eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        return status;
    }

    @Override
    public int updatePraise(Integer articleId, Integer userId){
        if (dataNULL(articleId, userId)) return -1;
        System.err.println("articleId => " + articleId);
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
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @Override
    public List<ArticleListVO> getMessage(int userId){
        List<UserFootDO> pages1 = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getPraiseStat, 1));
        List<UserFootDO> pages2 = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getCollectionStat, 1));
        List<UserFootDO> pages3 = userFootMapper.selectList(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentUserId, userId).eq(UserFootDO::getReadStat, 1));
        List<UserFootDO> list = new java.util.ArrayList<>(Stream.of(pages1, pages2, pages3).flatMap(Collection::stream).toList());
        list.sort(Comparator.comparing(UserFootDO::getCreateTime));
        return articleClient.selectIds(list.stream().map(UserFootDO::getDocumentId).toList()).getData();
    }

    public boolean dataNULL(Integer articleId, Integer userId){
        UserFootDO foot = userFootMapper.selectOne(Wrappers.<UserFootDO>lambdaQuery().eq(UserFootDO::getDocumentId, articleId).eq(UserFootDO::getUserId, userId));
        System.out.println("foot = " + foot);
        return foot == null;
    }
}

