package com.putl.articleservice.utils;

import cn.hutool.json.JSONUtil;
import com.putl.articleservice.controller.vo.ArticleVO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageResult<T> {
    private String type;
    private T data;

    public static String messageSystem(String data){
        return JSONUtil.toJsonStr(new MessageResult<>("SYSTEM", data));
    }

    public static String messageCache(String data){
        return JSONUtil.toJsonStr(new MessageResult<>("CACHE", data));
    }

    public static String messageSave(String data){
        return JSONUtil.toJsonStr(new MessageResult<>("SAVE", data));
    }

    public static String messageSelect(String data){
        ArticleVO article = JSONUtil.toBean(data, ArticleVO.class);
        return JSONUtil.toJsonStr(new MessageResult<>("SELECT", article));
    }

    public static String messagePublish(String data){
        return JSONUtil.toJsonStr(new MessageResult<>("PUBLISH", data));
    }
}