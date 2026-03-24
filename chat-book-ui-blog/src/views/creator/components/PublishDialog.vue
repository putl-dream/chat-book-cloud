<template>
    <el-dialog v-model="visible" title="发布文章" width="min(680px, 92vw)">
        <el-form :model="publishForm" label-width="88px" class="publish-form">
            <el-form-item label="文章分类">
                <el-select v-model="publishForm.category" placeholder="请选择分类">
                    <el-option v-for="(name, key) in categoryOptions" :key="key" :label="name"
                        :value="parseInt(key, 10)" />
                </el-select>
            </el-form-item>
            <el-form-item label="内容类型">
                <el-select v-model="contentType" placeholder="请选择内容类型">
                    <el-option label="学习/教程" :value="0" />
                    <el-option label="实战/项目" :value="1" />
                </el-select>
            </el-form-item>
            <el-form-item label="文章标签">
                <div class="tag-selection">
                    <div class="tag-group">
                        <span class="tag-group-label">技术栈（可选 1-3 个）</span>
                        <el-checkbox-group :model-value="selectedTechTags" @change="handleTechTagsChange">
                            <el-checkbox v-for="tag in techTags" :key="tag.id" :value="tag.id">
                                <el-tag :color="tag.color" size="small" effect="dark">{{ tag.name }}</el-tag>
                            </el-checkbox>
                        </el-checkbox-group>
                    </div>

                    <div class="tag-group">
                        <span class="tag-group-label">学习路径（可选 1 个）</span>
                        <el-radio-group :model-value="selectedPathTag" @change="handlePathTagChange">
                            <el-radio v-for="tag in pathTags" :key="tag.id" :value="tag.id">
                                <el-tag :color="tag.color" size="small" effect="dark">{{ tag.name }}</el-tag>
                            </el-radio>
                        </el-radio-group>
                    </div>
                </div>
            </el-form-item>
            <el-form-item label="文章摘要">
                <el-input v-model="publishForm.abstractText" type="textarea" :rows="3" placeholder="请输入文章摘要" />
            </el-form-item>
            <el-form-item label="文章封面">
                <el-upload class="avatar-uploader" action="" :http-request="handleCoverUpload" :show-file-list="false"
                    :before-upload="beforeCoverUpload">
                    <img v-if="publishForm.cover" :src="publishForm.cover" class="cover-preview" />
                    <el-icon v-else class="cover-uploader-trigger">
                        <Plus />
                    </el-icon>
                </el-upload>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="visible = false">取消</el-button>
                <el-button type="primary" @click="$emit('confirm')">确定发布</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import { computed } from 'vue';
import { Plus } from '@element-plus/icons-vue';

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false,
    },
    publishForm: {
        type: Object,
        required: true,
    },
    categoryOptions: {
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
    handleCoverUpload: {
        type: Function,
        required: true,
    },
    beforeCoverUpload: {
        type: Function,
        required: true,
    },
});

const emit = defineEmits(['update:modelValue', 'confirm', 'change-tech-tags', 'change-path-tag']);

const visible = computed({
    get: () => props.modelValue,
    set: (value) => {
        emit('update:modelValue', value);
    }
});

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
.publish-form {
    max-height: min(70vh, 760px);
    overflow-y: auto;
    padding-right: 8px;
}

.tag-selection {
    display: flex;
    flex-direction: column;
    gap: 14px;
    width: 100%;
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

:deep(.el-checkbox-group),
:deep(.el-radio-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 12px;
}

:deep(.el-checkbox),
:deep(.el-radio) {
    margin-right: 0;
}

.cover-preview {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 8px;
}

.cover-uploader-trigger {
    width: 100px;
    height: 100px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1px dashed var(--border-color-base);
    border-radius: 8px;
    cursor: pointer;
    overflow: hidden;
    color: var(--text-color-secondary);
    transition: var(--transition-fast);
}

.cover-uploader-trigger:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    background: var(--color-primary-light);
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

@media (max-width: 768px) {
    .publish-form {
        max-height: 60vh;
    }
}
</style>
