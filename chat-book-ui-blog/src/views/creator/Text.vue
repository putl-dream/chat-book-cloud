<template>
    <div class="editor-layout">
        <CreativeHeader class="site-header">
            <template #actions>
                <div class="editor-header-actions">
                    <el-button
                        class="editor-header-btn editor-header-btn--secondary"
                        :class="{ 'mobile-icon-btn': layoutState.isMobile }"
                        :loading="isSaving"
                        @click="saveContent"
                    >
                        <el-icon v-if="layoutState.isMobile"><DocumentChecked /></el-icon>
                        <span v-else>保存草稿</span>
                    </el-button>
                    <el-button
                        class="editor-header-btn editor-header-btn--accent"
                        :class="{ 'mobile-icon-btn': layoutState.isMobile }"
                        @click="publishContent"
                    >
                        <el-icon v-if="layoutState.isMobile"><Promotion /></el-icon>
                        <span v-else>发布文章</span>
                    </el-button>
                </div>
            </template>
        </CreativeHeader>

        <div class="text-toolbar">
            <div class="toolbar-wrapper">
                <TiptapToolbar
                    v-if="editor"
                    :editor="editor"
                    class="glass-toolbar"
                    @toggle-toc="toggleLeft"
                    :tocVisible="layoutState.leftOpen"
                    :spellcheck-enabled="spellcheckEnabled"
                    @toggle-spellcheck="toggleSpellcheck"
                />
                <div class="toolbar-meta">
                    <div class="status-bar">
                        <div class="status-indicator" :class="{ 'saving': saveState === 'saving' || aiGenerating }"></div>
                        <el-text class="status-text">{{ statusText }}</el-text>
                    </div>
                    <div class="word-count-chip">
                        <span class="word-count-label">字数</span>
                        <span class="word-count-value">{{ wordCount }}</span>
                    </div>
                </div>
                <el-button
                    v-if="!layoutState.isMobile"
                    @click="toggleRight"
                    size="small"
                    class="sidebar-toggle-btn"
                    :class="{ 'is-open': layoutState.rightOpen }"
                >
                    <span class="sidebar-toggle-btn__badge">AI</span>
                    <span class="sidebar-toggle-btn__label">智能助手</span>
                    <span class="sidebar-toggle-btn__status" :class="{ 'is-active': layoutState.rightOpen }">
                        {{ layoutState.rightOpen ? '已展开' : '打开' }}
                    </span>
                </el-button>
            </div>
        </div>

        <div
            class="editor-container"
            ref="containerRef"
            @mousemove="handleMouseMove"
            @mouseup="onMouseUp"
            @mouseleave="onMouseUp"
        >
            <div
                v-if="!layoutState.isMobile"
                class="layout-left"
                :class="{ 'is-dragging': dragging === 'left' }"
                v-show="layoutState.leftOpen"
                :style="{ width: layoutState.leftWidth + '%' }"
            >
                <ArticleToc v-if="editor" :editor="editor" />
            </div>

            <el-drawer
                v-if="layoutState.isMobile"
                v-model="layoutState.leftOpen"
                title="文档大纲"
                direction="ltr"
                size="70%"
                :append-to-body="true"
            >
                <div @click="setTimeout(() => layoutState.leftOpen = false, 100)">
                    <ArticleToc v-if="editor" :editor="editor" />
                </div>
            </el-drawer>

            <div
                v-if="!layoutState.isMobile"
                class="layout-splitter"
                v-show="layoutState.leftOpen"
                @mousedown.prevent="startDrag('left')"
            ></div>

            <div class="layout-content" :class="{ 'is-dragging': dragging }" :style="{ width: contentWidth + '%' }">
                <div class="scroll-area" ref="scrollAreaRef" @scroll="handleEditorScroll">
                    <div class="main-content">
                        <div
                            v-if="aiGenerating || aiGenerationStopped"
                            class="ai-generation-banner"
                            :class="{ 'is-stopped': aiGenerationStopped }"
                        >
                            <div class="ai-generation-banner__copy">
                                <span class="ai-generation-banner__eyebrow">
                                    {{ aiGenerationStopped ? '已停止生成' : 'AI 初稿生成中' }}
                                </span>
                                <strong>{{ statusText }}</strong>
                                <p>
                                    {{
                                        aiGenerationStopped
                                            ? '已保留当前内容，正文已经完全交还给你。'
                                            : '正文会按稳定段落流式进入编辑器。你现在可以先构思标题和摘要。'
                                    }}
                                </p>
                            </div>
                            <div class="ai-generation-banner__actions">
                                <button
                                    v-if="aiGenerating && !autoScrollEnabled"
                                    class="scroll-follow-chip"
                                    type="button"
                                    @click="resumeAutoScroll"
                                >
                                    回到底部继续跟随
                                </button>
                                <el-button v-if="aiGenerating" class="stop-generation-btn" @click="handleStopGeneration">
                                    停止生成
                                </el-button>
                            </div>
                        </div>

                        <div class="title-area">
                            <input
                                type="text"
                                v-model="title"
                                placeholder="请输入文章标题"
                                class="title-input"
                                @input="handleTitleInput"
                            />
                        </div>

                        <div class="summary-area" :class="{ 'is-generating': aiGenerating }">
                            <div class="summary-area__header">
                                <span class="summary-area__label">文章摘要</span>
                                <span class="summary-area__hint">
                                    {{ aiGenerating ? '你手动填写后，AI 不会在结束时覆盖。' : '可先手写摘要，也可在发布前再让 AI 提取。' }}
                                </span>
                            </div>
                            <textarea
                                v-model="publishForm.abstractText"
                                class="summary-textarea"
                                placeholder="先写一句你希望文章最终留下的核心结论。"
                                @input="handleSummaryInput"
                            />
                        </div>

                        <RichTextEditor
                            :editor="editor"
                            class="main-content-editor"
                            placeholder="请输入内容..."
                            variant="article"
                            :editable="editorContentEditable"
                            :theme="articleTheme"
                        />
                    </div>
                </div>
            </div>

            <div
                v-if="!layoutState.isMobile"
                class="layout-splitter"
                v-show="layoutState.rightOpen"
                @mousedown.prevent="startDrag('right')"
            ></div>

            <div
                v-if="!layoutState.isMobile"
                class="layout-right"
                :class="{ 'is-dragging': dragging === 'right' }"
                v-show="layoutState.rightOpen"
                :style="{ width: layoutState.rightWidth + '%' }"
            >
                <EditorAiPanel @close="toggleRight" />
            </div>

            <el-drawer
                v-if="layoutState.isMobile"
                v-model="layoutState.rightOpen"
                title="AI 智能助手"
                direction="btt"
                size="80vh"
                :append-to-body="true"
            >
                <EditorAiPanel @close="toggleRight" />
            </el-drawer>
        </div>

        <div v-if="layoutState.isMobile" class="mobile-fab-container">
            <button class="mobile-ai-fab" @click="toggleRight">
                <el-icon><MagicStick /></el-icon>
            </button>
        </div>

        <PublishDialog
            v-model="publishDialogVisible"
            :publish-form="publishForm"
            :category-options="categoryOptions"
            :topic-tags="topicTags"
            :tech-tags="techTags"
            :path-tags="pathTags"
            :selected-topic-tags="selectedTopicTags"
            :selected-tech-tags="selectedTechTags"
            :selected-path-tag="selectedPathTag"
            @change-topic-tags="handleTopicTagsChange"
            @extract-summary="handleExtractSummary"
            :summary-generating="summaryGenerating"
            @change-tech-tags="handleTechTagsChange"
            @change-path-tag="handlePathTagChange"
            :handle-cover-upload="handleCoverUpload"
            :before-cover-upload="beforeCoverUpload"
            @confirm="confirmPublish"
        />
    </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue';
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRouter } from 'vue-router';
import { useEditorLogic } from './_hooks/useEditorLogic.js';
import { CATEGORY_NAMES } from '@/constants';
import CreativeHeader from '@/views/creator/components/CreativeHeader.vue';
import TiptapToolbar from '@/views/creator/components/TiptapToolbar.vue';
import EditorAiPanel from '@/views/creator/components/EditorAiPanel.vue';
import PublishDialog from '@/views/creator/components/PublishDialog.vue';
import ArticleToc from '@/views/article/components/ArticleToc.vue';
import RichTextEditor from '@/components/common/rich-text/RichTextEditor.vue';
import { useSiteTheme } from '@/composables/useSiteTheme.js';
import { ElMessageBox } from 'element-plus';
import { DocumentChecked, Promotion, MagicStick } from '@element-plus/icons-vue';

