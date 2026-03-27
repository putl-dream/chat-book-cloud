export const ADMIN_SESSION_COOKIE = "chat_book_admin_token";
export const ADMIN_TOKEN_KEY = "chat_book_admin_token";
export const ADMIN_REFRESH_TOKEN_KEY = "chat_book_admin_refresh_token";
export const ADMIN_SESSION_MAX_AGE = 60 * 60 * 24 * 7;

export interface LoginVO {
    accessToken: string;
    refreshToken: string;
    expiresIn: number;
}

export function normalizeNextPath(nextPath?: string | null) {
  if (!nextPath || !nextPath.startsWith("/") || nextPath.startsWith("//")) {
    return "/dashboard";
  }

  return nextPath;
}

/**
 * 读取 Access Token
 */
export function readAccessToken(): string | null {
  if (typeof window !== "undefined") {
    return window.localStorage.getItem(ADMIN_TOKEN_KEY);
  }
  return null;
}

/**
 * 读取 Refresh Token
 */
export function readRefreshToken(): string | null {
  if (typeof window !== "undefined") {
    return window.localStorage.getItem(ADMIN_REFRESH_TOKEN_KEY);
  }
  return null;
}

/**
 * 存储双 Token（登录成功后调用）
 */
export function saveAdminSession(loginVO: LoginVO) {
  if (typeof window !== "undefined") {
    window.localStorage.setItem(ADMIN_TOKEN_KEY, loginVO.accessToken);
    window.localStorage.setItem(ADMIN_REFRESH_TOKEN_KEY, loginVO.refreshToken);
  }

  if (typeof document !== "undefined") {
    const secure = window.location.protocol === "https:" ? "; Secure" : "";
    document.cookie =
      `${ADMIN_SESSION_COOKIE}=${encodeURIComponent(loginVO.accessToken)}; Path=/; Max-Age=${loginVO.expiresIn}; SameSite=Lax${secure}`;
  }
}

/**
 * 清除所有 Token
 */
export function clearAdminSession() {
  if (typeof window !== "undefined") {
    window.localStorage.removeItem(ADMIN_TOKEN_KEY);
    window.localStorage.removeItem(ADMIN_REFRESH_TOKEN_KEY);
  }

  if (typeof document !== "undefined") {
    document.cookie = `${ADMIN_SESSION_COOKIE}=; Path=/; Max-Age=0; SameSite=Lax`;
  }
}

/**
 * 调用后端撤销 Refresh Token，然后清除本地存储
 */
export async function revokeAndClearSession(): Promise<void> {
  const refreshToken = readRefreshToken();
  if (refreshToken) {
    try {
      const baseURL = import.meta.env.NEXT_PUBLIC_API_BASE_URL || '';
      await fetch(`${baseURL}/api/auth/account/logout`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });
    } catch (e) {
      console.warn('Logout revoke failed:', e);
    }
  }
  clearAdminSession();
}
