import { defineStore } from "pinia";
import { themeOptions } from "@/data/admin-config";
import { ADMIN_THEME_STORAGE_KEY } from "@/services/auth";

type ThemeName = (typeof themeOptions)[number]["value"];

const DEFAULT_THEME: ThemeName = "linear";

function applyTheme(theme: ThemeName) {
  if (typeof document === "undefined") {
    return;
  }

  document.documentElement.setAttribute("data-theme", theme);
}

export const useThemeStore = defineStore("theme", {
  state: () => ({
    currentTheme: DEFAULT_THEME as ThemeName,
  }),
  getters: {
    themes: () => themeOptions,
  },
  actions: {
    initialize() {
      if (typeof window === "undefined") {
        return;
      }

      const savedTheme = window.localStorage.getItem(ADMIN_THEME_STORAGE_KEY) as ThemeName | null;
      const nextTheme =
        savedTheme && themeOptions.some((option) => option.value === savedTheme)
          ? savedTheme
          : DEFAULT_THEME;

      this.currentTheme = nextTheme;
      applyTheme(nextTheme);
    },
    setTheme(theme: ThemeName) {
      this.currentTheme = theme;

      if (typeof window !== "undefined") {
        window.localStorage.setItem(ADMIN_THEME_STORAGE_KEY, theme);
      }

      applyTheme(theme);
    },
  },
});
