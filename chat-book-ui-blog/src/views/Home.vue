<template>
  <div class="home">
    <div class="home-container">
      <div class="section-header">
        <h2 class="section-title">
          <span class="text-gradient">推荐阅读</span>
        </h2>
        <div class="header-line"></div>
      </div>

      <div class="recommended">
        <div v-for="(item, index) in recommendations" :key="item.id || index" class="card glass-panel"
          :style="{ '--delay': `${index * 0.1}s` }" @click="openArticle(item.id)">
          <div class="card-image-wrapper">
            <el-image src="https://img.shetu66.com/2023/06/26/1687770031227597.png" alt="Cover Image"
              class="cover-image" fit="cover" />
            <div class="card-overlay">
              <span class="read-more-btn">阅读更多</span>
            </div>
          </div>
          <div class="card-content">
            <div class="title-wrapper">
              <span class="title">{{ item.title }}</span>
            </div>
            <div class="info-list">
              <div class="info-item">
                <span class="dot primary"></span>
                {{ getCategoryName(item.category) }}
              </div>
              <div class="info-item">
                <span class="dot secondary"></span>
                {{ item.author }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="content-wrapper">
        <div class="list-life glass-panel">
          <div class="list-header">
            <h3>最新文章</h3>
          </div>
          <div class="post-list">
            <transition-group name="list-anim">
              <div v-for="(post, index) in posts" :key="post.id || index" class="post-item-wrapper"
                :style="{ '--delay': `${index * 0.05}s` }" @click="openArticle(post.id)">
                <ArticleCard :post="post" />
              </div>
            </transition-group>
          </div>
          <div v-if="loading" class="loading">
            <div class="spinner"></div>
          </div>
          <div v-if="noMoreArticles" class="no-more">
            <span>— 到底啦 —</span>
          </div>
        </div>

        <div class="home-right">
          <div class="home-right-card glass-panel">
            <HotCard />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElSkeleton } from 'element-plus';
import HotCard from "@/components/widget/HotCard.vue";
import ArticleCard from "@/components/widget/ArticleCard.vue";
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
  margin-bottom: 24px;
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
  -webkit-text-fill-color: transparent;
}

.header-line {
  height: 2px;
  flex: 1;
  background: linear-gradient(90deg, var(--color-primary-light), transparent);
  border-radius: 2px;
  opacity: 0.5;
}

.recommended {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
  margin-bottom: 48px;
}

.card {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  animation: fadeInUp 0.6s backwards;
  animation-delay: var(--delay);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.glass-panel {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.9);
  border-color: var(--color-primary-light);
}

.card-image-wrapper {
  position: relative;
  overflow: hidden;
  aspect-ratio: 16/9;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.card:hover .cover-image {
  transform: scale(1.1);
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card:hover .card-overlay {
  opacity: 1;
}

.read-more-btn {
  color: white;
  border: 1px solid white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  backdrop-filter: blur(4px);
  transform: translateY(10px);
  transition: transform 0.3s ease;
}

.card:hover .read-more-btn {
  transform: translateY(0);
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title-wrapper {
  margin-bottom: 12px;
  flex: 1;
}

.title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-color-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  transition: color 0.2s;
}

.card:hover .title {
  color: var(--color-primary);
}

.info-list {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--text-color-secondary);
}

.info-item {
  display: flex;
  align-items: center;
}

.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 6px;
}

.dot.primary {
  background-color: var(--color-primary);
  box-shadow: 0 0 8px var(--color-primary-light);
}

.dot.secondary {
  background-color: var(--color-success);
}

.content-wrapper {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.list-life {
  flex: 1;
  border-radius: 24px;
  padding: 24px;
  min-height: 500px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.list-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.list-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-color-primary);
}

.post-item-wrapper {
  margin-bottom: 16px;
  border-radius: 16px;
  transition: all 0.3s;
  animation: fadeInUp 0.5s backwards;
  animation-delay: var(--delay);
}

.post-item-wrapper:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: translateX(4px);
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

.home-right {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 32px;
}

.home-right-card {
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive Design */
@media (max-width: 1200px) {
  .recommended {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 992px) {
  .recommended {
    grid-template-columns: repeat(3, 1fr);
  }

  .content-wrapper {
    flex-direction: column;
  }

  .home-right {
    width: 100%;
    margin-left: 0;
  }

  .list-life {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .recommended {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .recommended {
    grid-template-columns: 1fr;
  }
}
</style>