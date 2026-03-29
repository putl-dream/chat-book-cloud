<template>
    <div class="draft-box c-creator-page c-creator-page--rounded u-animate-fade-in">
        <div class="drafts-container c-creator-scroll custom-scrollbar">
            <div class="c-section-heading c-section-heading--decorated">
                <h2 class="c-section-heading__title">草稿箱</h2>
                <div class="c-section-heading__decoration"></div>
            </div>

            <div class="draft-list c-creator-list">
                <div v-for="(draft, index) in drafts" :key="index" class="draft-card c-creator-card u-animate-list-in"
                    :style="{ animationDelay: `${index * 0.1}s` }">
                    <div class="draft-main c-creator-card__main">
                        <h3 class="draft-title c-creator-card__title">{{ draft.title || '无标题' }}</h3>
                        <p class="draft-summary c-creator-card__summary">{{ draft.abstractText || '暂无摘要' }}</p>
                        <div class="draft-meta c-creator-card__meta">
                            <span class="date c-creator-card__date">最后保存: {{ formatDate(draft.updateTime) }}</span>
                        </div>
                    </div>

                    <div class="card-actions c-creator-card__actions">
                        <el-button type="primary" @click="handleEdit(draft)">编辑</el-button>
                        <el-button type="danger" @click="handleDelete(draft)">删除</el-button>
                    </div>
                </div>
            </div>

            <div class="pagination-wrapper" v-if="totalDrafts > 0">
                <el-pagination background layout="prev, pager, next" :total="totalDrafts" :page-size="pageSize"
                    v-model:current-page="currentPage" @current-change="handlePageChange" />
            </div>

            <el-empty v-else description="暂无草稿" class="empty-state c-creator-empty"></el-empty>
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
