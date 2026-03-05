import { ref } from 'vue';
import { getCategoryPage, getNewPage, getSystemRecommendPage } from "@/api/article.js";
import { CATEGORY_ENUM } from "@/constants/index.js";

export function useHomeLogic() {
  const recommendations = ref([]);
  const posts = ref([]);
  const loading = ref(false);
  const noMoreArticles = ref(false);
  const page = ref(1);
  const pageSize = ref(10);

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
    fetchPosts,
    resetPosts
  };
}
