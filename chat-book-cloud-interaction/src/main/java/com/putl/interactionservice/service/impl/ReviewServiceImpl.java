package com.putl.interactionservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.interactionservice.entity.ReviewDO;
import com.putl.interactionservice.mapper.ReviewMapper;
import com.putl.interactionservice.service.ReviewService;
import com.putl.interactionservice.vo.ReviewListVO;
import com.putl.interactionservice.vo.ReviewVO;
import com.putl.userservice.api.UserClient;
import com.putl.userservice.api.dto.UserInfoDTO;
import fun.amireux.chat.book.framework.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, ReviewDO> implements ReviewService {
    private final ReviewMapper reviewMapper;
    private final UserClient userClient;

    @Override
    public List<ReviewListVO> getByArticleId(Integer articleId) {
        List<ReviewDO> dos = reviewMapper.selectList(Wrappers.<ReviewDO>lambdaQuery().eq(ReviewDO::getTextId, articleId));
        if (CollectionUtils.isEmpty(dos)) {
            return Collections.emptyList();
        }

        Map<Integer, ReviewListVO> map = new HashMap<>();
        List<ReviewListVO> header = new ArrayList<>();

        List<ReviewListVO> allComments = dos.stream().map(this::getReviewVO).toList();

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
        return reviewMapper.insert(bean) == 1;
    }

    private ReviewListVO getReviewVO(ReviewDO item) {
        UserInfoDTO user = userClient.getUserInfo(item.getUserId()).getData();
        ReviewListVO rspVO = new ReviewListVO();
        rspVO.setId(item.getId());
        rspVO.setArticleId(item.getTextId());
        rspVO.setContent(item.getContent());
        rspVO.setParentId(item.getParentId());
        rspVO.setCreateTime(item.getCreateTime());
        rspVO.setUsername(user.getUsername());
        rspVO.setHeaderImg(user.getPhoto());
        rspVO.setChildren(new ArrayList<>());
        return rspVO;
    }
}
