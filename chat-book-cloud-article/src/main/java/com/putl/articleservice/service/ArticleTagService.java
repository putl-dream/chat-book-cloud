package com.putl.articleservice.service;

import java.util.List;

public interface ArticleTagService {

    void replaceArticleTags(Integer articleId, Integer creatorId, List<String> authorTags);
}
