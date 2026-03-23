<template>
    <div class="draft-box animate-fade-in">
        <div class="drafts-container">
            <div class="section-header">
                <h2 class="section-title">草稿箱</h2>
                <div class="section-decoration"></div>
            </div>

            <div class="draft-list">
                <div v-for="(draft, index) in drafts" :key="index" class="draft-card"
                    :style="{ animationDelay: `${index * 0.1}s` }">
                    <div class="draft-main">
                        <h3 class="draft-title">{{ draft.title || '无标题' }}</h3>
                        <p class="draft-summary">{{ draft.abstractText || '暂无摘要' }}</p>
                        <div class="draft-meta">
                            <span class="date">最后保存: {{ formatDate(draft.updateTime) }}</span>
                        </div>
                    </div>

                    <div class="card-actions">
                        <el-button type="primary" @click="handleEdit(draft)">编辑</el-button>
                        <el-button type="danger" @click="handleDelete(draft)">删除</el-button>
                    </div>
                </div>
            </div>

            <div class="pagination-wrapper" v-if="totalDrafts > 0">
                <el-pagination background layout="prev, pager, next" :total="totalDrafts" :page-size="pageSize"
                    v-model:current-page="currentPage" @current-change="handlePageChange" />
            </div>

            <el-empty v-else description="暂无草稿" class="empty-state"></el-empty>
        </div>
    </div>
</template>

<script setup>
import { useDraftLogic, useDraftFormatter } from './_hooks/useDraftLogic.js';

const {
    drafts,
    totalDrafts,
    pageSize,
    currentPage,
    handlePageChange,
    handleEdit,
    handleDelete
} = useDraftLogic();

const {
    formatDate
} = useDraftFormatter();
</script>

<style scoped>
.draft-box {
    padding: 24px;
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
    border-radius: 16px;
}

.drafts-container {
    max-width: 1000px;
    width: 100%;
    margin: 0 auto;
    position: relative;
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding-right: 8px;
}

.drafts-container::-webkit-scrollbar {
    width: 6px;
}

.drafts-container::-webkit-scrollbar-track {
    background: transparent;
}

.drafts-container::-webkit-scrollbar-thumb {
    background: rgba(156, 163, 175, 0.5);
    border-radius: 3px;
}

.drafts-container::-webkit-scrollbar-thumb:hover {
    background: rgba(156, 163, 175, 0.8);
}

.animate-fade-in {
    animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.section-header {
    display: flex;
    align-items: center;
    margin-bottom: 24px;
    position: relative;
    padding-top: 8px;
}

.section-title {
    font-size: 24px;
    font-weight: 700;
    color: #1f2937;
    margin: 0;
    z-index: 1;
}

.section-decoration {
    position: absolute;
    bottom: -4px;
    left: 0;
    width: 60px;
    height: 8px;
    background: rgba(59, 130, 246, 0.2);
    border-radius: 4px;
    z-index: 0;
}

.draft-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.draft-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    animation: slideUp 0.5s ease-out forwards;
    opacity: 0;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.draft-card:hover {
    transform: translateY(-2px);
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.draft-main {
    flex: 1;
    margin-right: 32px;
    min-width: 0;
}

.draft-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 8px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: color 0.2s;
    cursor: pointer;
}

.draft-title:hover {
    color: #3b82f6;
}

.draft-summary {
    color: #6b7280;
    font-size: 14px;
    line-height: 1.6;
    margin: 0 0 12px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.draft-meta {
    font-size: 12px;
    color: #9ca3af;
    display: flex;
    align-items: center;
}

.date {
    background: rgba(0, 0, 0, 0.05);
    padding: 2px 8px;
    border-radius: 4px;
}

.card-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding-left: 24px;
}

.card-actions .el-button {
    margin: 0;
}

.pagination-wrapper {
    margin-top: 32px;
    display: flex;
    justify-content: center;
    margin-bottom: 32px;
}

.empty-state {
    padding: 60px 0;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 16px;
    backdrop-filter: blur(10px);
}

@media (max-width: 1024px) {
    .draft-card {
        flex-direction: column;
        align-items: flex-start;
        gap: 20px;
    }

    .draft-main {
        margin-right: 0;
        width: 100%;
    }

    .card-actions {
        flex-direction: row;
        width: 100%;
        justify-content: flex-end;
        padding-left: 0;
    }
}
</style>
