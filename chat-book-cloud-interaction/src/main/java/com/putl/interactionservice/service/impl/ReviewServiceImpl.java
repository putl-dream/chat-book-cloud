package com.putl.interactionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.interactionservice.entity.ReviewDO;
import com.putl.interactionservice.enums.ReviewStatus;
import com.putl.interactionservice.mapper.ReviewMapper;
import com.putl.interactionservice.service.ReviewService;
import com.putl.interactionservice.service.UserFootService;
import com.putl.interactionservice.controller.vo.ReviewAdminStatsVO;
import com.putl.interactionservice.controller.vo.ReviewListVO;
import com.putl.interactionservice.controller.vo.ReviewVO;
import com.putl.userservice.api.UserClient;
import com.putl.userservice.api.dto.UserResult;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, ReviewDO> implements ReviewService {
    private final ReviewMapper reviewMapper;
    private final UserClient userClient;
    private final UserFootService userFootService;

    @Override
    @Cacheable(value = "reviewListCache", key = "#articleId", unless = "#result == null || #result.isEmpty()")
    public List<ReviewListVO> getByArticleId(Integer articleId) {
        List<ReviewDO> dos = reviewMapper.selectList(Wrappers.<ReviewDO>lambdaQuery()
                .eq(ReviewDO::getTextId, articleId)
                .ne(ReviewDO::getStatus, ReviewStatus.DELETED.getCode()));
        if (CollectionUtils.isEmpty(dos)) {
            return Collections.emptyList();
        }

        Map<Integer, UserResult> userMap = getUserMap(dos.stream().map(ReviewDO::getUserId).distinct().toList());

        Map<Integer, ReviewListVO> map = new HashMap<>();
        List<ReviewListVO> header = new ArrayList<>();

        List<ReviewListVO> allComments = dos.stream().map(item -> getReviewVO(item, userMap)).toList();

        for (ReviewListVO comment : allComments) {
            if (comment.getParentId() == 0) {
                header.add(comment);
            }
            map.put(comment.getId(), comment);
        }

        for (ReviewListVO comment : allComments) {
            if (comment.getParentId() != 0 && map.containsKey(comment.getParentId())) {
                map.get(comment.getParentId()).getChildren().add(comment);
            }
        }
        return header;
    }

    @Override
    @Transactional
    @CacheEvict(value = "reviewListCache", key = "#reviewVO.articleId")
    public boolean save(ReviewVO reviewVO) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalStateException("用户信息未找到，请重新登录");
        }
        ReviewDO bean = new ReviewDO();
        bean.setUserId(Integer.parseInt(userId));
        bean.setTextId(reviewVO.getArticleId());
        bean.setParentId(reviewVO.getParentId());
        bean.setContent(reviewVO.getContent());
        boolean saved = reviewMapper.insert(bean) == 1;
        if (saved) {
            userFootService.recordComment(reviewVO.getArticleId(), Integer.parseInt(userId));
        }
        return saved;
    }

    private ReviewListVO getReviewVO(ReviewDO item, Map<Integer, UserResult> userMap) {
        UserResult user = userMap.get(item.getUserId());
        ReviewListVO rspVO = new ReviewListVO();
        rspVO.setId(item.getId());
        rspVO.setArticleId(item.getTextId());
        rspVO.setContent(item.getContent());
        rspVO.setParentId(item.getParentId());
        rspVO.setCreateTime(item.getCreateTime());
        if (user != null) {
            rspVO.setUsername(user.getUsername());
            rspVO.setHeaderImg(user.getPhoto());
        }
        rspVO.setChildren(new ArrayList<>());
        return rspVO;
    }

    // ==================== 后台评论治理 ====================

    @Override
    public IPage<ReviewVO> getAdminPage(Integer page, Integer size, Integer articleId,
            Integer userId, String keyword, Integer status,
            LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<ReviewDO> w = new LambdaQueryWrapper<>();
        w.eq(articleId != null, ReviewDO::getTextId, articleId);
        w.eq(userId != null, ReviewDO::getUserId, userId);
        w.eq(status != null, ReviewDO::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            w.like(ReviewDO::getContent, keyword);
        }
        w.ge(startTime != null, ReviewDO::getCreateTime, startTime);
        w.le(endTime != null, ReviewDO::getCreateTime, endTime);
        w.orderByDesc(ReviewDO::getCreateTime);
        IPage<ReviewDO> result = reviewMapper.selectPage(new Page<>(page, size), w);
        Map<Integer, UserResult> userMap = getUserMap(result.getRecords().stream()
                .map(ReviewDO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        Page<ReviewVO> pageResult = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        pageResult.setRecords(result.getRecords().stream().map(item -> toReviewVO(item, userMap)).toList());
        return pageResult;
    }

    @Override
    public ReviewAdminStatsVO getAdminStats() {
        long normalCount = countByStatus(ReviewStatus.NORMAL);
        long hiddenCount = countByStatus(ReviewStatus.HIDDEN);
        long deletedCount = countByStatus(ReviewStatus.DELETED);
        long totalCount = reviewMapper.selectCount(null);
        return ReviewAdminStatsVO.builder()
                .totalCount(totalCount)
                .normalCount(normalCount)
                .hiddenCount(hiddenCount)
                .deletedCount(deletedCount)
                .abnormalCount(hiddenCount + deletedCount)
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(Integer reviewId) {
        ReviewDO review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评论不存在");
        }
        review.setStatus(ReviewStatus.DELETED.getCode());
        review.setUpdateTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    @Override
    @Transactional
    public void hideReview(Integer reviewId) {
        ReviewDO review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评论不存在");
        }
        review.setStatus(ReviewStatus.HIDDEN.getCode());
        review.setUpdateTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    @Override
    @Transactional
    public void restoreReview(Integer reviewId) {
        ReviewDO review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评论不存在");
        }
        review.setStatus(ReviewStatus.NORMAL.getCode());
        review.setUpdateTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    private Map<Integer, UserResult> getUserMap(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, UserResult> userMap = new HashMap<>();
        try {
            CommonResult<List<UserResult>> batchResult = userClient.getUsersByIds(userIds);
            if (batchResult != null && batchResult.getData() != null) {
                for (UserResult userResult : batchResult.getData()) {
                    userMap.put(userResult.getId(), userResult);
                }
            }
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
        return userMap;
    }

    private long countByStatus(ReviewStatus status) {
        return reviewMapper.selectCount(Wrappers.<ReviewDO>lambdaQuery()
                .eq(ReviewDO::getStatus, status.getCode()));
    }

    private ReviewVO toReviewVO(ReviewDO item, Map<Integer, UserResult> userMap) {
        ReviewVO vo = new ReviewVO();
        vo.setId(item.getId());
        vo.setArticleId(item.getTextId());
        vo.setUserId(item.getUserId());
        vo.setParentId(item.getParentId());
        vo.setContent(item.getContent());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(item.getCreateTime());
        UserResult user = userMap.get(item.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setHeaderImg(user.getPhoto());
        }
        return vo;
    }
}
