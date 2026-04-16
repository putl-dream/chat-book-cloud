<template>
    <div class="article-tag-card c-solid-panel">
        <h3 class="card-title c-panel-title c-panel-title--compact">作者标签</h3>
        <div v-if="authorTags.length > 0" class="tag-list">
            <el-tag
                v-for="tagName in authorTags"
                :key="tagName"
                size="small"
                effect="dark"
                class="tag-item"
                @click="goToTag(tagName)">
                {{ tagName }}
            </el-tag>
        </div>
        <div v-else class="empty-state c-empty-panel c-empty-panel--plain">
            <span>暂无标签</span>
        </div>
    </div>
</template>

<script setup>
import { toRef } from 'vue';
import { useArticleTagCard } from '../_hooks/useArticleTagCard.js';

const props = defineProps({
    articleId: {
        type: [Number, String],
        required: true
    },
    authorTags: {
        type: Array,
        default: () => []
    }
});

const authorTagsRef = toRef(props, 'authorTags');
const { authorTags, goToTag } = useArticleTagCard(authorTagsRef);
</script>
