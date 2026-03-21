<template>
    <div class="tags-page">
        <div class="tags-header">
            <h1>技术标签</h1>
            <p class="subtitle">探索不同技术栈的优质文章</p>
        </div>

        <div class="tags-content">
            <div class="tag-section">
                <h2 class="section-title">
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

            <div class="tag-section">
                <h2 class="section-title">
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
import { getTagsByType } from '@/api/tag.js'
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
    text-align: center;
    margin-bottom: 48px;
}

.tags-header h1 {
    font-size: 32px;
    font-weight: 600;
    color: var(--text-color-primary);
    margin-bottom: 12px;
}

.subtitle {
    font-size: 16px;
    color: var(--text-color-secondary);
}

.tags-content {
    display: flex;
    flex-direction: column;
    gap: 48px;
}

.tag-section {
    background: var(--bg-color-white);
    border-radius: 16px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: 600;
    color: var(--text-color-primary);
    margin-bottom: 24px;
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
