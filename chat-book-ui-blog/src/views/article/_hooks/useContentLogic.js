import { ref } from 'vue';
import { getUserArticlePage } from "@/views/article/_domain/article.js";

export function useContentLogic() {
    const articles = ref([]);
    const loading = ref(false);
    const noMoreArticles = ref(false);
    const currentPage = ref(1);
    const pageSize = ref(10);

    const fetchArticles = async () => {
        if (loading.value || noMoreArticles.value) return;
        loading.value = true;
        try {
            const response = await getUserArticlePage(currentPage.value, pageSize.value);
            const newArticles = response.list;
            if (newArticles.length === 0) {
                noMoreArticles.value = true;
            } else {
                articles.value = currentPage.value === 1 ? newArticles : [...articles.value, ...newArticles];
                currentPage.value++;
            }
        } catch (error) {
            console.error('Failed to fetch articles:', error);
        } finally {
            loading.value = false;
        }
    };

    const handleScroll = (event) => {
        const { scrollTop, clientHeight, scrollHeight } = event.target;
        if (scrollTop + clientHeight >= scrollHeight - 10) {
            fetchArticles();
        }
    };

    return {
        articles,
        loading,
        noMoreArticles,
        fetchArticles,
        handleScroll
    };
}
