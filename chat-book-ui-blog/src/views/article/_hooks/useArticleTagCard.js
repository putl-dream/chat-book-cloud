import { computed } from 'vue';
import { useRouter } from 'vue-router';

export function useArticleTagCard(authorTagsRef) {
    const router = useRouter();

    const authorTags = computed(() => {
        const tags = typeof authorTagsRef === 'object' ? authorTagsRef.value : authorTagsRef;
        return Array.isArray(tags) ? tags.filter(Boolean) : [];
    });

    const goToTag = async (tagName) => {
        if (!tagName) {
            return;
        }
        await router.push(`/tag/${encodeURIComponent(tagName)}`);
    };

    return {
        authorTags,
        goToTag
    };
}
