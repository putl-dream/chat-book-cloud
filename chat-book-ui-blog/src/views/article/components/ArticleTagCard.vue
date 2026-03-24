<template>
    <div class="article-tag-card">
        <h3 class="card-title">文章标签</h3>
        <div v-if="articleTags.length > 0" class="tag-list">
            <div v-if="techTags.length > 0" class="tag-section">
                <div class="tag-section-title">技术栈</div>
                <div class="tag-items">
                    <el-tag
                        v-for="tag in techTags"
                        :key="tag.id"
                        :color="tag.color"
                        size="small"
                        effect="dark"
                        class="tag-item"
                        @click="goToTag(tag.id)">
                        {{ tag.name }}
                    </el-tag>
                </div>
            </div>
            <div v-if="pathTags.length > 0" class="tag-section">
                <div class="tag-section-title">学习路径</div>
                <div class="tag-items">
                    <el-tag
                        v-for="tag in pathTags"
                        :key="tag.id"
                        :color="tag.color"
                        size="small"
                        effect="dark"
                        class="tag-item"
                        @click="goToTag(tag.id)">
                        {{ tag.name }}
                    </el-tag>
                </div>
            </div>
        </div>
        <div v-else class="empty-state">
            <span>暂无标签</span>
        </div>
    </div>
</template>

<script setup>
import { onMounted, toRef } from 'vue';
import { useArticleTagCard } from '../_hooks/useArticleTagCard.js';

const props = defineProps({
    articleId: {
        type: [Number, String],
        required: true
    },
    tagIds: {
        type: Array,
        default: () => []
    }
});

const tagIdsRef = toRef(props, 'tagIds');
const { articleTags, techTags, pathTags, loadAllTags, goToTag } = useArticleTagCard(tagIdsRef);

onMounted(async () => {
    await loadAllTags();
});
</script>

<style scoped>
.article-tag-card {
    padding: 20px;
    background: var(--bg-color-white);
}

.card-title {
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-color-primary);
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.card-title::before {
    content: '';
    width: 4px;
    height: 16px;
    background: var(--color-primary);
    border-radius: var(--border-radius-round);
}

.tag-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.tag-section {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.tag-section-title {
    font-size: 0.75rem;
    color: var(--text-color-secondary);
    font-weight: 500;
}

.tag-items {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.tag-item {
    cursor: pointer;
    transition: all 0.2s ease;
    border: none;
}

.tag-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.empty-state {
    text-align: center;
    padding: 16px 0;
    color: var(--text-color-secondary);
    font-size: 0.875rem;
}
</style>
