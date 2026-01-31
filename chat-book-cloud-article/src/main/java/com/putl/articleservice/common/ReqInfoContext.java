package com.putl.articleservice.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.Principal;

public class ReqInfoContext {
    private static final ThreadLocal<ReqInfo> contexts = new ThreadLocal<>();

    public static void addReqInfo(ReqInfo reqInfo){
        contexts.set(reqInfo);
    }

    public static void clear(){
        contexts.remove();
    }

    public static ReqInfo getReqInfo(){
        return contexts.get();
    }

    @Data
    @AllArgsConstructor
    public static class ReqInfo implements Principal {
        /**
         * 客户端ip
         */
        private String clientIp;
        /**
         * 访问路径
         */
        private String path;
        /**
         * post 表单参数
         */
        private String payload;
        /**
         * 设备信息
         */
        private String device;
        /**
         * 用户id
         */
        private int userId;

        @Override
        public String getName(){
            return String.valueOf(userId);
        }
    }
}
