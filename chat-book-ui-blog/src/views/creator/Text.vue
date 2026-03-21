<template>
    <div class="editor-layout">
        <CreativeHeader class="site-header" />

        <div class="text-toolbar">
            <div class="toolbar-wrapper">
                <TiptapToolbar :editor="editor" class="glass-toolbar" v-if="editor" @toggle-toc="toggleLeft"
                    :tocVisible="layoutState.leftOpen" />
                <div class="status-bar">
                    <div class="status-indicator" :class="{ 'saving': saveState === 'saving' }"></div>
                    <el-text class="status-text">{{ statusText }}</el-text>
                </div>
                <el-button @click="toggleRight" :disabled="layoutState.isMobile" size="small"
                    class="sidebar-toggle-btn">
                    {{ layoutState.rightOpen ? '关闭侧边' : '打开侧边' }}
                </el-button>
            </div>
        </div>

        <div class="editor-container" ref="containerRef" @mousemove="handleMouseMove" @mouseup="onMouseUp"
            @mouseleave="onMouseUp">
            <!-- Left Column -->
            <div class="layout-left" :class="{ 'is-dragging': dragging === 'left' }" v-show="layoutState.leftOpen"
                :style="{ width: layoutState.leftWidth + '%' }">
                <ArticleToc v-if="editor" :editor="editor" />
            </div>

            <!-- Left Splitter -->
            <div class="layout-splitter" v-show="layoutState.leftOpen" @mousedown.prevent="startDrag('left')"></div>

            <!-- Content Column -->
            <div class="layout-content" :class="{ 'is-dragging': dragging }" :style="{ width: contentWidth + '%' }">
                <div class="scroll-area">
                    <div class="main-content">
                        <!-- 标题区域 -->
                        <div class="title-area">
                            <input type="text" v-model="title" placeholder="请输入文章标题" class="title-input"
                                @input="onInput" />
                        </div>

                        <!-- 内容区域 -->
                        <RichTextEditor
                            :editor="editor"
                            class="main-content-editor"
                            placeholder="请输入内容..."
                            variant="editor" />
                    </div>
                </div>
            </div>

            <!-- Right Splitter -->
            <div class="layout-splitter" v-show="layoutState.rightOpen" @mousedown.prevent="startDrag('right')"></div>

            <!-- Right Column -->
            <div class="layout-right" :class="{ 'is-dragging': dragging === 'right' }" v-show="layoutState.rightOpen"
                :style="{ width: layoutState.rightWidth + '%' }">
                <EditorMetaPanel :publish-form="publishForm" :tech-tags="techTags" :path-tags="pathTags"
                    :selected-tech-tags="selectedTechTags" :selected-path-tag="selectedPathTag"
                    @change-tech-tags="handleTechTagsChange" @change-path-tag="handlePathTagChange"
                    @close="toggleRight" />
            </div>
        </div>

        <EditorFooterActions :word-count="wordCount" @save="saveContent" @publish="publishContent" />
        <PublishDialog v-model="publishDialogVisible" :publish-form="publishForm" :category-options="categoryOptions"
            :handle-cover-upload="handleCoverUpload" :before-cover-upload="beforeCoverUpload"
            @confirm="confirmPublish" />
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRouter } from 'vue-router';
import { useEditorLogic } from './_hooks/useEditorLogic.js';
import { CATEGORY_NAMES } from '@/constants';
import CreativeHeader from "@/views/creator/components/CreativeHeader.vue";
import TiptapToolbar from "@/views/creator/components/TiptapToolbar.vue";
import EditorMetaPanel from "@/views/creator/components/EditorMetaPanel.vue";
import EditorFooterActions from "@/views/creator/components/EditorFooterActions.vue";
import PublishDialog from "@/views/creator/components/PublishDialog.vue";
import ArticleToc from "@/views/article/components/ArticleToc.vue";
import RichTextEditor from "@/components/common/rich-text/RichTextEditor.vue";
import { ElMessageBox } from 'element-plus';

const containerRef = ref(null);

const {
    title,
    wordCount,
    publishDialogVisible,
    publishForm,
    layoutState,
    dragging,
    techTags,
    pathTags,
    selectedTechTags,
    selectedPathTag,
    saveState,
    statusText,
    contentWidth,
    editor,
    updateTagIds,
    handleCoverUpload,
    beforeCoverUpload,
    toggleLeft,
    toggleRight,
    startDrag,
    onMouseMove,
    onMouseUp,
    queueSaveFlow,
    submitSaveDraft,
    confirmPublish,
    hasUnsavedChanges,
    userId,
    articleId,
    clearDraft
} = useEditorLogic();

const handleMouseMove = (e) => {
    onMouseMove(e, containerRef.value);
};

const categoryOptions = CATEGORY_NAMES;

const onInput = () => {
    queueSaveFlow();
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

const router = useRouter();

onBeforeRouteLeave(async (to) => {
    if (!hasUnsavedChanges.value) return true;

    const action = await ElMessageBox.confirm(
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

        const action = await ElMessageBox.confirm(
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
</script>

<style scoped src="./styles/text-layout.css"></style>
