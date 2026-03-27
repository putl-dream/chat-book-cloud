import { DEFAULT_THEME, THEME_STORAGE_KEY, isThemeName, type ThemeName } from "@/theme/config";

export function applyTheme(theme: ThemeName) {
  if (typeof document === "undefined") {
    return;
  }

  document.documentElement.setAttribute("data-theme", theme);
}

export function resolveStoredTheme() {
  if (typeof window === "undefined") {
    return DEFAULT_THEME;
  }

  const savedTheme = window.localStorage.getItem(THEME_STORAGE_KEY);
  return isThemeName(savedTheme) ? savedTheme : DEFAULT_THEME;
}

export function persistTheme(theme: ThemeName) {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.setItem(THEME_STORAGE_KEY, theme);
}
