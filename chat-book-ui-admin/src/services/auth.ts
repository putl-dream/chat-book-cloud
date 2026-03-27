export const ADMIN_SESSION_COOKIE = "chat_book_admin_token";
export const ADMIN_SESSION_STORAGE_KEY = "chat_book_admin_token";
export const ADMIN_THEME_STORAGE_KEY = "chat_book_admin_theme";
export const ADMIN_SESSION_MAX_AGE = 60 * 60 * 24 * 7;

export function normalizeNextPath(nextPath?: string | null) {
  if (!nextPath || !nextPath.startsWith("/") || nextPath.startsWith("//")) {
    return "/dashboard";
  }

  return nextPath;
}

export function readClientToken() {
  if (typeof document !== "undefined") {
    const cookie = document.cookie
      .split("; ")
      .find((item) => item.startsWith(`${ADMIN_SESSION_COOKIE}=`));

    if (cookie) {
      return decodeURIComponent(cookie.split("=").slice(1).join("="));
    }
  }

  if (typeof window !== "undefined") {
    return window.localStorage.getItem(ADMIN_SESSION_STORAGE_KEY);
  }

  return null;
}

export function saveAdminSession(token: string) {
  if (typeof window !== "undefined") {
    window.localStorage.setItem(ADMIN_SESSION_STORAGE_KEY, token);
  }

  if (typeof document !== "undefined") {
    const secure = window.location.protocol === "https:" ? "; Secure" : "";
    document.cookie =
      `${ADMIN_SESSION_COOKIE}=${encodeURIComponent(token)}; Path=/; Max-Age=${ADMIN_SESSION_MAX_AGE}; SameSite=Lax${secure}`;
  }
}

export function clearAdminSession() {
  if (typeof window !== "undefined") {
    window.localStorage.removeItem(ADMIN_SESSION_STORAGE_KEY);
  }

  if (typeof document !== "undefined") {
    document.cookie = `${ADMIN_SESSION_COOKIE}=; Path=/; Max-Age=0; SameSite=Lax`;
  }
}
