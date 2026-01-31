package com.putl.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.userservice.controller.vo.ReviewListVO;
import com.putl.userservice.controller.vo.ReviewVO;
import com.putl.userservice.mapper.entity.ReviewDO;

import java.util.List;

/**
 * [Review]表服务接口  评论表
 *
 * @since 2025-01-18 12:03:24
 */
public interface ReviewService extends IService<ReviewDO> {

    List<ReviewListVO> getByArticleId(Integer articleId);

    boolean save(ReviewVO reviewVO);
}

