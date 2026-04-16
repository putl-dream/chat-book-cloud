<template>
    <el-drawer
        v-model="visible"
        title="发布文章"
        direction="rtl"
        size="480px"
        :append-to-body="true"
        class="modern-drawer"
        header-class="modern-drawer__header"
        body-class="modern-drawer__body"
        footer-class="modern-drawer__footer"
        :close-on-click-modal="true">
        <div class="publish-form-content">
            <div class="form-item">
                <div class="form-label">作者标签 *</div>
                <el-select
                    :model-value="publishForm.authorTags"
                    class="modern-select"
                    multiple
                    filterable
                    remote
                    :remote-method="handleTagKeywordInput"
                    allow-create
                    default-first-option
                    reserve-keyword
                    :multiple-limit="5"
                    :popper-append-to-body="false"
                    placeholder="最多 5 个，前台展示只使用作者标签"
                    @update:model-value="handleAuthorTagsChange"
                    @change="handleAuthorTagsChange"
                    @visible-change="handleTagDropdownVisible">
                    <el-option
                        v-for="tag in authorTagOptions"
                        :key="tag.id || tag.name"
                        :label="tag.name"
                        :value="tag.name" />
                </el-select>
                <div class="field-caption">系统标签会根据后台映射自动生成，作者无需手动选择。</div>
            </div>

            <div class="form-item">
                <div class="form-label">文章类型 *</div>
                <div class="segmented-control wrap-control">
                    <div v-for="option in articleTypeOptions" :key="option.value" class="segment-item"
                        :class="{ active: publishForm.articleType === option.value }" @click="publishForm.articleType = option.value">
                        {{ option.label }}
                    </div>
                </div>
            </div>

            <div class="form-item">
                <div class="form-label">创作声明</div>
                <el-checkbox-group v-model="publishForm.creationStatements" class="modern-check-group">
                    <el-checkbox v-for="option in creationStatementOptions" :key="option.value" :label="option.value">
                        {{ option.label }}
                    </el-checkbox>
                </el-checkbox-group>
            </div>

            <div class="form-item">
                <div class="form-label">栏目分类 <span class="label-hint">（选填，兼容旧入口）</span></div>
                <el-select v-model="publishForm.category" placeholder="可选分类" clearable class="modern-select"
                    :popper-append-to-body="false">
                    <el-option v-for="option in normalizedCategoryOptions" :key="option.value" :label="option.label"
                        :value="option.value" />
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
                <div class="form-label">文章摘要</div>
                <div class="drawer-footer" style="justify-content: flex-start; margin-bottom: 12px;">
                    <el-button class="modern-btn btn-cancel" :loading="summaryGenerating" @click="$emit('extract-summary')">
                        {{ summaryGenerating ? '提取中...' : 'AI 提取摘要' }}
                    </el-button>
                </div>
                <el-input v-model="publishForm.abstractText" type="textarea" :rows="3" placeholder="一句话描述你的文章，或使用 AI 自动提取..."
                    class="modern-textarea" resize="none" />
            </div>

            <div class="form-item">
                <div class="form-label">文章封面</div>
                <el-upload class="modern-uploader" action="" :http-request="handleCoverUpload" :show-file-list="false"
                    :before-upload="beforeCoverUpload">
                    <div v-if="publishForm.cover" class="cover-preview-wrapper">
                        <img :src="publishForm.cover" alt="文章封面预览" class="cover-preview" />
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
import { ARTICLE_TYPE_NAMES, CREATION_STATEMENT_NAMES } from '@/constants';

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
    authorTagOptions: {
        type: Array,
        default: () => [],
    },
    handleCoverUpload: {
        type: Function,
        required: true,
    },
    beforeCoverUpload: {
        type: Function,
        required: true,
    },
    summaryGenerating: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(['update:modelValue', 'confirm', 'change-author-tags', 'search-author-tags', 'extract-summary']);

const visible = computed({
    get: () => props.modelValue,
    set: (value) => {
        emit('update:modelValue', value);
    }
});

const normalizedCategoryOptions = computed(() => (
    Object.entries(props.categoryOptions).map(([value, label]) => ({
        value: Number(value),
        label,
    }))
));

const articleTypeOptions = computed(() => (
    Object.entries(ARTICLE_TYPE_NAMES).map(([value, label]) => ({
        value,
        label,
    }))
));

const creationStatementOptions = computed(() => (
    Object.entries(CREATION_STATEMENT_NAMES).map(([value, label]) => ({
        value,
        label,
    }))
));

const contentType = computed({
    get: () => props.publishForm.contentType || 0,
    set: (value) => {
        props.publishForm.contentType = value;
    }
});

const handleAuthorTagsChange = (values = []) => {
    const normalized = [...new Set((Array.isArray(values) ? values : [])
        .map((item) => String(item || '').trim().replace(/\s+/g, ' '))
        .filter(Boolean))];
    if (normalized.length > 5) {
        ElMessage.warning('最多只能填写 5 个作者标签');
        emit('change-author-tags', normalized.slice(0, 5));
        return;
    }
    emit('change-author-tags', normalized);
};

const handleTagKeywordInput = (keyword) => {
    emit('search-author-tags', keyword);
};

const handleTagDropdownVisible = (visible) => {
    if (visible) {
        emit('search-author-tags', '');
    }
};
</script>
