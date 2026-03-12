package com.putl.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.userservice.controller.vo.NotificationVO;
import com.putl.userservice.controller.vo.UserFootListVO;
import com.putl.userservice.controller.vo.UserFootVO;
import com.putl.userservice.mapper.entity.UserFootDO;

import java.util.List;

/**
 * [UserFoot]表服务接口  用户足迹表
 *
 * @since 2025-01-17 16:00:13
 */
public interface UserFootService extends IService<UserFootDO> {

    boolean addBrowse(Integer articleId, Integer userId);

    int updateCollection(Integer articleId, Integer userId);

    int updateComment(Integer articleId, Integer userId);

    int updatePraise(Integer articleId, Integer userId);

    UserFootVO getUserFoot(Integer articleId, Integer userId);

    UserFootListVO getUserFootList(Integer articleId);

    List<ArticleListVO> getHistory(Integer userId, Integer page, Integer size);

    /**
     * 获取用户收到的互动通知（点赞/收藏/评论/浏览）
     * <p>P0 Fix：原 getMessage 返回 List&lt;ArticleListVO&gt; 丢失了操作人/操作类型/时间等关键信息</p>
     *
     * @param userId 当前用户ID（文章作者）
     * @return 通知列表，按时间倒序
     */
    List<NotificationVO> getNotifications(int userId);

    /**
     * @deprecated 使用 {@link #getNotifications(int)} 替代，此方法返回数据错误（仅文章列表，无通知语义）。
     */
    @Deprecated
    List<ArticleListVO> getMessage(int userId);
}


