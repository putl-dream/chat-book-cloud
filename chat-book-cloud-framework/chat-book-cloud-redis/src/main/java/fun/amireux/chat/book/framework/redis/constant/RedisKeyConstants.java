package fun.amireux.chat.book.framework.redis.constant;

/**
 * Redis key naming constants following pattern: cbc:{env}:{module}:{biz}:{identifier}
 * <p>
 * Format: cbc:{env}:{module}:{biz}:{identifier}
 * - cbc: Chat Book Cloud project prefix
 * - {env}: environment (dev/test/prod)
 * - {module}: module name (article/user/auth/chat/social/interaction)
 * - {biz}: business domain (cache/session/captcha etc.)
 * - {identifier}: specific identifier
 */
public class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    // ==================== Auth Module ====================
    public static final String AUTH_CAPTCHA = "cbc:%s:auth:captcha:%s"; // email

    // ==================== Article Module ====================
    public static final String ARTICLE_CACHE = "cbc:%s:article:cache:%s"; // articleId
    public static final String ARTICLE_LIST_CACHE = "cbc:%s:article:list:%s:%s"; // listType:pageNo:pageSize

    // ==================== Tag Module ====================
    public static final String TAG_LIST_CACHE = "cbc:%s:article:tag:list:%s"; // type(all/1/2)

    // ==================== Review Module ====================
    public static final String REVIEW_LIST_CACHE = "cbc:%s:interaction:review:%s"; // articleId

    // ==================== Social Module ====================
    public static final String FOLLOW_STAT_CACHE = "cbc:%s:social:follow:stat:%s"; // userId

    // ==================== User Module ====================
    public static final String USER_CACHE = "cbc:%s:user:cache:%s"; // userId
    public static final String USER_BATCH_CACHE = "cbc:%s:user:batch:%s"; // md5(sortedIds)

    // ==================== Common ====================
    public static final String SESSION = "cbc:%s:session:%s"; // sessionId

    /**
     * Build a key with environment placeholder
     */
    public static String authCaptcha(String env, String email) {
        return String.format(AUTH_CAPTCHA, env, email);
    }

    public static String articleCache(String env, String articleId) {
        return String.format(ARTICLE_CACHE, env, articleId);
    }

    public static String userCache(String env, String userId) {
        return String.format(USER_CACHE, env, userId);
    }

    public static String userBatchCache(String env, String idsKey) {
        return String.format(USER_BATCH_CACHE, env, idsKey);
    }

    public static String articleListCache(String env, String listType, String pageNo) {
        return String.format(ARTICLE_LIST_CACHE, env, listType, pageNo);
    }

    public static String tagListCache(String env, String type) {
        return String.format(TAG_LIST_CACHE, env, type);
    }

    public static String reviewListCache(String env, String articleId) {
        return String.format(REVIEW_LIST_CACHE, env, articleId);
    }

    public static String followStatCache(String env, String userId) {
        return String.format(FOLLOW_STAT_CACHE, env, userId);
    }

    public static String session(String env, String sessionId) {
        return String.format(SESSION, env, sessionId);
    }
}
