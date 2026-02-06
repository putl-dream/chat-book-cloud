package com.putl.userservice.util;


import com.putl.userservice.controller.vo.MessageVO;
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

    public static String messageUser(MessageVO data) {
        return BeanUtil.toJsonString(new MessageResult<>("USER", data));
    }
}