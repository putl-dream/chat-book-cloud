<template>
  <el-container class="app-layout page" :class="{'bg-creative': meta.sidebar === 'creative'}">
    <!-- Creative specific bg decorations -->
    <template v-if="meta.sidebar === 'creative'">
      <div class="bg-decoration-1"></div>
      <div class="bg-decoration-2"></div>
    </template>

    <el-header class="header" v-if="meta.headerType !== 'none'" :height="headerHeight">
      <CommonHeader v-if="meta.headerType === 'common'" :show-search="meta.showSearch !== false" />
      <CreativeHeader v-else-if="meta.headerType === 'creative'" />
    </el-header>

    <!-- With Sidebar -->
    <el-container class="main-container" v-if="meta.sidebar === 'creative'">
      <el-aside class="aside" width="240px">
        <CreativeAside />
      </el-aside>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
        <SiteFooter v-if="meta.showFooter !== false" class="layout-footer creative-footer" />
      </el-main>
    </el-container>

    <!-- Without Sidebar -->
    <el-main class="main-content-wrapper" v-else :class="{'no-padding': meta.headerType === 'none'}">
      <div class="main-view">
        <router-view v-slot="{ Component }">
          <component :is="Component" class="route-view" />
        </router-view>
      </div>
      <SiteFooter v-if="meta.showFooter !== false" class="layout-footer" />
    </el-main>

  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import CommonHeader from "@/components/common/CommonHeader.vue";
import CreativeHeader from "@/views/creator/components/CreativeHeader.vue";
import CreativeAside from "@/views/creator/components/CreativeAside.vue";
import SiteFooter from "@/components/common/SiteFooter.vue";

const route = useRoute();
const meta = computed(() => {
  // Default values for standard pages
  const defaults = {
    headerType: 'common',
    showSearch: true,
    showFooter: true,
    sidebar: false
  };
  return Object.assign({}, defaults, route.meta || {});
});
const headerHeight = computed(() => 'auto'); // Let it size naturally
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--app-shell-radial), var(--app-shell-bg);
  display: flex;
  flex-direction: column;
}

.page.bg-creative {
  background-color: var(--bg-color-base, #f8fafc);
  background-image: none; /* override shell background for creative */
  position: relative;
  overflow-x: hidden;
}

.header {
  padding: 0;
  width: 100%;
  z-index: var(--z-index-header, 100);
  flex-shrink: 0;
  position: sticky;
  top: 0;
}

/* Creative Background Decorations */
.bg-decoration-1 {
  position: absolute;
  top: -10%;
  left: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.15) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  filter: blur(40px);
  z-index: 0;
  pointer-events: none;
}

.bg-decoration-2 {
  position: absolute;
  bottom: -10%;
  right: -5%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  filter: blur(40px);
  z-index: 0;
  pointer-events: none;
}

/* With Sidebar Container */
.main-container {
  position: relative;
  z-index: 10;
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
  flex: 1;
}

.aside {
  padding-top: 20px;
  overflow: visible;
}

.main-content {
  padding: 20px 40px;
  display: flex;
  flex-direction: column;
}

.creative-footer {
  margin-top: auto;
  padding-top: 20px;
}

/* No Sidebar Wrapper */
.main-content-wrapper {
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  overflow: visible !important;
}

.main-content-wrapper.no-padding {
  background-color: var(--bg-color-base);
  background-image: none;
}

.main-view {
  flex: 1 0 auto;
  display: flex;
  flex-direction: column;
}

.route-view {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.layout-footer {
  flex-shrink: 0;
  margin-top: auto;
}

/* Router View Transition */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .aside {
    display: none !important;
  }
  .main-content {
    padding: 16px;
  }
}
</style>