const containerRef = ref(null);
const scrollAreaRef = ref(null);
const autoScrollEnabled = ref(true);

const {
    title,
    wordCount,
    publishDialogVisible,
    publishForm,
    topicTags,
    layoutState,
    dragging,
    techTags,
    pathTags,
    selectedTopicTags,
    selectedTechTags,
    selectedPathTag,
    isSaving,
    summaryGenerating,
    saveState,
    statusText,
    contentWidth,
    editor,
    spellcheckEnabled,
    hasUnsavedChanges,
    userId,
    articleId,
    aiGenerating,
    aiGenerationStopped,
    aiRenderTick,
    editorContentEditable,
    updateTagIds,
    handleCoverUpload,
    beforeCoverUpload,
    handleExtractSummary,
    toggleLeft,
    toggleRight,
    startDrag,
    onMouseMove,
    onMouseUp,
    toggleSpellcheck,
    submitSaveDraft,
    confirmPublish,
    clearDraft,
    stopAgentDraftGeneration,
    handleUserTitleInput,
    handleUserSummaryInput
} = useEditorLogic();

const categoryOptions = CATEGORY_NAMES;
const { articleTheme } = useSiteTheme();
const router = useRouter();

const handleMouseMove = (event) => {
    onMouseMove(event, containerRef.value);
};

