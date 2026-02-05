package fun.amireux.chat.book.auth.utils;

import java.security.SecureRandom;

public class RandomNumUtil {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成指定长度的随机字符串（包含数字、大小写字母）
     * 使用 SecureRandom 增强安全性
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String getCaptchaByLen(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
