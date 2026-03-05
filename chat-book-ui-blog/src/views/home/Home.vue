<template>
  <div class="home">
    <div class="home-container">
      <div class="island-container">
        <div class="section-header">
          <h2 class="section-title">
            <span class="text-gradient">Bento 热点舞台</span>
          </h2>
          <div class="header-line"></div>
        </div>

        <div class="hot-bento-grid" v-if="recommendations && recommendations.length > 0">
          <!-- 主热点 (Large) -->
          <div class="hot-item main-feature interactive-card glass-effect hover-soft animate-pop"
            @click="openArticle(recommendations[0].id)">
            <ArticleCard :post="recommendations[0]" variant="feature" />
          </div>

          <!-- 次热点 -->
          <template v-if="recommendations.length > 1">
            <div v-for="(item, index) in recommendations.slice(1, 5)"
              class="hot-item interactive-card glass-effect hover-soft animate-pop" :key="'hot-' + (item.id || index)"
              :style="{ '--delay': `${index * 0.1 + 0.1}s` }" @click="openArticle(item.id)">
              <ArticleCard :post="item" variant="image" />
            </div>
          </template>
        </div>
      </div>

      <div class="section-header" style="margin-top: 40px;" v-if="posts && posts.length > 0">
        <h2 class="section-title" style="display: flex; align-items: center; gap: 8px;">
          <span class="animated-icon">🌊</span>
          <span class="text-gradient">最新流动流</span>
        </h2>
        <div class="header-line"></div>
      </div>

      <div class="bento-waterfall">
        <template v-for="(post, index) in posts" :key="'post-' + (post.id || index)">
          <div class="bento-item interactive-card glass-effect hover-soft animate-delayed"
            :style="{ '--delay': `${index * 0.05}s` }" @click="openArticle(post.id)">
            <ArticleCard :post="post" :variant="getPostVariant(post, index)" />
          </div>
        </template>
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
      </div>
      <div v-if="noMoreArticles" class="no-more">
        <span>— 到底啦 —</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import HotCard from "@/components/domain/HotCard.vue";
import ArticleCard from "@/components/domain/ArticleCard.vue";
import router from "@/router/index.js";
import { useHomeLogic } from "./_hooks/useHomeLogic.js";

const route = useRoute();

const currentPath = ref('/');
const { recommendations, posts, loading, noMoreArticles, fetchPosts, resetPosts } = useHomeLogic();

const getPostVariant = (post, index) => {
  if (index % 7 === 3 && post.cover) return 'large-image';
  if (index % 5 === 1 || !post.cover) return 'text-only';
  return 'default';
};

const handleScroll = (e) => {
  const target = e.target;
  const scrollTop = target.scrollTop;
  const clientHeight = target.clientHeight;
  const scrollHeight = target.scrollHeight;

  if (scrollTop + clientHeight >= scrollHeight - 50) {
    fetchPosts(currentPath.value);
  }
};

const openArticle = async (id) => {
  await router.push({ name: 'Article', params: { id: id } });
};

watch(
  () => route.path,
  async (newPath) => {
    currentPath.value = newPath;
    resetPosts();
    await fetchPosts(newPath);
  },
  { immediate: true }
);

onMounted(() => {
  const main = document.getElementById('common-layout-main');
  if (main) {
    main.addEventListener('scroll', handleScroll);
  }
});

onUnmounted(() => {
  const main = document.getElementById('common-layout-main');
  if (main) {
    main.removeEventListener('scroll', handleScroll);
  }
});
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding-top: 32px;
  background: transparent;
}

.home-container {
  max-width: var(--container-width-lg);
  margin: 0 auto;
  padding: 0 var(--container-padding);
}

.section-header {
  margin-bottom: 32px;
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
}

.section-title {
  font-size: 24px;
  font-weight: 800;
  margin: 0;
}

.text-gradient {
  background: linear-gradient(135deg, var(--text-color-primary) 0%, var(--color-primary) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-line {
  height: 2px;
  flex: 1;
  background: linear-gradient(90deg, var(--color-primary-light), transparent);
  border-radius: 2px;
  opacity: 0.5;
}

.island-container {
  padding: 32px;
  margin: -16px -32px 40px;
  background: radial-gradient(circle at top left, var(--color-primary-light), transparent 70%);
  border-radius: var(--border-radius-xl);
  border-bottom: 1px solid var(--border-color-base);
}

.animated-icon {
  font-size: 20px;
  animation: wave 2s infinite ease-in-out;
}

@keyframes wave {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-4px);
  }
}

.hot-bento-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  grid-template-rows: repeat(2, 180px);
  /* 扁平化锁死高度 */
  gap: 24px;
  margin-bottom: 0px;
}

.hot-item {
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.main-feature {
  grid-row: span 2;
  height: 100%;
  border-width: 1.5px;
  animation: border-pulse 4s infinite, popIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) backwards;
}

/* 利用深层穿透应用色彩呼应的蒙版渐变 */
.main-feature :deep(.variant-feature .card-overlay) {
  background: linear-gradient(to top, var(--color-primary), transparent 100%);
  opacity: 0.15;
  mix-blend-mode: overlay;
}

@keyframes border-pulse {
  0% {
    border-color: var(--border-color-glass);
    box-shadow: var(--box-shadow-glass);
  }

  50% {
    border-color: var(--color-primary-light);
    box-shadow: 0 0 16px var(--border-color-glass);
  }

  100% {
    border-color: var(--border-color-glass);
    box-shadow: var(--box-shadow-glass);
  }
}

.bento-waterfall {
  column-count: 3;
  column-gap: 24px;
  margin-bottom: 40px;
}

.bento-item {
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  height: max-content;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  break-inside: avoid;
  page-break-inside: avoid;
  margin-bottom: 24px;
  transform: translateZ(0);
}

.animate-pop {
  animation: popIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) backwards;
  animation-delay: var(--delay, 0s);
}

.animate-delayed {
  animation: fadeInUp 0.6s backwards;
  /* Additional 0.2s delay for the entire waterfall layer */
  animation-delay: calc(var(--delay, 0s) + 0.2s);
}

/* 占据两列的 Feature 卡片 - 多栏布局中建议全局占用或者使用默认流 */
.feature-span-2 {
  border-radius: var(--border-radius-xl);
}

/* 热榜小组件可能不占很多 row，自动适应即可 */
.widget-span {
  border-radius: var(--border-radius-xl);
}

/* 柔和阴影效果覆盖 override */
.hover-soft {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border: 1px solid var(--border-color-glass);
}

.hover-soft:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.05), 0 0 24px -6px rgba(var(--color-primary-rgb), 0.2);
  border-color: var(--color-primary-light);
}

.loading {
  text-align: center;
  padding: 40px 0;
  display: flex;
  justify-content: center;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.no-more {
  text-align: center;
  padding: 40px 0;
  color: var(--text-color-placeholder);
  font-size: 13px;
  letter-spacing: 1px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes popIn {
  0% {
    opacity: 0;
    transform: scale(0.95) translateY(20px);
  }

  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* Responsive Design */
@media (max-width: 1200px) {
  .hot-bento-grid {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto;
    grid-auto-rows: 200px;
  }
}

@media (max-width: 992px) {
  .hot-bento-grid {
    grid-template-columns: 1fr;
  }

  .bento-waterfall {
    column-count: 2;
  }
}

@media (max-width: 768px) {
  .bento-waterfall {
    column-count: 1;
  }
}

@media (max-width: 480px) {
  .bento-waterfall {
    column-count: 1;
  }
}
</style>