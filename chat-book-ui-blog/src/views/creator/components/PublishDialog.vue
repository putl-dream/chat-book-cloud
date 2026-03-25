<template>
    <el-drawer v-model="visible" title="发布文章" direction="rtl" size="480px" :append-to-body="true" class="modern-drawer"
        :close-on-click-modal="true">
        <div class="publish-form-content">
            <div class="form-item">
                <div class="form-label">文章分类</div>
                <el-select v-model="publishForm.category" placeholder="请选择分类" class="modern-select"
                    :popper-append-to-body="false">
                    <el-option v-for="(name, key) in categoryOptions" :key="key" :label="name"
                        :value="parseInt(key, 10)" />
                </el-select>
            </div>

            <div class="form-item">
                <div class="form-label">内容类型</div>
                <div class="segmented-control">
                    <div class="segment-item" :class="{ active: contentType === 0 }" @click="contentType = 0">学习/教程
                    </div>
                    <div class="segment-item" :class="{ active: contentType === 1 }" @click="contentType = 1">实战/项目
                    </div>
                </div>
            </div>

            <div class="form-item">
                <div class="form-label">技术栈 <span class="label-hint">（可选 1-3 个）</span></div>
                <div class="modern-tags">
                    <div v-for="tag in techTags" :key="tag.id" class="modern-tag capsule"
                        :class="{ 'is-selected': selectedTechTags.includes(tag.id) }" @click="toggleTechTag(tag.id)">
                        <span class="tag-dot"
                            :style="{ backgroundColor: selectedTechTags.includes(tag.id) ? (tag.color || 'var(--color-primary)') : '#d9d9d9' }"></span>
                        <span class="tag-name">{{ tag.name }}</span>
                    </div>
                </div>
            </div>

            <div class="form-item">
                <div class="form-label">学习路径 <span class="label-hint">（可选 1 个）</span></div>
                <div class="segmented-control wrap-control">
                    <div v-for="tag in pathTags" :key="tag.id" class="segment-item"
                        :class="{ active: selectedPathTag === tag.id }" @click="selectPathTag(tag.id)">
                        {{ tag.name }}
                    </div>
                </div>
            </div>

            <div class="form-item">
                <div class="form-label">文章摘要</div>
                <el-input v-model="publishForm.abstractText" type="textarea" :rows="3" placeholder="一句话描述你的文章..."
                    class="modern-textarea" resize="none" />
            </div>

            <div class="form-item">
                <div class="form-label">文章封面</div>
                <el-upload class="modern-uploader" action="" :http-request="handleCoverUpload" :show-file-list="false"
                    :before-upload="beforeCoverUpload">
                    <div v-if="publishForm.cover" class="cover-preview-wrapper">
                        <img :src="publishForm.cover" class="cover-preview" />
                        <div class="cover-preview-overlay">更换封面</div>
                    </div>
                    <div v-else class="cover-uploader-trigger">
                        <el-icon class="upload-icon">
                            <UploadFilled />
                        </el-icon>
                        <span class="upload-text">点击上传封面</span>
                    </div>
                </el-upload>
            </div>
        </div>

        <template #footer>
            <div class="drawer-footer">
                <el-button class="modern-btn btn-cancel" @click="visible = false">取消</el-button>
                <el-button class="modern-btn btn-confirm" @click="$emit('confirm')">确定发布</el-button>
            </div>
        </template>
    </el-drawer>
</template>

<script setup>
import { computed } from 'vue';
import { UploadFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

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
    get: () => props.publishForm.contentType || 0,
    set: (value) => {
        props.publishForm.contentType = value;
    }
});

const toggleTechTag = (id) => {
    const current = [...props.selectedTechTags];
    const index = current.indexOf(id);
    if (index > -1) {
        current.splice(index, 1);
    } else {
        if (current.length < 3) {
            current.push(id);
        } else {
            ElMessage.warning('最多只能选择 3 个技术栈');
            return;
        }
    }
    emit('change-tech-tags', current);
};

const selectPathTag = (id) => {
    emit('change-path-tag', id);
};

console.log('PublishDialog rendering with Drawer UI');
</script>

<style scoped>
/* Publish Form Layout Container */
.publish-form-content {
    display: flex;
    flex-direction: column;
    gap: 28px;
}

