import { API_CONFIG } from '@/config/index.js';

const ACCESS_TOKEN_KEY = 'token';
const REFRESH_TOKEN_KEY = 'refreshToken';

let isRefreshing = false;
let refreshingPromise = null;

function getApiBaseURL() {
    const baseURL = API_CONFIG.baseURL || '';
    return baseURL.endsWith('/') ? baseURL.slice(0, -1) : baseURL;
}

function normalizeLoginTokens(loginVO) {
    if (!loginVO || typeof loginVO !== 'object') {
        return null;
    }

    const accessToken = typeof loginVO.accessToken === 'string' ? loginVO.accessToken.trim() : '';
    const refreshToken = typeof loginVO.refreshToken === 'string' ? loginVO.refreshToken.trim() : '';

    if (!accessToken || !refreshToken) {
        return null;
    }

    return {
        accessToken,
        refreshToken
    };
}

/**
 * 获取 Access Token
 */
export function getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
}

/**
 * 获取 Refresh Token
 */
export function getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
}

/**
 * 存储双 Token（登录成功后调用）
 * @param {Object} loginVO - { accessToken, refreshToken, expiresIn }
 */
export function setTokens(loginVO) {
    const normalized = normalizeLoginTokens(loginVO);
    if (!normalized) {
        throw new Error('登录接口未返回 refreshToken，无法启用 JWT 刷新机制');
    }

    localStorage.setItem(ACCESS_TOKEN_KEY, normalized.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, normalized.refreshToken);
}

/**
 * 清除所有 Token
 */
export function clearTokens() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem('avatar');
}

/**
 * 刷新 Access Token 并返回新的 Access Token
 * 保证并发请求只触发一次刷新
 */
export async function refreshAccessToken() {
    if (isRefreshing && refreshingPromise) {
        return refreshingPromise;
    }

    const refreshToken = getRefreshToken();
    if (!refreshToken) {
        return null;
    }

    isRefreshing = true;

    refreshingPromise = (async () => {
        try {
            const baseURL = getApiBaseURL();
            const response = await fetch(`${baseURL}/api/auth/account/refresh`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken }),
            });

            const res = await response.json();

            if (res.code === 200 && res.data) {
                const normalized = normalizeLoginTokens(res.data);
                if (!normalized) {
                    clearTokens();
                    return null;
                }

                setTokens(normalized);
                return normalized.accessToken;
            }

            clearTokens();
            return null;
        } catch (e) {
            console.error('Token refresh failed:', e);
            clearTokens();
            return null;
        } finally {
            isRefreshing = false;
            refreshingPromise = null;
        }
    })();

    return refreshingPromise;
}

/**
 * 调用后端撤销 Refresh Token，然后清除本地存储
 */
export async function logoutAndRevoke() {
    const refreshToken = getRefreshToken();
    if (refreshToken) {
        try {
            const baseURL = getApiBaseURL();
            await fetch(`${baseURL}/api/auth/account/logout`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken }),
            });
        } catch (e) {
            console.warn('Logout revoke failed:', e);
        }
    }
    clearTokens();
}
