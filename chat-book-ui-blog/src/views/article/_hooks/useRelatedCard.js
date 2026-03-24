import { ref, onMounted } from 'vue';
import { getRelatedPage } from "@/views/article/_domain/article.js";
import { useRouter } from 'vue-router';

export function useRelatedCard(articleIdRef) {
    const relatedArticles = ref([]);
    const router = useRouter();

    const getRankClass = (index) => {
        if (index === 0) return 'rank-1';
        if (index === 1) return 'rank-2';
        if (index === 2) return 'rank-3';
        return 'rank-other';
    };

    const queryRelatedRequest = async () => {
        try {
            const articleId = typeof articleIdRef === 'object' ? articleIdRef.value : articleIdRef;
            if (!articleId) return;
            const response = await getRelatedPage(articleId, 1, 10);
            if (response && response.list) {
                relatedArticles.value = response.list;
            } else if (response && response.records) {
                relatedArticles.value = response.records;
            }
        } catch (error) {
            console.error('Failed to fetch related articles:', error);
        }
    };

    const openArticle = async (id) => {
        await router.push({ name: 'Article', params: { id: id } });
    };

    return {
        relatedArticles,
        getRankClass,
        queryRelatedRequest,
        openArticle
    };
}
