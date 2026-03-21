export const SAVE_STATE_ENUM = {
    SAVED: 'saved',
    SAVING: 'saving',
    LOCAL_CACHED: 'local-cached',
    CACHED: 'cached',
    ERROR: 'error',
    DRAFT: 'draft',
    PUBLISHED: 'published'
};

export const SAVE_STATE_TEXT_MAP = {
    [SAVE_STATE_ENUM.LOCAL_CACHED]: '已本地保存',
    [SAVE_STATE_ENUM.SAVING]: '保存中...',
    [SAVE_STATE_ENUM.CACHED]: '已缓存',
    [SAVE_STATE_ENUM.SAVED]: '已保存',
    [SAVE_STATE_ENUM.ERROR]: '保存失败',
};

export const EDITOR_CONFIG = {
    MAX_COVER_SIZE_MB: 2,
    CACHE_DELAY_MS: 400,
    AUTOSAVE_DELAY_MS: 2000,
    ACK_TIMEOUT_MS: 4000,
    LAYOUT: {
        MIN_PANEL_WIDTH: 15,
        MAX_TOTAL_SIDE_WIDTH: 70,
        MOBILE_BREAKPOINT: 768
    }
};
