package com.putl.userservice.util;


import org.springframework.security.crypto.bcrypt.BCrypt;

public class PwdUtil {
    /**
     * 使用bcrypt算法加密密码。
     *
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public static String hashPassword(String plainPassword) {
        // BCrypt.gensalt() 会生成一个随机的 salt，默认工作因子为10（表示计算强度）

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 验证提供的明文密码是否与存储的哈希密码匹配。
     *
     * @param plainPassword  明文密码
     * @param hashedPassword 存储的哈希密码
     * @return 如果密码匹配返回true，否则返回false
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * 生成带有指定工作因子的salt。
     * 工作因子决定了哈希计算的复杂度，范围从4到31，默认值为10。
     *
     * @param logRounds 工作因子
     * @return 生成的salt
     */
    public static String generateSalt(int logRounds) {
        return BCrypt.gensalt(logRounds);
    }

    /**
     * 使用给定的salt对密码进行哈希。
     *
     * @param plainPassword 明文密码
     * @param salt          提供的salt
     * @return 加密后的密码
     */
    public static String hashPasswordWithSalt(String plainPassword, String salt) {
        return BCrypt.hashpw(plainPassword, salt);
    }

    // 测试方法
    public static void main(String[] args) {
        String password = "mySecretPassword123";

        // 加密密码
        String hashed = hashPassword(password);
        System.out.println("Hashed password: " + hashed);

        // 验证密码
        boolean matches = checkPassword(password, hashed);
        System.out.println("Password matches: " + matches);

        // 使用特定的工作因子生成salt并加密密码
        String customSalt = generateSalt(12); // 更高的工作因子意味着更慢但更安全的哈希
        String customHashed = hashPasswordWithSalt(password, customSalt);
        System.out.println("Custom hashed password: " + customHashed);

        // 再次验证密码
        boolean customMatches = checkPassword(password, customHashed);
        System.out.println("Custom password matches: " + customMatches);
    }
}
