package com.putl.articleservice.service.impl;

import com.putl.articleservice.service.ArticleTagService;
import com.putl.articleservice.service.AuthorTagService;
import com.putl.articleservice.service.SystemTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleTagServiceImpl implements ArticleTagService {

    private final AuthorTagService authorTagService;
    private final SystemTagService systemTagService;

    @Override
    @Transactional
    public void replaceArticleTags(Integer articleId, Integer creatorId, List<String> authorTags) {
        List<Integer> authorTagIds = authorTagService.replaceArticleAuthorTags(articleId, creatorId, authorTags);
        systemTagService.syncAutoTags(articleId, authorTagIds);
    }
}
