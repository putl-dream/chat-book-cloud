<template>
    <div class="dashboard c-creator-page u-animate-fade-in">
        <!-- 数据展示卡片 (Reserved) -->
        <!-- <div class="data-cards">
            <UserDataCard/>
        </div> -->

        <!-- 文章列表卡片 -->
        <div class="articles-container c-creator-scroll custom-scrollbar">
            <div class="c-section-heading c-section-heading--decorated">
                <h2 class="c-section-heading__title">近期文章</h2>
                <div class="c-section-heading__decoration"></div>
            </div>

            <div class="article-list c-creator-list">
                <div v-for="(article, index) in articles" :key="index" class="article-card c-creator-card u-animate-list-in"
                    :style="{ animationDelay: `${index * 0.1}s` }">
                    <div class="article-main c-creator-card__main">
                        <h3 class="article-title c-creator-card__title">{{ article.title }}</h3>
                        <p class="article-summary c-creator-card__summary">{{ article.abstractText }}</p>
                        <div class="article-meta c-creator-card__meta">
                            <span class="date c-creator-card__date">{{ formatDate(article.createTime) }}</span>
                        </div>
                    </div>

                    <div class="article-stats c-creator-card__stats">
                        <div class="stat-item c-creator-card__stat" title="阅读量">
                            <el-icon class="stat-icon c-creator-card__stat-icon">
                                <View />
                            </el-icon>
                            <span class="stat-value c-creator-card__stat-value">{{ formatNumber(article.viewCount) }}</span>
                            <span class="stat-label c-creator-card__stat-label">阅读</span>
                        </div>
                        <div class="stat-item c-creator-card__stat" title="评论量">
                            <el-icon class="stat-icon c-creator-card__stat-icon">
                                <ChatDotSquare />
                            </el-icon>
                            <span class="stat-value c-creator-card__stat-value">{{ formatNumber(article.commentCount) }}</span>
                            <span class="stat-label c-creator-card__stat-label">评论</span>
                        </div>
                        <div class="stat-item c-creator-card__stat" title="点赞数">
                            <el-icon class="stat-icon c-creator-card__stat-icon">
                                <Star />
                            </el-icon>
                            <span class="stat-value c-creator-card__stat-value">{{ formatNumber(article.praiseCount) }}</span>
                            <span class="stat-label c-creator-card__stat-label">点赞</span>
                        </div>
                        <div class="stat-item c-creator-card__stat" title="收藏数">
                            <el-icon class="stat-icon c-creator-card__stat-icon">
                                <Collection />
                            </el-icon>
                            <span class="stat-value c-creator-card__stat-value">{{ formatNumber(article.collectCount) }}</span>
                            <span class="stat-label c-creator-card__stat-label">收藏</span>
                        </div>
                    </div>

                    <div class="card-actions c-creator-card__actions">
                        <el-button link type="primary" @click="handleEdit(article)">编辑</el-button>
                        <el-button link type="danger" @click="handleDelete(article)">删除</el-button>
                    </div>
                </div>
            </div>

            <div class="pagination-wrapper" v-if="totalArticles > 0">
                <el-pagination background layout="prev, pager, next" :total="totalArticles" :page-size="pageSize"
                    v-model:current-page="currentPage" @current-change="handlePageChange" />
            </div>

            <el-empty v-else description="暂无文章，快去创作吧" class="empty-state c-creator-empty"></el-empty>
        </div>
    </div>
</template>

<script setup>
import { useCreativeLogic, useCreativeFormatter } from './_hooks/useCreativeLogic.js';
import { View, ChatDotSquare, Star, Collection } from '@element-plus/icons-vue';

const {
    articles,
    totalArticles,
    pageSize,
    currentPage,
    handlePageChange,
    handleEdit,
    handleDelete
} = useCreativeLogic();

const {
    formatNumber,
    formatDate
} = useCreativeFormatter();
</script>
