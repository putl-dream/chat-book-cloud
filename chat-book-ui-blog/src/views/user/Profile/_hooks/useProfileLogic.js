import { ref } from 'vue';
import { getUserBySelf } from "@/api/user.js";
import { getUserArticlePage } from "@/api/article.js";

export function useProfileLogic() {
  const user = ref({});
  const posts = ref([]);
  const activeTab = ref('articles');
  const loading = ref(false);

  const fetchUserData = async () => {
    try {
      const res = await getUserBySelf();
      if (res) {
        user.value = res;
      }
    } catch (error) {
      console.error('获取用户信息失败', error);
    }
  };

  const fetchUserPosts = async () => {
    loading.value = true;
    try {
      const res = await getUserArticlePage(1, 10);
      if (res && res.list) {
        posts.value = res.list;
      }
    } catch (error) {
      console.error('获取文章失败', error);
    } finally {
      loading.value = false;
    }
  };

  return {
    user,
    posts,
    activeTab,
    loading,
    fetchUserData,
    fetchUserPosts
  };
}
