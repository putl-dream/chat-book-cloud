package com.putl.articleservice.utils;

import com.alibaba.fastjson2.JSON;
import com.putl.articleservice.controller.vo.ArticleVO;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class MessageResult<T> {
    private String type;
    private T data;

    public static String messageSystem(String data) {
        return JSON.toJSONString(new MessageResult<>("SYSTEM", data));
    }

    public static String messageCache(String data) {
        return JSON.toJSONString(new MessageResult<>("CACHE", data));
    }

    public static String messageSave(String data) {
        return JSON.toJSONString(new MessageResult<>("SAVE", data));
    }

    public static String messageSelect(String data) {
        ArticleVO article = JSON.parseObject(data, ArticleVO.class);
        return JSON.toJSONString(new MessageResult<>("SELECT", article));
    }

    public static String messagePublish(String data) {
        return JSON.toJSONString(new MessageResult<>("PUBLISH", data));
    }
}