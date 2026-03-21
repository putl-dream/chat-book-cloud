<template>
    <el-dialog v-model="visible" title="发布文章" width="500px">
        <el-form :model="publishForm" label-width="80px">
            <el-form-item label="文章分类">
                <el-select v-model="publishForm.category" placeholder="请选择分类">
                    <el-option v-for="(name, key) in categoryOptions" :key="key" :label="name"
                        :value="parseInt(key, 10)" />
                </el-select>
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
    handleCoverUpload: {
        type: Function,
        required: true,
    },
    beforeCoverUpload: {
        type: Function,
        required: true,
    },
});

const emit = defineEmits(['update:modelValue', 'confirm']);

const visible = computed({
    get: () => props.modelValue,
    set: (value) => {
        emit('update:modelValue', value);
    }
});
</script>

<style scoped>
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
</style>
