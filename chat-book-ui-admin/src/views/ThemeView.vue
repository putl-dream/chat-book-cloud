<template>
  <section class="page-shell">
    <div class="page-hero compact">
      <p class="eyebrow">Theme Studio</p>
      <h1>主题设置</h1>
      <p class="hero-copy">保留原有主题变量体系，并把切换能力迁移到了 Vue + Pinia 的实现上。</p>
    </div>

    <div class="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
      <article
        v-for="theme in themeStore.themes"
        :key="theme.value"
        :class="[
          'panel cursor-pointer transition',
          themeStore.currentTheme === theme.value ? 'ring-cb-primary ring-2' : '',
        ]"
        @click="themeStore.setTheme(theme.value)"
      >
        <div class="stack-list">
          <div class="stack-title-row">
            <h3>{{ theme.label }}</h3>
            <span v-if="themeStore.currentTheme === theme.value" class="pill pill-safe">当前使用</span>
          </div>
          <p>{{ theme.subtitle }}</p>
          <div class="rounded-[20px] border p-4" style="border-color: var(--cb-border-subtle)">
            <div class="mb-3 h-3 w-1/2 rounded" style="background: var(--cb-primary)"></div>
            <div class="space-y-2">
              <div
                class="h-2 rounded"
                style="background: color-mix(in srgb, var(--cb-bg-main) 88%, transparent)"
              ></div>
              <div
                class="h-2 w-4/5 rounded"
                style="background: color-mix(in srgb, var(--cb-bg-main) 82%, transparent)"
              ></div>
              <div
                class="h-2 w-3/5 rounded"
                style="background: color-mix(in srgb, var(--cb-bg-main) 76%, transparent)"
              ></div>
            </div>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useThemeStore } from "@/stores/theme";

const themeStore = useThemeStore();
</script>
