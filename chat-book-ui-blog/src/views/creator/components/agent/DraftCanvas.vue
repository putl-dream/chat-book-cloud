<template>
    <div class="draft-canvas">
        <!-- Status: Empty -->
        <div class="canvas-state empty-state u-animate-fade-in" v-if="store.draftStatus === 'empty'">
            <div class="empty-content">
                <div class="empty-icon-wrapper">
                    <el-icon class="empty-icon"><EditPen /></el-icon>
                </div>
                <h2 class="empty-title">嗨！今天想写点什么？</h2>
                <p class="empty-subtitle">可以从右侧对话框输入想法，或者尝试下方快捷指令：</p>
                <div class="empty-chips">
                    <span 
                        class="empty-chip" 
                        v-for="chip in starterChips" 
                        :key="chip"
                        @click="handleChipClick(chip)"
                    >
                        {{ chip }}
                    </span>
                </div>
            </div>
        </div>

        <!-- Status: Generating or Optimizing -->
        <div class="canvas-state generating-state u-animate-fade-in" v-else-if="store.draftStatus === 'generating' || store.draftStatus === 'optimizing'">
            <div class="skeleton-paper">
                <div class="skeleton-header">
                    <div class="breath-indicator">
                        <span class="breath-dot"></span>
                        <span class="breath-text">{{ store.draftStatus === 'generating' ? '正在构建文章框架...' : '正在进行局部优化重写...' }}</span>
                    </div>
                </div>
                <div class="skeleton-body">
                    <el-skeleton :rows="14" animated />
                </div>
            </div>
        </div>

        <!-- Status: Completed (Draft View) -->
        <div class="canvas-state completed-state custom-scrollbar u-animate-fade-in" v-else-if="store.displayDraft">
            <div class="a4-paper">
                <!-- Meta Toolbar floating on top of paper if there is a pending candidate -->
                <div class="floating-toolbar" v-if="store.candidateDraft">
                    <div class="floating-toolbar-info">
                        <el-icon><RefreshRight /></el-icon>
                        <span>预览优化版本 V{{ store.candidateDraft.versionNo }}</span>
                    </div>
                    <div class="floating-toolbar-actions">
                        <el-button size="small" @click="store.rejectCandidateVersion">撤销</el-button>
                        <el-button type="primary" size="small" @click="store.adoptCandidateVersion">确认应用</el-button>
                    </div>
                </div>

                <div class="draft-content">
                    <h1 class="draft-title">{{ store.displayDraft.title || '未命名草稿' }}</h1>
                    <div class="draft-summary" v-if="store.displayDraft.summary">
                        {{ store.displayDraft.summary }}
                    </div>
                    <RichTextViewer
                        class="draft-viewer"
                        :html="renderHtml(store.displayDraft.content)"
                        variant="article" 
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useAgentStudioStore } from '@/store/agentStudio.js';
import { buildRichTextHtml } from '@/components/common/rich-text/content-pipeline.js';
import RichTextViewer from '@/components/common/rich-text/RichTextViewer.vue';
import { EditPen, RefreshRight } from '@element-plus/icons-vue';

const store = useAgentStudioStore();

const starterChips = [
    "帮我规划一篇小红书旅行攻略，关于京都的",
    "撰写一篇 SpringBoot3 新特性的技术播报",
    "写一份年度项目总结PPT的演讲稿大纲",
    "用通俗易懂的语言解释什么是大模型"
];

const handleChipClick = (msg) => {
    // Only send if not currently communicating
    if (store.chatting || store.loadingSession || store.generatingDraft) return;
    store.sendMessage(msg);
};

const renderHtml = (content) => buildRichTextHtml(content || '', 'markdown');
</script>

<style scoped>
.draft-canvas {
    flex: 1;
    height: 100%;
    background: #F9FAFB;
    position: relative;
    overflow: hidden;
}

.canvas-state {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    position: absolute;
    top: 0;
    left: 0;
}

