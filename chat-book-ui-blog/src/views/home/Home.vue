<template>
  <div class="home">
    <div class="home-container">
      <div class="section-header">
        <h2 class="section-title">
          <span class="text-gradient">发现与探索</span>
        </h2>
        <div class="header-line"></div>
      </div>

      <div class="bento-waterfall">
        <!-- Feature Card - 置顶大卡片跨两列 -->
        <div v-if="recommendations.length > 0"
          class="bento-item feature-span-2 interactive-card glass-effect hover-soft"
          @click="openArticle(recommendations[0].id)">
          <ArticleCard :post="recommendations[0]" variant="feature" />
        </div>

        <div v-for="(item, index) in recommendations.slice(1)"
          class="bento-item interactive-card glass-effect hover-soft" :key="'rec-' + (item.id || index)"
          :style="{ '--delay': `${index * 0.1}s` }" @click="openArticle(item.id)">
          <ArticleCard :post="item" variant="image" />
        </div>

        <!-- 将 HotCard 组件化并融入瀑布流打破对称性 -->
        <template v-for="(post, index) in posts" :key="'post-' + (post.id || index)">
          <div v-if="index === 2" class="bento-item widget-span interactive-card glass-effect hover-soft">
            <HotCard />
          </div>

          <div class="bento-item interactive-card glass-effect hover-soft" :style="{ '--delay': `${index * 0.05}s` }"
            @click="openArticle(post.id)">
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
import { ElSkeleton } from 'element-plus';
import HotCard from "@/components/domain/HotCard.vue";
import ArticleCard from "@/components/domain/ArticleCard.vue";
import router from "@/router/index.js";
import { getCategoryName } from "@/utils/category.js";
import { getCategoryPage, getNewPage, getSystemRecommendPage } from "@/api/article.js";
import { CATEGORY_ENUM } from "@/constants/index.js";

const route = useRoute();

const recommendations = ref([]);
const posts = ref([]);
const loading = ref(false);
const noMoreArticles = ref(false);
const page = ref(1);
const pageSize = ref(10);
const currentPath = ref('/');

// API 策略配置
const apiStrategy = {
  '/algorithm': {
    list: (p, s) => getCategoryPage(p, s, CATEGORY_ENUM.ALGORITHM),
    recommend: () => getCategoryPage(1, 5, CATEGORY_ENUM.ALGORITHM)
  },
  '/mysql': {
    list: (p, s) => getCategoryPage(p, s, CATEGORY_ENUM.MYSQL),
    recommend: () => getCategoryPage(1, 5, CATEGORY_ENUM.MYSQL)
  },
  '/backend': {
    list: (p, s) => getCategoryPage(p, s, CATEGORY_ENUM.BACKEND),
    recommend: () => getCategoryPage(1, 5, CATEGORY_ENUM.BACKEND)
  },
  '/frontend': {
    list: (p, s) => getCategoryPage(p, s, CATEGORY_ENUM.FRONTEND),
    recommend: () => getCategoryPage(1, 5, CATEGORY_ENUM.FRONTEND)
  },
  'default': {
    list: (p, s) => getNewPage(p, s),
    recommend: () => getSystemRecommendPage(1, 5)
  }
};

const getPostVariant = (post, index) => {
  if (index % 7 === 3 && post.cover) return 'large-image';
  if (index % 5 === 1 || !post.cover) return 'text-only';
  return 'default';
};

const fetchPosts = async () => {
  if (loading.value || noMoreArticles.value) return;
  loading.value = true;

  try {
    const strategy = apiStrategy[currentPath.value] || apiStrategy['default'];

    // 并行请求推荐文章和列表文章
    const [listRes, recommendRes] = await Promise.all([
      strategy.list(page.value, pageSize.value),
      strategy.recommend(page.value, pageSize.value)
    ]);

    // Axios 拦截器已经处理了 CommonResult，并返回了 .data (即 PageResult)
    if (recommendRes && recommendRes.list) {
      recommendations.value = recommendRes.list;
    }

    if (listRes && listRes.list) {
      const newPosts = listRes.list;
      if (newPosts.length === 0) {
        noMoreArticles.value = true;
      } else {
        posts.value = [...posts.value, ...newPosts];
        page.value++;
      }
    }
  } catch (error) {
    console.error('Failed to fetch posts:', error);
  } finally {
    loading.value = false;
  }
};

const handleScroll = (e) => {
  const target = e.target;
  const scrollTop = target.scrollTop;
  const clientHeight = target.clientHeight;
  const scrollHeight = target.scrollHeight;

  if (scrollTop + clientHeight >= scrollHeight - 50) {
    fetchPosts();
  }
};

const openArticle = async (id) => {
  await router.push({ name: 'Article', params: { id: id } });
};

watch(
  () => route.path,
  async (newPath) => {
    currentPath.value = newPath;
    posts.value = [];
    recommendations.value = [];
    page.value = 1;
    noMoreArticles.value = false;
    await fetchPosts();
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

.bento-waterfall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  grid-auto-flow: dense;
  gap: 24px;
  margin-bottom: 40px;
  align-items: start;
}

.bento-item {
  border-radius: var(--border-radius-xl);
  overflow: hidden;
  height: max-content;
  animation: fadeInUp 0.6s backwards;
  animation-delay: var(--delay, 0s);
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

/* 占据两列的 Feature 卡片 */
.feature-span-2 {
  grid-column: span 2;
  grid-row: span 2;
}

/* 热榜小组件可能不占很多 row，自动适应即可 */
.widget-span {
  grid-column: span 1;
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
    transform: translateY(24px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive Design */
@media (max-width: 992px) {
  .bento-waterfall {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .feature-span-2 {
    grid-column: span 1;
    grid-row: span 1;
  }
}

@media (max-width: 480px) {
  .bento-waterfall {
    grid-template-columns: 1fr;
  }
}
</style>