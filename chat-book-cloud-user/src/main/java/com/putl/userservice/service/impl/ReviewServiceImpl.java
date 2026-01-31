package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.userservice.common.ReqInfoContext;
import com.putl.userservice.controller.vo.ReviewListVO;
import com.putl.userservice.controller.vo.ReviewVO;
import com.putl.userservice.mapper.ReviewMapper;
import com.putl.userservice.mapper.entity.ReviewDO;
import com.putl.userservice.mapper.entity.UserInfoDO;
import com.putl.userservice.service.ReviewService;
import com.putl.userservice.service.UserInfoService;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * [Review]表服务接口  评论表
 *
 * @since 2025-01-18 12:03:24
 */

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, ReviewDO> implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final UserInfoService userDataService;

    @Override
    public List<ReviewListVO> getByArticleId(Integer articleId){
        // 根据文章id查询相关评论信息 再构建评论树
        List<ReviewDO> dos = reviewMapper.selectList(Wrappers.<ReviewDO>lambdaQuery().eq(ReviewDO::getTextId, articleId));
        if (CollectionUtils.isEmpty(dos)){
            return Collections.emptyList();
        }

        // 构建评论树
        Map<Integer, ReviewListVO> map = new HashMap<>();
        List<ReviewListVO> header = new ArrayList<>();

        // 将所有评论转换为 ReviewListVO
        List<ReviewListVO> allComments = dos.stream()
                .map(this::getReviewVO)
                .toList();

        // 分离顶级评论和非顶级评论
        for (ReviewListVO comment : allComments) {
            if (comment.getParentId() == 0) {
                header.add(comment);
            }
            map.put(comment.getId(), comment);
        }

        // 构建评论树
        for (ReviewListVO comment : allComments) {
            if (comment.getParentId() != 0 && map.containsKey(comment.getParentId())) {
                map.get(comment.getParentId()).getChildren().add(comment);
            }
        }
        return header;
    }



    @Override
    public boolean save(ReviewVO reviewVO){
        ReviewDO bean = BeanUtil.toBean(reviewVO, ReviewDO.class);
        bean.setUserId(ReqInfoContext.getReqInfo().getUserId());
        bean.setTextId(reviewVO.getArticleId());
        return reviewMapper.insert(bean) == 1;
    }

    private ReviewListVO getReviewVO(ReviewDO item){
        UserInfoDO user = userDataService.getByUserId(item.getUserId());
        ReviewListVO rspVO = BeanUtil.toBean(item, ReviewListVO.class);
        rspVO.setUsername(user.getUsername());
        rspVO.setHeaderImg(user.getPhoto());
        rspVO.setChildren(new ArrayList<>());
        return rspVO;
    }
}

