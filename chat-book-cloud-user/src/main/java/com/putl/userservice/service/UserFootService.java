package com.putl.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.userservice.client.result.ArticleListVO;
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

    List<ArticleListVO> getMessage(int userId);
}

