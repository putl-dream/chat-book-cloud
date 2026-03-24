/**
 * 校验文件是否可以作为封面上传
 * @param {File} file 
 * @returns {boolean}
 */
export function isValidCoverFile(file) {
    if (file.type !== 'image/jpeg' && file.type !== 'image/png') {
        return false;
    }
    if (file.size / 1024 / 1024 > 10) {
        return false;
    }
    return true;
}

/**
 * 校验是否有实际内�? * @param {number|string} articleId 
 * @param {string} title 
 * @param {string} html 
 * @returns {boolean}
 */
export function hasMeaningfulContent(articleId, title, html) {
    return Boolean(articleId || title?.trim() || html?.replace(/<[^>]+>/g, '').trim());
}

/**
 * 构造保�?发布的请�?Payload
 * @param {object} rawData 
 * @returns {object}
 */
export function buildArticlePayload(rawData) {
    return {
        id: rawData.articleId,
        title: rawData.title,
        content: rawData.html,
        category: rawData.publishForm.category,
        contentType: rawData.publishForm.contentType,
        tagIds: rawData.publishForm.tagIds,
        abstractText: rawData.publishForm.abstractText,
        cover: rawData.publishForm.cover,
        updatedAt: rawData.lastSavedAt
    };
}
