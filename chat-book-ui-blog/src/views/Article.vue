<template>
    <div class="article-detail">
        <div class="article-buttons">
            <div>
                <el-button class="comment-button" size="large" type="warning" :icon="Pointer" circle
                    :style="{ backgroundColor: praiseStat === 0 ? 'white' : 'orange' }" @click="handleLike" />
            </div>
            <div>
                <el-button class="comment-button" type="warning" :icon="ChatLineRound" circle @click="handleComment" />
            </div>
            <div>
                <el-button class="comment-button" size="large" type="warning" :icon="Star" circle
                    :style="{ backgroundColor: collectStat === 0 ? 'white' : 'orange' }" @click="handleFavorite" />
            </div>
        </div>

        <div class="content">
            <header class="article-header">
                <h1>{{ article.title }}</h1>
                <div class="article-meta">
                    <span class="gap">作者: {{ article.author }}</span>
                    <span class="gap">发布日期: {{ article.createTime }}</span>
                    <span class="gap">阅读量: {{ article.viewCount }}</span>
                </div>
            </header>
            <main class="article-content">
                <p v-html="article.content"></p>
            </main>
        </div>

        <div class="article-right">
            <div class="article-right-card">
                <AuthorCard :userId="article.authorId" />
            </div>
            <div class="article-right-card">
                <HotCard />
            </div>
        </div>

        <!-- 抽屉组件 -->
        <el-drawer title="评论" v-model="drawerVisible" :direction="direction" size="30%" class="article-drawer"
            :with-header="false">
            <div class="article-header" style="display:flex;justify-content: space-between;margin-bottom: 10px">
                <el-text size="large"><b>评论</b></el-text>
                <div>
                    <el-button type="primary" @click="addComment">发布</el-button>
                </div>
            </div>
            <div class="comment-section">
                <!-- 评论框 -->
                <div class="comment-avatar">
                    <el-avatar :size="40" :src="selfPhoto" />
                </div>
                <el-input v-model="newComment" type="textarea" :rows="6" placeholder="请输入评论" class="comment-input" />
            </div>
            <div class="comments-list">
                <div v-for="(comment, index) in comments" :key="index" class="comment-item">
                    <div class="comment-avatar">
                        <el-avatar :size="40" :src="comment.headerImg" />
                    </div>
                    <div class="comment-details">
                        <div class="comment-header">
                            <span class="comment-username">{{ comment.username }}</span>
                            <span class="comment-time">{{ comment.createTime }}</span>
                        </div>
                        <div class="comment-message">
                            <p>{{ comment.content }}</p>
                            <div class="comment-actions">
                                <el-button type="text" @click="toggleComment(comment)"
                                    v-if="comment.children.length > 0">
                                    {{ comment.expanded ? '收起' : '展开' }}
                                </el-button>
                                <el-button type="text" @click="replyToComment(comment)">回复</el-button>
                            </div>
                        </div>

                        <div v-if="comment.expanded" class="sub-comments">
                            <div v-for="(subComment, subIndex) in comment.children" :key="subIndex"
                                class="sub-comment-item">
                                <div class="comment-avatar">
                                    <el-avatar :size="40" :src="subComment.headerImg" />
                                </div>
                                <div class="comment-details">
                                    <div class="comment-header">
                                        <span class="comment-username">{{ subComment.username }}</span>
                                        <span class="comment-time">{{ subComment.createTime }}</span>
                                    </div>
                                    <div class="comment-message">
                                        <p>{{ subComment.content }}</p>
                                        <div class="comment-actions">
                                            <el-button type="text" @click="replyToComment(subComment)">回复</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </el-drawer>
    </div>
</template>
<script setup>
import { nextTick, onMounted, ref } from 'vue';
import { ChatLineRound, Pointer, Star } from '@element-plus/icons-vue';
import HotCard from "@/components/widget/HotCard.vue";
import { useRoute } from "vue-router";
import { getByArticleId, queryArticle } from "@/api/article.js";
import AuthorCard from "@/components/widget/AuthorCard.vue";
import { saveReview, updateCollection, updatePraise } from "@/api/user.js";
import { ElDrawer, ElAvatar, ElInput, ElButton, ElMessage } from 'element-plus';
import { checkLogin } from "@/utils/index.js";

// 获取路由参数
const route = useRoute();
const articleId = route.params.id;

