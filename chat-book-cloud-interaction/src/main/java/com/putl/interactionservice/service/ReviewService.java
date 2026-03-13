package com.putl.interactionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.interactionservice.entity.ReviewDO;
import com.putl.interactionservice.vo.ReviewListVO;
import com.putl.interactionservice.vo.ReviewVO;

import java.util.List;

public interface ReviewService extends IService<ReviewDO> {
    List<ReviewListVO> getByArticleId(Integer articleId);
    boolean save(ReviewVO reviewVO);
}
