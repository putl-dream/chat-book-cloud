<template>
    <div class="tag-filter">
        <el-dropdown @command="handleTagSelect" trigger="click">
            <el-button type="primary" plain>
                作者标签筛选 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
                <el-dropdown-menu>
                    <div class="tag-group">
                        <div class="tag-group-title">热门作者标签</div>
                        <el-dropdown-item
                            v-for="tag in hotTags"
                            :key="tag.id || tag.name"
                            :command="tag.name"
                        >
                            <span class="tag-option-name">{{ tag.name }}</span>
                            <span v-if="tag.articleCount" class="tag-option-count">{{ tag.articleCount }}</span>
                        </el-dropdown-item>
                    </div>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
        <div v-if="selectedTagName" class="selected-tag">
            <el-tag closable effect="dark" @close="clearTagFilter">
                {{ selectedTagName }}
            </el-tag>
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { getHotAuthorTags } from '@/views/article/_domain/tag.js'

const emit = defineEmits(['tag-change'])

const hotTags = ref([])
const selectedTagName = ref('')

const loadTags = async () => {
    try {
        hotTags.value = await getHotAuthorTags(20) || []
    } catch (error) {
        console.error('加载作者标签失败:', error)
    }
}

const handleTagSelect = (tagName) => {
    selectedTagName.value = tagName
    emit('tag-change', tagName)
}

const clearTagFilter = () => {
    selectedTagName.value = ''
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
    min-width: 180px;
    padding: 4px 12px;
}

.tag-group-title {
    margin-bottom: 8px;
    color: var(--text-color-secondary);
    font-size: 12px;
    font-weight: 500;
}

.tag-option-name {
    margin-right: 8px;
}

.tag-option-count {
    color: var(--text-color-secondary);
    font-size: 12px;
}

.selected-tag {
    margin-left: 4px;
}
</style>
