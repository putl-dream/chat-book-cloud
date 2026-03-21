<template>
    <div class="right-sidebar">
        <div class="right-sidebar-header">
            <span class="right-sidebar-title">属性设置</span>
            <el-icon class="close-icon" @click="$emit('close')">
                <Close />
            </el-icon>
        </div>
        <div class="right-sidebar-body">
            <el-form label-width="80px" size="small">
                <el-form-item label="内容类型">
                    <el-select v-model="contentType" placeholder="请选择内容类型">
                        <el-option label="学习/教程" :value="0" />
                        <el-option label="实战/项目" :value="1" />
                    </el-select>
                </el-form-item>
                <el-form-item label="文章标签">
                    <div class="tag-selection">
                        <div class="tag-group">
                            <span class="tag-group-label">技术栈（可选1-3个）</span>
                            <el-checkbox-group :model-value="selectedTechTags" @change="handleTechTagsChange">
                                <el-checkbox v-for="tag in techTags" :key="tag.id" :value="tag.id">
                                    <el-tag :color="tag.color" size="small" effect="dark">{{ tag.name }}</el-tag>
                                </el-checkbox>
                            </el-checkbox-group>
                        </div>
                        <div class="tag-group">
                            <span class="tag-group-label">学习路径（可选1个）</span>
                            <el-radio-group :model-value="selectedPathTag" @change="handlePathTagChange">
                                <el-radio v-for="tag in pathTags" :key="tag.id" :value="tag.id">
                                    <el-tag :color="tag.color" size="small" effect="dark">{{ tag.name }}</el-tag>
                                </el-radio>
                            </el-radio-group>
                        </div>
                    </div>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
    publishForm: {
        type: Object,
        required: true,
    },
    techTags: {
        type: Array,
        default: () => [],
    },
    pathTags: {
        type: Array,
        default: () => [],
    },
    selectedTechTags: {
        type: Array,
        default: () => [],
    },
    selectedPathTag: {
        type: [Number, String],
        default: null,
    },
});

const emit = defineEmits(['change-tech-tags', 'change-path-tag', 'close']);

const contentType = computed({
    get: () => props.publishForm.contentType,
    set: (value) => {
        props.publishForm.contentType = value;
    }
});

const handleTechTagsChange = (value) => {
    emit('change-tech-tags', value);
};

const handlePathTagChange = (value) => {
    emit('change-path-tag', value);
};
</script>

<style scoped>
.right-sidebar {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.right-sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid var(--border-color-base);
}

.right-sidebar-title {
    font-weight: 600;
    color: var(--text-color-primary);
}

.close-icon {
    cursor: pointer;
    font-size: 18px;
    color: var(--text-color-regular);
    transition: color 0.2s ease;
}

.close-icon:hover {
    color: var(--text-color-primary);
}

.right-sidebar-body {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
}

.tag-selection {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.tag-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.tag-group-label {
    font-size: 12px;
    color: var(--text-color-secondary);
}
</style>
