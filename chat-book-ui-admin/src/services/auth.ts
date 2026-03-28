export const ADMIN_TOKEN_KEY = "chat_book_admin_token";
export const ADMIN_REFRESH_TOKEN_KEY = "chat_book_admin_refresh_token";

export interface LoginVO {
  accessToken: string;
  refreshToken: string;
  expiresIn?: number;
}

type LoginResponse = Partial<LoginVO> | null | undefined;

export function normalizeLoginVO(payload: LoginResponse): LoginVO | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const accessToken = typeof payload.accessToken === "string" ? payload.accessToken.trim() : "";
  const refreshToken = typeof payload.refreshToken === "string" ? payload.refreshToken.trim() : "";
  if (!accessToken || !refreshToken) {
    return null;
  }

  return {
    accessToken,
    refreshToken,
    expiresIn: payload.expiresIn,
  };
}

export function normalizeNextPath(nextPath?: string | null) {
  if (!nextPath || !nextPath.startsWith("/") || nextPath.startsWith("//")) {
    return "/dashboard";
  }

  return nextPath;
}

function normalizeApiBase(baseUrl: string) {
  const normalized = baseUrl.replace(/\/$/, "");
  return normalized.endsWith("/api") ? normalized : `${normalized}/api`;
}

export function getBrowserApiBaseUrl() {
  const configuredBase =
    import.meta.env.VITE_API_BASE_URL?.trim() ||
    import.meta.env.NEXT_PUBLIC_API_BASE_URL?.trim() ||
    "";

  return configuredBase ? normalizeApiBase(configuredBase) : "/api";
}

function shouldRevokeSessionOnLogout() {
  return import.meta.env.VITE_AUTH_REVOKE_ON_LOGOUT?.trim() === "true";
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
 * 存储 Token（登录成功后调用）
 */
export function saveAdminSession(loginVO: LoginVO) {
  const normalized = normalizeLoginVO(loginVO);
  if (!normalized || typeof window === "undefined") {
    return;
  }

  window.localStorage.setItem(ADMIN_TOKEN_KEY, normalized.accessToken);
  window.localStorage.setItem(ADMIN_REFRESH_TOKEN_KEY, normalized.refreshToken);
}

/**
 * 清除所有 Token
 */
export function clearAdminSession() {
  if (typeof window !== "undefined") {
    window.localStorage.removeItem(ADMIN_TOKEN_KEY);
    window.localStorage.removeItem(ADMIN_REFRESH_TOKEN_KEY);
  }
}

/**
 * 调用后端撤销 Refresh Token，然后清除本地存储
 */
export async function revokeAndClearSession(): Promise<void> {
  const refreshToken = readRefreshToken();
  if (refreshToken && shouldRevokeSessionOnLogout()) {
    try {
      await fetch(`${getBrowserApiBaseUrl()}/auth/account/logout`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });
    } catch (e) {
      console.warn("Logout revoke failed:", e);
    }
  }
  clearAdminSession();
}
