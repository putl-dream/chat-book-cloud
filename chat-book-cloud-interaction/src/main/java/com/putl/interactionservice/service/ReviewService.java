package com.putl.interactionservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.interactionservice.controller.vo.ReviewAdminStatsVO;
import com.putl.interactionservice.entity.ReviewDO;
import com.putl.interactionservice.controller.vo.ReviewListVO;
import com.putl.interactionservice.controller.vo.ReviewVO;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewService extends IService<ReviewDO> {
    List<ReviewListVO> getByArticleId(Integer articleId);
    boolean save(ReviewVO reviewVO);

    /** 管理员评论分页查询 */
    IPage<ReviewVO> getAdminPage(Integer page, Integer size, Integer articleId,
            Integer userId, String keyword, Integer status,
            LocalDateTime startTime, LocalDateTime endTime);

    /** 评论治理统计 */
    ReviewAdminStatsVO getAdminStats();

    /** 删除评论（软删除） */
    void deleteReview(Integer reviewId);

    /** 屏蔽评论 */
    void hideReview(Integer reviewId);

    /** 恢复评论 */
    void restoreReview(Integer reviewId);
}
