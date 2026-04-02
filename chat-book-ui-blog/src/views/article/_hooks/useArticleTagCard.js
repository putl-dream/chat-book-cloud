import { ref, computed, onMounted } from 'vue';
import { getTagsByType } from '@/views/article/_domain/tag.js';
import { TAG_TYPE_ENUM } from '@/constants/index.js';
import { useRouter } from 'vue-router';

export function useArticleTagCard(tagIdsRef) {
    const allTags = ref([]);
    const router = useRouter();

    const articleTags = computed(() => {
        const tagIds = typeof tagIdsRef === 'object' ? tagIdsRef.value : tagIdsRef;
        if (!Array.isArray(tagIds) || tagIds.length === 0) {
            return [];
        }
        return allTags.value.filter(tag => tagIds.includes(tag.id));
    });

    const techTags = computed(() => {
        return articleTags.value.filter(tag => tag.type === TAG_TYPE_ENUM.TECH);
    });

    const pathTags = computed(() => {
        return articleTags.value.filter(tag => tag.type === TAG_TYPE_ENUM.PATH);
    });

    const topicTags = computed(() => {
        return articleTags.value.filter(tag => tag.type === TAG_TYPE_ENUM.TOPIC);
    });

    const loadAllTags = async () => {
        try {
            const [topicRes, techRes, pathRes] = await Promise.all([
                getTagsByType(TAG_TYPE_ENUM.TOPIC),
                getTagsByType(TAG_TYPE_ENUM.TECH),
                getTagsByType(TAG_TYPE_ENUM.PATH)
            ]);
            allTags.value = [...(topicRes || []), ...(techRes || []), ...(pathRes || [])];
        } catch (error) {
            console.error('加载标签失败:', error);
        }
    };

    const goToTag = async (tagId) => {
        await router.push({ name: 'TagArticles', params: { tagId } });
    };

    return {
        articleTags,
        topicTags,
        techTags,
        pathTags,
        loadAllTags,
        goToTag
    };
}
