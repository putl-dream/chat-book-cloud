<template>
    <div class="tag-filter">
        <el-dropdown @command="handleTagSelect" trigger="click">
            <el-button type="primary" plain>
                标签筛�?<el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
                <el-dropdown-menu>
                    <div class="tag-group">
                        <div class="tag-group-title">技术栈</div>
                        <el-dropdown-item v-for="tag in techTags" :key="tag.id" :command="tag.id">
                            <el-tag :color="tag.color" size="small" effect="dark">{{ tag.name }}</el-tag>
                        </el-dropdown-item>
                    </div>
                    <el-divider style="margin: 8px 0" />
                    <div class="tag-group">
                        <div class="tag-group-title">学习路径</div>
                        <el-dropdown-item v-for="tag in pathTags" :key="tag.id" :command="tag.id">
                            <el-tag :color="tag.color" size="small" effect="dark">{{ tag.name }}</el-tag>
                        </el-dropdown-item>
                    </div>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
        <div v-if="selectedTag" class="selected-tag">
            <el-tag closable @close="clearTagFilter" :color="selectedTag.color" effect="dark">
                {{ selectedTag.name }}
            </el-tag>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { getTagsByType, getAllTags } from '@/views/article/_domain/tag.js'
import { TAG_TYPE_ENUM } from '@/constants/index.js'

const emit = defineEmits(['tag-change'])

const techTags = ref([])
const pathTags = ref([])
const selectedTagId = ref(null)
const selectedTag = computed(() => {
    const allTags = [...techTags.value, ...pathTags.value]
    return allTags.find(tag => tag.id === selectedTagId.value)
})

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

const handleTagSelect = (tagId) => {
    selectedTagId.value = tagId
    emit('tag-change', tagId)
}

const clearTagFilter = () => {
    selectedTagId.value = null
    emit('tag-change', null)
}

onMounted(() => {
    loadTags()
})
</script>

<style scoped>
.tag-filter {
    display: flex;
    align-items: center;
    gap: 12px;
}

.tag-group {
    padding: 4px 12px;
}

.tag-group-title {
    font-size: 12px;
    color: var(--text-color-secondary);
    margin-bottom: 8px;
    font-weight: 500;
}

.selected-tag {
    margin-left: 4px;
}
</style>
