<template>
    <div class="tags-page">
        <div class="tags-header c-page-header c-page-header--center">
            <h1 class="c-page-header__title">技术标签</h1>
            <p class="subtitle c-page-header__subtitle">探索不同技术栈的优质文章</p>
        </div>

        <div class="tags-content">
            <div class="tag-section c-solid-panel">
                <h2 class="c-section-heading c-section-heading--compact c-section-heading__title c-section-heading__title--md">
                    <el-icon><Monitor /></el-icon>
                    技术栈
                </h2>
                <div class="tag-cloud">
                    <el-tag
                        v-for="tag in techTags"
                        :key="tag.id"
                        :color="tag.color"
                        effect="dark"
                        class="tag-item"
                        @click="goToTagArticles(tag)"
                    >
                        {{ tag.name }}
                    </el-tag>
                </div>
            </div>

            <div class="tag-section c-solid-panel">
                <h2 class="c-section-heading c-section-heading--compact c-section-heading__title c-section-heading__title--md">
                    <el-icon><Guide /></el-icon>
                    学习路径
                </h2>
                <div class="tag-cloud">
                    <el-tag
                        v-for="tag in pathTags"
                        :key="tag.id"
                        :color="tag.color"
                        effect="dark"
                        class="tag-item"
                        @click="goToTagArticles(tag)"
                    >
                        {{ tag.name }}
                    </el-tag>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, Guide } from '@element-plus/icons-vue'
import { getTagsByType } from '@/views/article/_domain/tag.js'
import { TAG_TYPE_ENUM } from '@/constants/index.js'

const router = useRouter()

const techTags = ref([])
const pathTags = ref([])

const loadTags = async () => {
    try {
        const [techRes, pathRes] = await Promise.all([
            getTagsByType(TAG_TYPE_ENUM.TECH),
            getTagsByType(TAG_TYPE_ENUM.PATH)
        ])
        // http.js already unwraps CommonResult, data is directly available
        techTags.value = techRes || []
        pathTags.value = pathRes || []
    } catch (error) {
        console.error('加载标签失败:', error)
    }
}

const goToTagArticles = (tag) => {
    router.push(`/tag/${tag.id}`)
}

onMounted(() => {
    loadTags()
})
</script>

<style scoped>
.tags-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 40px 20px;
}

.tags-header {
    --page-header-margin: 0 0 48px;
}

.subtitle {
    --page-header-subtitle-size: 16px;
}

.tags-content {
    display: flex;
    flex-direction: column;
    gap: 48px;
}

.tag-section {
    --solid-panel-padding: 32px;
    --solid-panel-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.tag-cloud {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.tag-item {
    cursor: pointer;
    padding: 8px 16px;
    font-size: 14px;
    border-radius: 16px;
    transition: transform 0.2s, box-shadow 0.2s;
}

.tag-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