const isNearBottom = (target) => (target.scrollHeight - target.scrollTop - target.clientHeight) < 48;

const scrollToBottom = async (behavior = 'smooth') => {
    await nextTick();
    const target = scrollAreaRef.value;
    if (!target) return;
    if (typeof target.scrollTo === 'function') {
        target.scrollTo({
            top: target.scrollHeight,
            behavior
        });
        return;
    }
    target.scrollTop = target.scrollHeight;
};

const handleEditorScroll = (event) => {
    const target = event.target;
    if (!target || !aiGenerating.value) {
        return;
    }
    autoScrollEnabled.value = isNearBottom(target);
};

const resumeAutoScroll = () => {
    autoScrollEnabled.value = true;
    scrollToBottom();
};

const handleStopGeneration = () => {
    stopAgentDraftGeneration();
    autoScrollEnabled.value = true;
};

const handleTitleInput = () => {
    handleUserTitleInput();
};

const handleSummaryInput = () => {
    handleUserSummaryInput();
};

const handleTopicTagsChange = (value) => {
    selectedTopicTags.value = value;
    updateTagIds();
};

const handleTechTagsChange = (value) => {
    selectedTechTags.value = value;
    updateTagIds();
};

const handlePathTagChange = (value) => {
    selectedPathTag.value = value;
    updateTagIds();
};

const saveContent = async () => {
    await submitSaveDraft(true);
};

const publishContent = async () => {
    publishDialogVisible.value = true;
};

onBeforeRouteLeave(async (to) => {
    if (!hasUnsavedChanges.value) return true;

    await ElMessageBox.confirm(
        '您有未保存的修改，是否保存草稿？',
        '离开页面',
        {
            confirmButtonText: '保存草稿',
            cancelButtonText: '放弃修改',
            distinguishCancelAndClose: true,
            beforeClose: (action, instance, done) => {
                if (action === 'confirm') {
                    submitSaveDraft(false).then(() => {
                        router.push(to);
                    });
                    done();
                } else if (action === 'cancel') {
                    hasUnsavedChanges.value = false;
                    if (userId.value) {
                        clearDraft(userId.value, articleId.value);
                    }
                    router.push(to);
                    done();
                } else {
                    done();
                }
            }
        }
    ).catch(() => false);

    return false;
});

