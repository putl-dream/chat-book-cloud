package com.putl.articleservice.utils;

import com.putl.articleservice.controller.vo.ArticleVO;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class MessageResult<T> {
    private String type;
    private T data;

    public static String messageSystem(String data) {
        return BeanUtil.toJsonString(new MessageResult<>("SYSTEM", data));
    }

    public static String messageCache(String data) {
        return BeanUtil.toJsonString(new MessageResult<>("CACHE", data));
    }

    public static String messageSave(String data) {
        return BeanUtil.toJsonString(new MessageResult<>("SAVE", data));
    }

    public static String messageSelect(String data) {
        ArticleVO article = BeanUtil.toBean(data, ArticleVO.class);
        return BeanUtil.toJsonString(new MessageResult<>("SELECT", article));
    }

    public static String messagePublish(String data) {
        return BeanUtil.toJsonString(new MessageResult<>("PUBLISH", data));
    }

    public static String messageError(String data) {
        return BeanUtil.toJsonString(new MessageResult<>("ERROR", data));
    }
}