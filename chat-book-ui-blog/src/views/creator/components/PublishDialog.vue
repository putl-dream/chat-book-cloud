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
                <div class="form-label">文章标签 *</div>
                <div class="modern-tags">
                    <div v-for="tag in topicTags" :key="tag.id" class="modern-tag capsule"
                        :class="{ 'is-selected': selectedTopicTags.includes(tag.id) }" @click="toggleTopicTag(tag.id)">
                        <span class="tag-dot"
                            :style="{ backgroundColor: selectedTopicTags.includes(tag.id) ? (tag.color || 'var(--color-primary)') : '#d9d9d9' }"></span>
                        <span class="tag-name">{{ tag.name }}</span>
                    </div>
                </div>
            </div>

            <div class="form-item">
                <div class="form-label">技术栈 <span class="label-hint">（补充标签，可选 1-3 个）</span></div>
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
import {computed} from 'vue';
import {UploadFilled} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {ARTICLE_TYPE_NAMES, CREATION_STATEMENT_NAMES} from '@/constants';

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
    topicTags: {
        type: Array,
        default: () => [],
    },
    techTags: {
        type: Array,
        default: () => [],
    },
    pathTags: {
        type: Array,
        default: () => [],
    },
    selectedTopicTags: {
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
    summaryGenerating: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(['update:modelValue', 'confirm', 'change-topic-tags', 'change-tech-tags', 'change-path-tag', 'extract-summary']);

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

const toggleTopicTag = (id) => {
    const current = [...props.selectedTopicTags];
    const index = current.indexOf(id);
    if (index > -1) {
        current.splice(index, 1);
    } else {
        if (current.length < 5) {
            current.push(id);
        } else {
            ElMessage.warning('最多只能选择 5 个文章标签');
            return;
        }
    }
    emit('change-topic-tags', current);
};

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
</script>
