import { defineStore } from "pinia";
import { clearAdminSession, readClientToken, saveAdminSession } from "@/services/auth";
import { BrowserApiError, getCurrentAdminUser, loginAdmin } from "@/services/admin-api";
import type { CurrentAdminUser } from "@/types/admin";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: "" as string,
    user: null as CurrentAdminUser | null,
    initialized: false,
    loading: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => state.user?.role === "admin",
  },
  actions: {
    hydrateToken() {
      this.token = readClientToken() ?? "";
    },
    async ensureSession(force = false) {
      if (!force && this.user && this.token) {
        return this.user;
      }

      this.hydrateToken();

      if (!this.token) {
        this.user = null;
        this.initialized = true;
        return null;
      }

      this.loading = true;

      try {
        const user = await getCurrentAdminUser({ redirectOnUnauthorized: false });

        if (user.role !== "admin") {
          throw new BrowserApiError("当前账号不是管理员，无法访问后台。", 403, 403);
        }

        this.user = user;
        this.initialized = true;
        return user;
      } catch (error) {
        clearAdminSession();
        this.token = "";
        this.user = null;
        this.initialized = true;
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async login(username: string, password: string) {
      const token = await loginAdmin(username, password);
      saveAdminSession(token);
      this.token = token;

      try {
        const user = await getCurrentAdminUser({ redirectOnUnauthorized: false });

        if (user.role !== "admin") {
          throw new BrowserApiError("当前账号不是管理员，无法访问后台。", 403, 403);
        }

        this.user = user;
        this.initialized = true;
        return user;
      } catch (error) {
        clearAdminSession();
        this.token = "";
        this.user = null;
        throw error;
      }
    },
    logout() {
      clearAdminSession();
      this.token = "";
      this.user = null;
      this.initialized = true;
    },
  },
});
