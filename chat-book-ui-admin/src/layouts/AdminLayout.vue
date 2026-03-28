<template>
  <div class="bg-cb-bg-main min-h-screen w-full">
    <aside class="admin-sidebar px-4 py-6">
      <div class="flex items-center px-2">
        <h1 class="text-cb-primary text-xl font-semibold tracking-tight">Admin Control Plane</h1>
      </div>

      <nav class="mt-6 flex flex-1 flex-col gap-6 overflow-y-auto">
        <div v-for="group in adminNavigation" :key="group.title" class="flex flex-col gap-2">
          <h2 class="text-cb-text-secondary px-2 text-xs font-semibold tracking-[0.24em] uppercase">
            {{ group.title }}
          </h2>
          <div class="flex flex-col gap-1">
            <RouterLink
              v-for="item in group.items"
              :key="item.href"
              :to="item.href"
              :class="[
                'menu-item flex items-center justify-between rounded-2xl px-3 py-3 text-sm font-medium transition',
                { active: route.path === item.href },
              ]"
            >
              <span>{{ item.label }}</span>
              <span v-if="route.path === item.href" class="h-2 w-2 rounded-full bg-cb-primary" />
            </RouterLink>
          </div>
        </div>
      </nav>
    </aside>

    <div class="admin-main-layout flex min-h-screen flex-col">
      <header
        class="border-cb-border bg-cb-bg-card sticky top-0 z-30 flex min-h-16 flex-wrap items-center justify-between gap-4 border-b px-6 py-4"
        style="backdrop-filter: var(--cb-card-backdrop); -webkit-backdrop-filter: var(--cb-card-backdrop)"
      >
        <div class="min-w-0">
          <p class="header-kicker">Admin Control Plane</p>
          <div class="breadcrumb-row">
            <template v-for="(item, index) in breadcrumbs" :key="`${item.href}-${index}`">
              <span v-if="index > 0">/</span>
              <span :class="{ 'is-current': index === breadcrumbs.length - 1 }">{{ item.label }}</span>
            </template>
          </div>
          <p v-if="currentNav" class="header-subtitle">{{ currentNav.description }}</p>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <div class="header-segment">
            <button
              v-for="option in themeStore.themes"
              :key="option.value"
              :class="['header-segment-button', { 'is-active': themeStore.currentTheme === option.value }]"
              type="button"
              @click="themeStore.setTheme(option.value)"
            >
              {{ option.label }}
            </button>
          </div>

          <div class="header-profile">
            <div class="header-avatar">
              <span class="text-sm font-semibold">A</span>
            </div>
            <div class="min-w-0">
              <strong>{{ authStore.user?.username ?? "Admin" }}</strong>
              <span>
                {{ authStore.user?.role === "admin" ? "管理员" : "受限账号" }}
                <template v-if="authStore.user"> · UID {{ authStore.user.userId }}</template>
              </span>
            </div>
          </div>

          <button class="header-action-button" type="button" @click="handleLogout">退出登录</button>
        </div>
      </header>

      <main class="flex-1 p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { adminNavigation, buildBreadcrumbs, findCurrentNav } from "@/data/admin-config";
import { useAuthStore } from "@/stores/auth";
import { useThemeStore } from "@/stores/theme";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const themeStore = useThemeStore();

const currentNav = computed(() => findCurrentNav(route.path));
const breadcrumbs = computed(() => buildBreadcrumbs(route.path));

async function handleLogout() {
  await authStore.logout();
  router.replace("/login");
}
</script>
