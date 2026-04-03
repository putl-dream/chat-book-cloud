import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { normalizeNextPath } from "@/services/auth";
import { BrowserApiError } from "@/services/admin-api";
import AdminLayout from "@/layouts/AdminLayout.vue";
import DashboardView from "@/views/DashboardView.vue";
import UsersView from "@/views/UsersView.vue";
import UserAuditView from "@/views/UserAuditView.vue";
import ArticleReviewView from "@/views/ArticleReviewView.vue";
import ContentView from "@/views/ContentView.vue";
import TagsView from "@/views/TagsView.vue";
import InteractionsView from "@/views/InteractionsView.vue";
import SystemView from "@/views/SystemView.vue";
import ThemeView from "@/views/ThemeView.vue";
import LoginView from "@/views/LoginView.vue";
import ForbiddenView from "@/views/ForbiddenView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: "/dashboard",
    },
    {
      path: "/login",
      component: LoginView,
      meta: { public: true },
    },
    {
      path: "/forbidden",
      component: ForbiddenView,
      meta: { public: true },
    },
    {
      path: "/",
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: "dashboard", component: DashboardView },
        { path: "users", component: UsersView },
        { path: "users/audit", component: UserAuditView },
        { path: "articles/review", component: ArticleReviewView },
        { path: "articles/content", component: ContentView },
        { path: "tags", component: TagsView },
        { path: "interactions", component: InteractionsView },
        { path: "system", component: SystemView },
        { path: "theme", component: ThemeView },
      ],
    },
  ],
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  authStore.hydrateToken();

  if (to.meta.public) {
    if (to.path === "/login" && authStore.token) {
      try {
        await authStore.ensureSession();
        return normalizeNextPath(typeof to.query.next === "string" ? to.query.next : "/dashboard");
      } catch {
        return true;
      }
    }

    return true;
  }

  if (!authStore.token) {
    return {
      path: "/login",
      query: {
        next: normalizeNextPath(to.fullPath),
      },
    };
  }

  try {
    await authStore.ensureSession();
    return true;
  } catch (error) {
    if (error instanceof BrowserApiError && error.status === 403) {
      return { path: "/forbidden" };
    }

    return {
      path: "/login",
      query: {
        next: normalizeNextPath(to.fullPath),
        reason: "session-expired",
      },
    };
  }
});

export default router;
