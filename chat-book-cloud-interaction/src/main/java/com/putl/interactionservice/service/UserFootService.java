package com.putl.interactionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.interactionservice.entity.UserFootDO;
import com.putl.interactionservice.vo.NotificationVO;
import com.putl.interactionservice.vo.UserFootListVO;
import com.putl.interactionservice.vo.UserFootVO;

import java.util.List;

public interface UserFootService extends IService<UserFootDO> {
    boolean addBrowse(Integer articleId, Integer userId);
    int updateCollection(Integer articleId, Integer userId);
    int updateComment(Integer articleId, Integer userId);
    int updatePraise(Integer articleId, Integer userId);
    UserFootVO getUserFoot(Integer articleId, Integer userId);
    UserFootListVO getUserFootList(Integer articleId);
    List<ArticleListVO> getHistory(Integer userId, Integer page, Integer size);
    List<NotificationVO> getNotifications(int userId);
}