.completed-state {
    align-items: flex-start;
    overflow-y: auto;
    padding: 40px 0;
}

/* Empty State */
.empty-content {
    text-align: center;
    max-width: 500px;
}

.empty-icon-wrapper {
    width: 64px;
    height: 64px;
    border-radius: 20px;
    background: rgba(209, 96, 61, 0.1);
    color: #d1603d;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    margin: 0 auto 24px;
}

.empty-title {
    font-size: 24px;
    color: #13273f;
    margin: 0 0 12px;
    letter-spacing: -0.02em;
}

.empty-subtitle {
    font-size: 15px;
    color: rgba(19, 39, 63, 0.6);
    margin: 0 0 32px;
}

.empty-chips {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
}

.empty-chip {
    padding: 10px 18px;
    background: #fff;
    border: 1px solid rgba(22, 50, 79, 0.1);
    border-radius: 999px;
    font-size: 13px;
    color: rgba(19, 39, 63, 0.7);
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 6px rgba(21, 37, 64, 0.02);
}

.empty-chip:hover {
    color: #d1603d;
    border-color: rgba(209, 96, 61, 0.3);
    background: rgba(209, 96, 61, 0.02);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(209, 96, 61, 0.08);
}

/* Skeleton & A4 Paper */
.skeleton-paper,
.a4-paper {
    width: 100%;
    max-width: 800px;
    min-height: 1000px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 12px 32px rgba(21, 37, 64, 0.06), 0 2px 8px rgba(21, 37, 64, 0.04);
    border: 1px solid rgba(22, 50, 79, 0.04);
    margin: 0 auto;
    position: relative;
}

.skeleton-paper {
    padding: 60px 80px;
    display: flex;
    flex-direction: column;
    gap: 40px;
}

.breath-indicator {
    display: inline-flex;
    align-items: center;
    gap: 12px;
    padding: 10px 20px;
    background: rgba(22, 50, 79, 0.04);
    border-radius: 999px;
    margin-bottom: 20px;
}

.breath-dot {
    width: 10px;
    height: 10px;
    background: #d1603d;
    border-radius: 50%;
    animation: breath 1.5s infinite ease-in-out;
}

@keyframes breath {
    0%, 100% { opacity: 0.4; transform: scale(0.8); }
    50% { opacity: 1; transform: scale(1.2); }
}

.breath-text {
    font-size: 14px;
    font-weight: 500;
    color: #13273f;
    letter-spacing: 1px;
}

.a4-paper {
    padding: 80px 100px;
    min-height: auto;
    height: auto;
    margin-bottom: 80px;
}

.floating-toolbar {
    position: absolute;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 8px 12px 8px 20px;
    background: rgba(22, 50, 79, 0.9);
    border-radius: 999px;
    box-shadow: 0 8px 24px rgba(22, 50, 79, 0.2);
    z-index: 100;
    color: #fff;
}

.floating-toolbar-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    font-weight: 500;
}

.floating-toolbar-actions {
    display: flex;
    gap: 8px;
}

.draft-title {
    font-size: 32px;
    font-weight: 700;
    line-height: 1.3;
    color: #13273f;
    margin: 0 0 24px;
}

.draft-summary {
    padding: 16px 20px;
    background: rgba(22, 50, 79, 0.03);
    border-left: 4px solid rgba(22, 50, 79, 0.1);
    border-radius: 0 8px 8px 0;
    font-size: 15px;
    color: rgba(19, 39, 63, 0.7);
    line-height: 1.7;
    margin-bottom: 40px;
}

.draft-viewer {
    font-size: 16px;
    line-height: 1.8;
    color: #334155;
}

@media (max-width: 1100px) {
    .a4-paper, .skeleton-paper {
        margin: 0 20px 40px;
        padding: 40px 40px;
    }
}
@media (max-width: 768px) {
    .a4-paper, .skeleton-paper {
        margin: 0 10px 20px;
        padding: 30px 20px;
    }
}
</style>
