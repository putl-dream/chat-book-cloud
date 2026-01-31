package com.putl.articleservice.utils;

import com.putl.articleservice.controller.vo.ArticleVO;
import lombok.Builder;
import lombok.Getter;

public class ArticleUtil {
    public static ArticleDTO getArticleInfo(ArticleVO article){
        // 获取文章封面
        String cover = HtmlUtil.getFirstImg(article.getContent());
        if (cover == null) {
            cover = ImageUtil.getImageByText(article.getTitle());
        }
        // 获取文章摘要
        String abstractText = HtmlUtil.getLen200ByHtml(article.getContent());
        return ArticleDTO.builder().
                title(article.getTitle()).
                cover(cover).
                abstractText(abstractText).
                content(article.getContent()).build();

    }

    @Getter
    @Builder
    public static class ArticleDTO {
        private String title;
        private String cover;
        private String abstractText;
        private String content;
    }
}
