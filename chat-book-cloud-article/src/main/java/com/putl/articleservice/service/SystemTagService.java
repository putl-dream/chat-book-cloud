package com.putl.articleservice.service;

import com.putl.articleservice.controller.dto.SystemTagSaveRequestDTO;
import com.putl.articleservice.controller.vo.SystemTagVO;
import com.putl.articleservice.utils.PageResult;

import java.util.List;
import java.util.Map;

public interface SystemTagService {

    PageResult<SystemTagVO> getPage(Integer pageNo, Integer pageSize, String keyword, String dimension, String status);

    List<SystemTagVO> getAllActiveTags();

    SystemTagVO create(SystemTagSaveRequestDTO request);

    void update(SystemTagSaveRequestDTO request);

    void delete(Integer systemTagId);

    List<String> getArticleSystemTags(Integer articleId);

    Map<Integer, List<String>> getArticleSystemTagMap(List<Integer> articleIds);

    List<Integer> getArticleIdsBySystemTagNames(List<String> systemTagNames);

    void syncAutoTags(Integer articleId, List<Integer> authorTagIds);

    void updateArticleSystemTagsByAdmin(Integer articleId, List<Integer> systemTagIds);
}
