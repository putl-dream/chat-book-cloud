<template>
  <div class="home">
    <div class="home-container">
      <div class="recommended">
        <div v-for="(item, index) in recommendations" :key="item.id || index" class="card"
          @click="openArticle(item.id)">
          <el-image src="https://img.shetu66.com/2023/06/26/1687770031227597.png" alt="Cover Image"
            class="cover-image" />
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
        <div class="list-life">
          <div v-for="(post, index) in posts" :key="post.id || index" class="post-item" @click="openArticle(post.id)">
            <ArticleCard :post="post" />
          </div>
          <div v-if="loading" class="loading">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-if="noMoreArticles" class="no-more">没有更多文章了</div>
        </div>

        <div class="home-right">
          <div class="home-right-card">
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

.recommended {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.card {
  background: var(--bg-color-white);
  border: 1px solid var(--border-color-light);
  border-radius: var(--border-radius-large);
  overflow: hidden;
  transition: var(--transition-base);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  box-shadow: var(--box-shadow-base);
}

.card:hover {
  transform: translateY(-6px);
  box-shadow: var(--box-shadow-hover);
  border-color: var(--color-primary-light);
}

.cover-image {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: cover;
  transition: var(--transition-base);
}

.card:hover .cover-image {
  transform: scale(1.05);
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
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-color-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  transition: var(--transition-fast);
}

.card:hover .title {
  color: var(--color-primary);
}

.info-list {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 0.75rem;
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
  background: var(--bg-color-white);
  border-radius: var(--border-radius-xl);
  padding: 8px;
  box-shadow: var(--box-shadow-base);
  border: 1px solid var(--border-color-light);
}

.post-item {
  border-bottom: 1px solid var(--border-color-lighter);
  padding: 4px;
  cursor: pointer;
  border-radius: var(--border-radius-large);
  transition: var(--transition-base);
}

.post-item:hover {
  background-color: var(--bg-color-light);
}

.post-item:last-child {
  border-bottom: none;
}

.loading {
  text-align: center;
  padding: 40px 0;
}

.no-more {
  text-align: center;
  padding: 40px 0;
  color: var(--text-color-secondary);
  font-size: 0.875rem;
}

.home-right {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 32px;
}

.home-right-card {
  background: var(--bg-color-white);
  border-radius: var(--border-radius-xl);
  box-shadow: var(--box-shadow-base);
  border: 1px solid var(--border-color-light);
  overflow: hidden;
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