.form-item {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.form-label {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color-primary, #333);
    display: flex;
    align-items: baseline;
    letter-spacing: 0.2px;
}

.label-hint {
    margin-left: 8px;
    font-size: 12px;
    font-weight: normal;
    color: var(--text-color-secondary, #888);
}

/* Base structural overrides for Ele UI Form Elements inside Drawer */
:deep(.modern-select .el-input__wrapper),
:deep(.modern-textarea .el-textarea__inner) {
    background-color: var(--bg-color-secondary, #f5f5f5) !important;
    box-shadow: none !important;
    border: none !important;
    border-radius: 12px !important;
    padding: 12px 16px !important;
    transition: all 0.25s cubic-bezier(0.2, 0, 0, 1) !important;
}

:deep(.modern-select .el-input__wrapper:hover),
:deep(.modern-textarea .el-textarea__inner:hover),
:deep(.modern-select .el-input__wrapper.is-focus),
:deep(.modern-textarea .el-textarea__inner:focus) {
    background-color: var(--bg-color-hover, #ebebeb) !important;
    box-shadow: 0 0 0 2px rgba(26, 26, 26, 0.05) !important;
    /* soft outline effect */
}

:deep(.modern-select .el-input__wrapper) {
    padding: 6px 16px !important;
    /* Adjust height for select */
}

/* Tag Styles */
.modern-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.modern-tag.capsule {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    background-color: var(--bg-color-secondary, #f5f5f5);
    border-radius: 999px;
    /* Capsule shape */
    font-size: 13px;
    color: var(--text-color-secondary, #666);
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.2, 0, 0, 1);
    user-select: none;
    border: 1px solid transparent;
}

.modern-tag.capsule:hover {
    background-color: var(--bg-color-hover, #ebebeb);
}

.modern-tag.capsule.is-selected {
    background-color: transparent;
    color: var(--text-color-primary, #333);
    border-color: rgba(0, 0, 0, 0.08);
    /* Subtle border for selected */
    background-color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    font-weight: 500;
}

.tag-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    transition: background-color 0.3s ease;
}

.modern-tag.capsule:hover .tag-dot {
    opacity: 0.8;
}

.tag-name {
    line-height: 1;
    margin-bottom: -1px;
    /* visual optical alignment */
}

/* Segmented Control */
.segmented-control {
    display: inline-flex;
    background-color: var(--bg-color-secondary, #f5f5f5);
    border-radius: 12px;
    padding: 4px;
    gap: 4px;
}

.segmented-control.wrap-control {
    flex-wrap: wrap;
}

.segment-item {
    padding: 8px 16px;
    font-size: 13px;
    color: var(--text-color-secondary, #666);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.2, 0, 0, 1);
    user-select: none;
    text-align: center;
}

.segment-item:hover {
    color: var(--text-color-primary, #333);
}

.segment-item.active {
    background-color: #ffffff;
    color: var(--text-color-primary, #1a1a1a);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    /* Soft shadow for active state */
    font-weight: 600;
}

/* Cover Uploader */
.modern-uploader {
    width: 100%;
}

:deep(.modern-uploader .el-upload) {
    display: block;
    width: 100%;
}

.cover-uploader-trigger {
    width: 100%;
    height: 160px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: var(--bg-color-secondary, #f5f5f5);
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.25s cubic-bezier(0.2, 0, 0, 1);
    gap: 12px;
    border: 1px dashed transparent;
}

.cover-uploader-trigger:hover {
    background-color: var(--bg-color-hover, #ebebeb);
}

.upload-icon {
    font-size: 28px;
    color: #a0a0a0;
    transition: color 0.3s ease;
}

.cover-uploader-trigger:hover .upload-icon {
    color: var(--text-color-primary, #333);
}

.upload-text {
    font-size: 13px;
    color: #888;
    font-weight: 500;
}

.cover-preview-wrapper {
    position: relative;
    width: 100%;
    height: 160px;
    border-radius: 16px;
    overflow: hidden;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    /* soft shadow */
}

.cover-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}

.cover-preview-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 500;
    opacity: 0;
    backdrop-filter: blur(4px);
    transition: all 0.25s ease;
}

.cover-preview-wrapper:hover .cover-preview {
    transform: scale(1.02);
}

.cover-preview-wrapper:hover .cover-preview-overlay {
    opacity: 1;
}

/* Footer Buttons */
.drawer-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.modern-btn {
    border-radius: 12px !important;
    padding: 10px 24px !important;
    height: 40px !important;
    font-weight: 500 !important;
    font-size: 14px !important;
    border: none !important;
    transition: all 0.25s cubic-bezier(0.2, 0, 0, 1) !important;
}

.modern-btn.btn-cancel {
    background-color: var(--bg-color-secondary, #f5f5f5) !important;
    color: var(--text-color-secondary, #666) !important;
}

.modern-btn.btn-cancel:hover {
    background-color: var(--bg-color-hover, #ebebeb) !important;
    color: var(--text-color-primary, #333) !important;
}

.modern-btn.btn-confirm {
    background-color: var(--text-color-primary, #1a1a1a) !important;
    /* Elegant dark button */
    color: #fff !important;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.1) !important;
}

.modern-btn.btn-confirm:hover {
    background-color: #333 !important;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15) !important;
    transform: translateY(-1px);
}
</style>

<style>
/* Global Drawer overrides to effect el-drawer from outside scoped block */
.modern-drawer {
    background: rgba(255, 255, 255, 0.85) !important;
    backdrop-filter: blur(24px) saturate(180%) !important;
    -webkit-backdrop-filter: blur(24px) saturate(180%) !important;
    box-shadow: -10px 0 40px rgba(0, 0, 0, 0.08) !important;
}

.modern-drawer .el-drawer__header {
    margin-bottom: 0 !important;
    padding: 24px 32px 16px !important;
    font-size: 20px !important;
    font-weight: 700 !important;
    color: var(--text-color-primary, #1a1a1a) !important;
    border-bottom: none !important;
}

.modern-drawer .el-drawer__body {
    padding: 16px 32px !important;
}

.modern-drawer .el-drawer__footer {
    padding: 20px 32px !important;
    border-top: none !important;
}

@media (max-width: 768px) {
    .modern-drawer {
        width: 100% !important;
    }

    .modern-drawer .el-drawer__body,
    .modern-drawer .el-drawer__header,
    .modern-drawer .el-drawer__footer {
        padding-left: 20px !important;
        padding-right: 20px !important;
    }
}
</style>
