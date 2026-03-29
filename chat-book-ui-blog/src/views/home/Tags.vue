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
