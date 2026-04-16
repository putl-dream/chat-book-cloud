<template>
    <div class="tags-page">
        <div class="tags-header c-page-header c-page-header--center">
            <h1 class="c-page-header__title">作者标签</h1>
            <p class="subtitle c-page-header__subtitle">前台只展示作者标签，系统标签改由后台治理与推荐使用。</p>
        </div>

        <div class="tags-content">
            <div class="tag-section c-solid-panel">
                <h2 class="c-section-heading c-section-heading--compact c-section-heading__title c-section-heading__title--md">
                    <el-icon><Collection /></el-icon>
                    热门作者标签
                </h2>
                <div class="tag-cloud">
                    <el-tag
                        v-for="tag in hotTags"
                        :key="tag.id"
                        effect="dark"
                        class="tag-item"
                        @click="goToTagArticles(tag)"
                    >
                        {{ tag.name }}
                        <span v-if="tag.articleCount" class="tag-count">{{ tag.articleCount }}</span>
                    </el-tag>
                </div>
                <p v-if="hotTags.length === 0" class="empty-tip">暂时还没有可展示的作者标签。</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Collection } from '@element-plus/icons-vue'
import { getHotAuthorTags } from '@/views/article/_domain/tag.js'

const router = useRouter()
const hotTags = ref([])

const loadTags = async () => {
    try {
        hotTags.value = await getHotAuthorTags(30) || []
    } catch (error) {
        console.error('加载作者标签失败:', error)
    }
}

const goToTagArticles = (tag) => {
    router.push(`/tag/${encodeURIComponent(tag.name)}`)
}

onMounted(() => {
    loadTags()
})
</script>
