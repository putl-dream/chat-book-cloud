import { ref } from 'vue';
import { getCategoryPage, getNewPage, getTodayHotPage, getContentTypePage, getTagPage } from "@/views/article/_domain/article.js";
import { CATEGORY_ENUM, CONTENT_TYPE_ENUM } from "@/constants/index.js";

export function useHomeLogic() {
  const recommendations = ref([]);
  const posts = ref([]);
  const loading = ref(false);
  const noMoreArticles = ref(false);
  const page = ref(1);
  const pageSize = ref(10);

  // 当前筛选参数
  const currentFilters = ref({
    contentType: null,
    category: null,
    tagName: null
  });

  const apiStrategy = {
    // 学习/教程
    '/learn': {
      list: (p, s) => getContentTypePage(p, s, CONTENT_TYPE_ENUM.LEARN),
      recommend: () => getContentTypePage(1, 5, CONTENT_TYPE_ENUM.LEARN)
    },
    // 实战/项目
    '/practice': {
      list: (p, s) => getContentTypePage(p, s, CONTENT_TYPE_ENUM.PRACTICE),
      recommend: () => getContentTypePage(1, 5, CONTENT_TYPE_ENUM.PRACTICE)
    },
    // 保留旧分类路由（兼容性）
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
      recommend: () => getTodayHotPage(1, 5)
    }
  };

  // 按标签获取文章的策略
  const fetchTagArticles = async (authorTagName) => {
    if (loading.value || noMoreArticles.value) return;
    loading.value = true;

    try {
      const [listRes, recommendRes] = await Promise.all([
        getTagPage(page.value, pageSize.value, authorTagName),
        getTodayHotPage(1, 5)
      ]);

      if (recommendRes && recommendRes.list) {
        // 推荐区去重：过滤掉主列表已出现的文章
        // 只在第一页时更新推荐区，滚动加载更多时保持热点舞台不变
        if (page.value === 1) {
          const postIds = new Set(posts.value.map(p => p.id));
          recommendations.value = recommendRes.list.filter(r => !postIds.has(r.id));
        }
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
      console.error('Failed to fetch tag articles:', error);
    } finally {
      loading.value = false;
    }
  };

  const fetchPosts = async (currentPath) => {
    if (loading.value || noMoreArticles.value) return;
    loading.value = true;

    try {
      const strategy = apiStrategy[currentPath] || apiStrategy['default'];

      const [listRes, recommendRes] = await Promise.all([
        strategy.list(page.value, pageSize.value),
        strategy.recommend()
      ]);

      if (recommendRes && recommendRes.list) {
        // 推荐区去重：过滤掉主列表已出现的文章
        // 只在第一页时更新推荐区，滚动加载更多时保持热点舞台不变
        if (page.value === 1) {
          const postIds = new Set(posts.value.map(p => p.id));
          recommendations.value = recommendRes.list.filter(r => !postIds.has(r.id));
        }
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

  const resetPosts = () => {
    posts.value = [];
    recommendations.value = [];
    page.value = 1;
    noMoreArticles.value = false;
  };

  return {
    recommendations,
    posts,
    loading,
    noMoreArticles,
    currentFilters,
    fetchPosts,
    fetchTagArticles,
    resetPosts
  };
}