const selfPhoto = localStorage.getItem('avatar')
const article = ref({});
const praiseStat = ref(0);
const collectStat = ref(0);
const drawerVisible = ref(false);
const direction = ref('rtl'); // 抽屉方向
const newComment = ref('');
const parentId = ref(0); // 评论的父ID

// 模拟评论数据
const comments = ref([]);

const handleLike = async () => {
    if (!checkLogin()) return;
    const res = await updateCollection(articleId);
    console.log("点赞", res);
    praiseStat.value = res;
};

const handleComment = async () => {
    if (!checkLogin()) return;
    console.log('评论' + articleId);
    drawerVisible.value = true; // 打开抽屉
    await nextTick(() => {
        const messageList = document.querySelector('.article-content');
        messageList.scrollTop = messageList.scrollHeight;
    });
};

const handleFavorite = async () => {
    if (!checkLogin()) return;
    const res = await updatePraise(articleId);
    console.log('收藏', res);
    if (res === 0) {
        ElMessage.warning('取消收藏');
    } else {
        ElMessage.success('收藏成功');
    }
    collectStat.value = res;
};

const queryArticleRequest = async () => {
    const res = await queryArticle(articleId);
    if (res) {
        article.value = res;
        praiseStat.value = article.value.praiseStat;
        collectStat.value = article.value.collectStat;
        console.log("article->", article.value);
    }
};

const queryCommentRequest = async () => {
    const res = await getByArticleId(articleId);
    if (res) {
        comments.value = res.map(comment => ({
            ...comment,
            expanded: true
        }));
        console.log("构建评论", comments.value);
    }
};

const addComment = async () => {
    if (!checkLogin()) return;
    if (newComment.value.trim() === '') {
        return;
    }
    const res = await saveReview({
        articleId: articleId,
        parentId: parentId.value,
        content: newComment.value,
    });
    console.log("评论结果", res)
    newComment.value = '';
    parentId.value = 0;
};
const toggleComment = (comment) => {
    comment.expanded = !comment.expanded;
};

const replyToComment = (comment) => {
    newComment.value = `回复 ${comment.username}: `;
    parentId.value = comment.id;
};

const findCommentById = (comments, id) => {
    for (const comment of comments) {
        if (comment.id === id) {
            return comment;
        }
        if (comment.children.length > 0) {
            const found = findCommentById(comment.children, id);
            if (found) {
                return found;
            }
        }
    }
    return null;
};


onMounted(() => {
    queryArticleRequest();
    queryCommentRequest();
});
</script>
<style scoped>
.article-detail {
    display: flex;
    max-width: var(--container-width-lg);
    margin: 0 auto;
    gap: 32px;
    padding: 32px var(--container-padding);
    position: relative;
}

.content {
    flex: 1;
    min-width: 0;
    max-width: 840px;
    padding: 40px;
    background: var(--bg-color-white);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
}

.article-header {
    border-bottom: 1px solid var(--border-color-light);
    padding-bottom: 24px;
    margin-bottom: 32px;
}

.article-header h1 {
    font-size: 2.25rem;
    font-weight: 800;
    color: var(--text-color-primary);
    line-height: 1.3;
    margin-bottom: 16px;
}

.article-meta {
    display: flex;
    gap: 24px;
    font-size: 0.875rem;
    color: var(--text-color-secondary);
}

.article-content {
    font-size: 1.0625rem;
    line-height: 1.8;
    color: var(--text-color-regular);
}

.article-buttons {
    position: sticky;
    top: calc(var(--header-height) + 40px);
    display: flex;
    flex-direction: column;
    gap: 16px;
    height: fit-content;
}

.comment-button {
    width: 48px;
    height: 48px;
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
    transition: var(--transition-base);
}

.comment-button:hover {
    transform: scale(1.1);
    box-shadow: var(--box-shadow-hover);
}

.article-right {
    width: 320px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 24px;
    position: sticky;
    top: calc(var(--header-height) + 32px);
    height: fit-content;
}

.article-right-card {
    background: var(--bg-color-white);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
    overflow: hidden;
}

.comment-section {
    display: flex;
    gap: 16px;
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--border-color-light);
}

.comments-list {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.comment-item {
    display: flex;
    gap: 16px;
}

.sub-comments {
    margin-top: 16px;
    padding-left: 16px;
    border-left: 2px solid var(--border-color-light);
    display: flex;
    flex-direction: column;
    gap: 16px;
}
</style>
