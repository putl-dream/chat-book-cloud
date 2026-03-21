import { computed, readonly, ref } from 'vue';

export const DEFAULT_SITE_THEME = 'light';
export const SITE_THEME_STORAGE_KEY = 'chat-book-site-theme';

export const SITE_THEME_OPTIONS = Object.freeze([
    {
        id: 'light',
        name: '冷色现代',
        description: '冷调导航、现代文章层次和深色代码面板，适合作为系统默认主题。',
        preview: 'linear-gradient(135deg, #dbeafe 0%, #e0e7ff 52%, #ffffff 100%)',
        accent: '#4f46e5'
    },
    {
        id: 'reading',
        name: '暖纸阅读',
        description: '将导航、页面背景和文章正文统一切换到纸感暖色阅读风格。',
        preview: 'linear-gradient(135deg, #f2dccb 0%, #fbf0e4 55%, #fffdf9 100%)',
        accent: '#c86143'
    }
]);

const ARTICLE_THEME_BY_SITE_THEME = Object.freeze({
    light: 'light',
    reading: 'reading'
});

const activeTheme = ref(DEFAULT_SITE_THEME);

const normalizeSiteTheme = (themeId) => {
    if (SITE_THEME_OPTIONS.some((option) => option.id === themeId)) {
        return themeId;
    }

    return DEFAULT_SITE_THEME;
};

const syncDomTheme = (themeId) => {
    if (typeof document === 'undefined') {
        return;
    }

    document.documentElement.setAttribute('data-theme', themeId);
};

export const setSiteTheme = (themeId, { persist = true } = {}) => {
    const resolvedTheme = normalizeSiteTheme(themeId);
    activeTheme.value = resolvedTheme;
    syncDomTheme(resolvedTheme);

    if (persist && typeof window !== 'undefined') {
        window.localStorage.setItem(SITE_THEME_STORAGE_KEY, resolvedTheme);
    }

    return resolvedTheme;
};

export const initSiteTheme = () => {
    if (typeof window === 'undefined') {
        syncDomTheme(DEFAULT_SITE_THEME);
        return DEFAULT_SITE_THEME;
    }

    const storedTheme = window.localStorage.getItem(SITE_THEME_STORAGE_KEY);
    const resolvedTheme = normalizeSiteTheme(storedTheme || DEFAULT_SITE_THEME);
    activeTheme.value = resolvedTheme;
    syncDomTheme(resolvedTheme);

    return resolvedTheme;
};

export function useSiteTheme() {
    return {
        siteTheme: readonly(activeTheme),
        articleTheme: computed(() => ARTICLE_THEME_BY_SITE_THEME[activeTheme.value] || ''),
        themeOptions: SITE_THEME_OPTIONS,
        setSiteTheme
    };
}
