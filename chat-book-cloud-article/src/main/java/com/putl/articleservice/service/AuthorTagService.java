package com.putl.articleservice.service;

import com.putl.articleservice.controller.vo.AuthorTagVO;
import com.putl.articleservice.utils.PageResult;

import java.util.List;
import java.util.Map;

public interface AuthorTagService {

    PageResult<AuthorTagVO> getAdminPage(Integer pageNo, Integer pageSize, String keyword);

    List<AuthorTagVO> search(String keyword, Integer limit);

    List<AuthorTagVO> getHotTags(Integer limit);

    List<Integer> replaceArticleAuthorTags(Integer articleId, Integer creatorId, List<String> tagNames);

    List<String> getArticleAuthorTags(Integer articleId);

    Map<Integer, List<String>> getArticleAuthorTagMap(List<Integer> articleIds);

    List<Integer> getArticleIdsByAuthorTagName(String authorTagName);

    List<Integer> getArticleIdsByAuthorTagIds(List<Integer> authorTagIds);
}
