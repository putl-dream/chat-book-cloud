import { defineStore } from "pinia";
import { DEFAULT_THEME, themeOptions, type ThemeName } from "@/theme/config";
import { applyTheme, persistTheme, resolveStoredTheme } from "@/theme/dom";

export const useThemeStore = defineStore("theme", {
  state: () => ({
    currentTheme: DEFAULT_THEME as ThemeName,
  }),
  getters: {
    themes: () => themeOptions,
  },
  actions: {
    initialize() {
      const nextTheme = resolveStoredTheme();
      this.currentTheme = nextTheme;
      applyTheme(nextTheme);
    },
    setTheme(theme: ThemeName) {
      this.currentTheme = theme;
      persistTheme(theme);
      applyTheme(theme);
    },
  },
});