onBeforeRouteUpdate(async (to, from) => {
    if (to.params.id !== from.params.id) {
        if (!hasUnsavedChanges.value) return true;

        await ElMessageBox.confirm(
            '您有未保存的修改，是否保存草稿？',
            '离开页面',
            {
                confirmButtonText: '保存草稿',
                cancelButtonText: '放弃修改',
                distinguishCancelAndClose: true,
                beforeClose: (action, instance, done) => {
                    if (action === 'confirm') {
                        submitSaveDraft(false).then(() => {
                            router.push(to);
                        });
                        done();
                    } else if (action === 'cancel') {
                        hasUnsavedChanges.value = false;
                        if (userId.value) {
                            clearDraft(userId.value, articleId.value);
                        }
                        router.push(to);
                        done();
                    } else {
                        done();
                    }
                }
            }
        ).catch(() => false);
        return false;
    }
    return true;
});

watch(aiRenderTick, async () => {
    if (!aiGenerating.value || !autoScrollEnabled.value) {
        return;
    }
    await scrollToBottom();
});

watch(aiGenerating, async (isGenerating) => {
    if (!isGenerating) {
        return;
    }
    autoScrollEnabled.value = true;
    await scrollToBottom('auto');
});
</script>

<style scoped>
.ai-generation-banner {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 18px;
    padding: 18px 20px;
    border-radius: 22px;
    background:
        radial-gradient(circle at top right, rgba(209, 96, 61, 0.16), transparent 30%),
        linear-gradient(135deg, rgba(255, 247, 240, 0.98), rgba(255, 255, 255, 0.96));
    border: 1px solid rgba(209, 96, 61, 0.18);
    box-shadow: 0 18px 36px rgba(19, 39, 63, 0.06);
}

.ai-generation-banner.is-stopped {
    background: linear-gradient(135deg, rgba(246, 248, 251, 0.96), rgba(255, 255, 255, 0.98));
    border-color: rgba(19, 39, 63, 0.12);
}

.ai-generation-banner__copy {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.ai-generation-banner__copy strong {
    color: #13273f;
    font-size: 16px;
    letter-spacing: -0.02em;
}

.ai-generation-banner__copy p {
    margin: 0;
    color: rgba(19, 39, 63, 0.62);
    font-size: 13px;
    line-height: 1.6;
}

.ai-generation-banner__eyebrow {
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: #d1603d;
}

.ai-generation-banner__actions {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
}

.scroll-follow-chip {
    border: none;
    background: rgba(19, 39, 63, 0.08);
    color: #13273f;
    border-radius: 999px;
    padding: 10px 14px;
    cursor: pointer;
    font-size: 12px;
    font-weight: 600;
}

.stop-generation-btn {
    border-radius: 999px;
    border: 1px solid rgba(19, 39, 63, 0.14);
    background: #fff;
    color: #13273f;
}

.summary-area {
    margin-bottom: 22px;
    padding: 16px 18px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.88);
    border: 1px solid rgba(22, 50, 79, 0.08);
    box-shadow: 0 10px 24px rgba(19, 39, 63, 0.04);
}

.summary-area.is-generating {
    border-color: rgba(209, 96, 61, 0.16);
    box-shadow: 0 12px 28px rgba(209, 96, 61, 0.08);
}

.summary-area__header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
}

.summary-area__label {
    font-size: 13px;
    font-weight: 700;
    color: #13273f;
    letter-spacing: 0.04em;
}

.summary-area__hint {
    font-size: 12px;
    color: rgba(19, 39, 63, 0.5);
}

.summary-textarea {
    width: 100%;
    min-height: 88px;
    resize: vertical;
    border: none;
    outline: none;
    background: transparent;
    font-size: 14px;
    line-height: 1.7;
    color: #13273f;
}

@media (max-width: 900px) {
    .ai-generation-banner,
    .summary-area__header {
        flex-direction: column;
    }

    .ai-generation-banner__actions {
        width: 100%;
        justify-content: flex-start;
    }
}
</style>
