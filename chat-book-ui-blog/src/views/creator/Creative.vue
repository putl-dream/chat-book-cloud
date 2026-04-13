<template>
    <div class="dashboard c-creator-page u-animate-fade-in">
        <section class="creator-agent-entry">
            <div class="creator-agent-entry__content">
                <span class="creator-agent-entry__eyebrow">MVP 已就绪</span>
                <h2 class="creator-agent-entry__title">先和 Agent 把问题聊透，再把讨论沉淀成初稿</h2>
                <p class="creator-agent-entry__text">
                    Agent Studio 负责主题讨论、知识点扩展和观点校验；正式编辑器负责承接流式初稿、继续润色、排版和发布。
                </p>
            </div>

            <div class="creator-agent-entry__actions">
                <el-button class="creator-agent-entry__button" type="primary" @click="handleOpenAgent">
                    开始思考共创
                </el-button>
                <span class="creator-agent-entry__hint">适合先讨论主题、结构和读者定位，再落成稿</span>
            </div>
        </section>

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
    handleOpenAgent,
    handleEdit,
    handleDelete
} = useCreativeLogic();

const {
    formatNumber,
    formatDate
} = useCreativeFormatter();
</script>

<style scoped>
.creator-agent-entry {
    display: grid;
    grid-template-columns: minmax(0, 1.4fr) auto;
    gap: 24px;
    align-items: center;
    margin-bottom: 24px;
    padding: 28px 30px;
    border: 1px solid rgba(255, 255, 255, 0.72);
    border-radius: 26px;
    background:
        radial-gradient(circle at top right, rgba(209, 96, 61, 0.14), transparent 32%),
        linear-gradient(135deg, rgba(255, 255, 255, 0.82), rgba(248, 250, 252, 0.86));
    box-shadow: 0 24px 42px rgba(15, 23, 42, 0.06);
}

.creator-agent-entry__eyebrow {
    display: inline-flex;
    align-items: center;
    min-height: 28px;
    padding: 0 12px;
    border-radius: 999px;
    background: rgba(22, 50, 79, 0.08);
    color: #16324f;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.12em;
    text-transform: uppercase;
}

.creator-agent-entry__title {
    margin: 14px 0 10px;
    color: #13273f;
    font-size: 28px;
    line-height: 1.15;
    letter-spacing: -0.04em;
}

.creator-agent-entry__text,
.creator-agent-entry__hint {
    margin: 0;
    color: rgba(19, 39, 63, 0.7);
    font-size: 14px;
    line-height: 1.8;
}

.creator-agent-entry__actions {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
}

.creator-agent-entry__button {
    min-width: 156px;
    border: none;
    border-radius: 999px;
    background: linear-gradient(135deg, #d1603d, #c04e2b);
    box-shadow: 0 14px 28px rgba(209, 96, 61, 0.22);
    font-weight: 600;
}

@media (max-width: 900px) {
    .creator-agent-entry {
        grid-template-columns: 1fr;
        padding: 24px;
    }

    .creator-agent-entry__actions {
        align-items: stretch;
    }
}
</style>
