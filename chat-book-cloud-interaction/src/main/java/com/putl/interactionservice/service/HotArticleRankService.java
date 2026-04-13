package com.putl.interactionservice.service;

public interface HotArticleRankService {

    boolean tryAcquireViewToken(Integer articleId, Integer userId);

    void recordView(Integer articleId);

    void recordPraise(Integer articleId, boolean active);

    void recordCollection(Integer articleId, boolean active);

    void recordComment(Integer articleId);
}
