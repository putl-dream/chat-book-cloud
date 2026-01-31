package com.putl.userservice.util;

public class RandomNumUtil {
    // 根据length生成长度为length随机字符串
    public static String getCaptchaByLen(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomChar = (int) (Math.random() * 62);
            if (randomChar < 10) {
                sb.append((char) ('0' + randomChar));
            } else if (randomChar < 36) {
                sb.append((char) ('a' + randomChar - 10));
            } else {
                sb.append((char) ('A' + randomChar - 36));
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(getCaptchaByLen(6));
    }
}
