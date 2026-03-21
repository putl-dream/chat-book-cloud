/**
 * 本地草稿存储工具
 * 基于 localStorage 实现，TTL 7 �?
 */

const DRAFT_PREFIX = 'draft';
const TTL_DAYS = 7;

/**
 * 生成草稿 key
 * @param {string|number} userId
 * @param {string|number|null} articleId - 新建文章时为 null
 * @returns {string}
 */
function getDraftKey(userId, articleId) {
    return `${DRAFT_PREFIX}:${userId}:${articleId ?? 'new'}`;
}

/**
 * 获取 TTL 毫秒�?
 * @returns {number}
 */
function getTTLMs() {
    return TTL_DAYS * 24 * 60 * 60 * 1000;
}

/**
 * 检查草稿是否有意义（非空）
 * @param {object} draft
 * @returns {boolean}
 */
function hasMeaningfulContent(draft) {
    if (!draft) return false;
    return Boolean(
        draft.articleId ||
        draft.title?.trim() ||
        draft.content?.replace(/<[^>]+>/g, '').trim()
    );
}

/**
 * 保存草稿�?localStorage
 * @param {string|number} userId
 * @param {string|number|null} articleId
 * @param {object} data - buildPayload() 格式的数�?
 * @returns {boolean} 是否保存成功
 */
export function saveDraft(userId, articleId, data) {
    if (!userId) return false;
    if (!data) return false;

    // 校验非空
    const draft = {
        articleId: data.articleId ?? null,
        title: data.title ?? '',
        content: data.content ?? '',
        category: data.category ?? null,
        contentType: data.contentType ?? 0,
        tagIds: data.tagIds ?? [],
        abstractText: data.abstractText ?? '',
        cover: data.cover ?? '',
        savedAt: new Date().toISOString()
    };

    // 校验内容
    if (!hasMeaningfulContent(draft)) return false;

    try {
        const key = getDraftKey(userId, articleId);
        const payload = JSON.stringify(draft);
        // 存入�?TTL 的结�?
        const stored = JSON.stringify({
            data: draft,
            expireAt: Date.now() + getTTLMs()
        });
        localStorage.setItem(key, stored);
        return true;
    } catch (e) {
        console.error('保存草稿失败:', e);
        return false;
    }
}

/**
 * �?localStorage 加载草稿
 * @param {string|number} userId
 * @param {string|number|null} articleId
 * @returns {object|null} 草稿数据�?null
 */
export function loadDraft(userId, articleId) {
    if (!userId) return null;

    try {
        const key = getDraftKey(userId, articleId);
        const stored = localStorage.getItem(key);
        if (!stored) return null;

        const { data, expireAt } = JSON.parse(stored);

        // 检�?TTL
        if (Date.now() > expireAt) {
            clearDraft(userId, articleId);
            return null;
        }

        // 校验数据结构完整�?
        if (!data || typeof data !== 'object') {
            clearDraft(userId, articleId);
            return null;
        }

        return data;
    } catch (e) {
        console.error('加载草稿失败:', e);
        return null;
    }
}

/**
 * 清除指定草稿
 * @param {string|number} userId
 * @param {string|number|null} articleId
 */
export function clearDraft(userId, articleId) {
    if (!userId) return;
    try {
        const key = getDraftKey(userId, articleId);
        localStorage.removeItem(key);
    } catch (e) {
        console.error('清除草稿失败:', e);
    }
}

/**
 * 检查是否存在草�?
 * @param {string|number} userId
 * @param {string|number|null} articleId
 * @returns {boolean}
 */
export function hasDraft(userId, articleId) {
    if (!userId) return false;
    const draft = loadDraft(userId, articleId);
    return draft !== null && hasMeaningfulContent(draft);
}

/**
 * 比较草稿是否比服务端更新
 * @param {object} draft - 本地草稿
 * @param {string|number} serverSavedAt - 服务端更新时�?(ISO string 或时间戳)
 * @returns {boolean} true if draft is newer
 */
export function isDraftNewer(draft, serverSavedAt) {
    if (!draft || !draft.savedAt) return false;
    if (!serverSavedAt) return true; // 无服务端时间，说明是新建

    const draftTime = new Date(draft.savedAt).getTime();
    const serverTime = new Date(serverSavedAt).getTime();

    return draftTime > serverTime;
}

/**
 * 获取草稿的已保存时间
 * @param {object} draft
 * @returns {string|null} ISO timestamp
 */
export function getDraftSavedAt(draft) {
    return draft?.savedAt ?? null;
}
