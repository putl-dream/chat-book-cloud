package com.putl.userservice.util;


import com.alibaba.fastjson2.JSON;
import com.putl.userservice.controller.vo.MessageVO;
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

    public static String messageUser(MessageVO data) {
        return JSON.toJSONString(new MessageResult<>("USER", data));
    }
}