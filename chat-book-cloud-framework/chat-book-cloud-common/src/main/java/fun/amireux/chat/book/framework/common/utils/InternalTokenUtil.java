package fun.amireux.chat.book.framework.common.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 内部通信令牌工具类
 * 用于生成和校验 Gateway 到微服务之间的可信签名
 */
public class InternalTokenUtil {

    private static final String HMAC_ALGO = "HmacSHA256";

    /**
     * 生成内部通信签名
     * @param data 要签名的数据 (如 userId)
     * @param secret 密钥
     * @return 签名字符串
     */
    public static String generateSignature(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC signature generation failed", e);
        }
    }

    /**
     * 校验内部通信签名
     * @param data 原始数据
     * @param signature 接收到的签名
     * @param secret 密钥
     * @return 是否通过
     */
    public static boolean verifySignature(String data, String signature, String secret) {
        if (data == null || signature == null || secret == null) {
            return false;
        }
        String generated = generateSignature(data, secret);
        return generated.equals(signature);
    }
}
