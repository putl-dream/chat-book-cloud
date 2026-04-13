<template>
  <div class="home">
    <div class="home-container">
      <div class="island-container">
        <div class="c-section-heading c-section-heading--line section-header">
          <h2 class="c-section-heading__title c-section-heading__title--xl">
            <span class="u-text-gradient">Bento 热点舞台</span>
          </h2>
          <div class="c-section-heading__line"></div>
        </div>

        <div class="hot-bento-grid" v-if="recommendations && recommendations.length > 0">
          <!-- 主热点(Large) -->
          <div class="hot-item main-feature interactive-card glass-effect hover-soft animate-pop"
               @click="openArticle(recommendations[0].id)">
            <ArticleCard :post="recommendations[0]" variant="feature"/>
          </div>

          <!-- 次热点 -->
          <template v-if="recommendations.length > 1">
            <div v-for="(item, index) in recommendations.slice(1, 5)"
                 class="hot-item interactive-card glass-effect hover-soft animate-pop" :key="'hot-' + (item.id || index)"
                 :style="{ '--delay': `${index * 0.1 + 0.1}s` }" @click="openArticle(item.id)">
              <ArticleCard :post="item" variant="bento-secondary"/>
            </div>
          </template>
        </div>
      </div>

      <div class="c-section-heading c-section-heading--line home-stream-heading" v-if="posts && posts.length > 0">
        <h2 class="c-section-heading__title c-section-heading__title--xl home-stream-title">
          <span class="animated-icon">🌊</span>
          <span class="u-text-gradient">最新流动流</span>
        </h2>
        <div class="c-section-heading__line"></div>
      </div>

      <div class="bento-waterfall">
        <template v-for="(post, index) in posts" :key="'post-' + (post.id || index)">
          <div class="bento-item interactive-card glass-effect hover-soft animate-delayed"
               :style="{ '--delay': `${index * 0.05}s` }" @click="openArticle(post.id)">
            <ArticleCard :post="post" :variant="getPostVariant(post, index)"/>
          </div>
        </template>
      </div>

      <div v-if="loading" class="loading c-loading-state">
        <div class="spinner c-spinner"></div>
      </div>
      <div v-if="noMoreArticles" class="no-more c-status-note">
        <span>到底啦</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import ArticleCard from "@/views/article/components/ArticleCard.vue";
import router from "@/router/index.js";
import {useHomeLogic} from "./_hooks/useHomeLogic.js";

const route = useRoute();

const currentPath = ref('/');
const currentTagId = ref(null);
const {recommendations, posts, loading, noMoreArticles, fetchPosts, fetchTagArticles, resetPosts} = useHomeLogic();

const getPostVariant = (post, index) => {
  if (index % 7 === 3 && post.cover) return 'large-image';
  if (index % 11 === 5) return 'text-only'; // 偶尔使用纯文本卡片，增加排版错落感
  return 'default';
};

const handleScroll = (e) => {
  const target = e.target;
  const scrollTop = target.scrollTop;
  const clientHeight = target.clientHeight;
  const scrollHeight = target.scrollHeight;

  if (scrollTop + clientHeight >= scrollHeight - 50) {
    if (currentTagId.value) {
      fetchTagArticles(currentTagId.value);
    } else {
      fetchPosts(currentPath.value);
    }
  }
};

const openArticle = async (id) => {
  await router.push({name: 'Article', params: {id: id}});
};

watch(
    () => route.path,
    async (newPath) => {
      resetPosts();
      // 检查是否是标签路由
      if (newPath.startsWith('/tag/')) {
        currentTagId.value = parseInt(route.params.tagId);
        currentPath.value = '/tags';
        await fetchTagArticles(currentTagId.value);
      } else {
        currentTagId.value = null;
        currentPath.value = newPath;
        await fetchPosts(newPath);
      }
    },
    {immediate: true}
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
