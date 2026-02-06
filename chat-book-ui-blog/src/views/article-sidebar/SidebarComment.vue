<template>
    <div class="panel-content comment-panel">
        <div class="panel-header">
            <el-text size="large"><b>评论</b></el-text>
            <div>
                <el-button type="primary" size="small" @click="addComment">发布</el-button>
            </div>
        </div>
        <div class="comment-section">
            <div class="comment-avatar">
                <el-avatar :size="40" :src="selfPhoto" />
            </div>
            <el-input v-model="newComment" type="textarea" :rows="3" placeholder="请输入评论" class="comment-input" />
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
                            <el-button type="text" @click="toggleComment(comment)" v-if="comment.children.length > 0">
                                {{ comment.expanded ? '收起' : '展开' }}
                            </el-button>
                            <el-button type="text" @click="replyToComment(comment)">回复</el-button>
                        </div>
                    </div>

                    <div v-if="comment.expanded" class="sub-comments">
                        <div v-for="(subComment, subIndex) in comment.children" :key="subIndex" class="sub-comment-item">
                            <div class="comment-avatar">
                                <el-avatar :size="32" :src="subComment.headerImg" />
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
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElAvatar, ElInput, ElButton, ElText } from 'element-plus';
import { getByArticleId } from "@/api/article.js";
import { saveReview } from "@/api/user.js";
import { checkLogin } from "@/utils/index.js";

const props = defineProps({
    articleId: {
        type: [Number, String],
        required: true
    }
});

const selfPhoto = localStorage.getItem('avatar');
const newComment = ref('');
const parentId = ref(0);
const comments = ref([]);

const queryCommentRequest = async () => {
    const res = await getByArticleId(props.articleId);
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
        articleId: props.articleId,
        parentId: parentId.value,
        content: newComment.value,
    });
    console.log("评论结果", res)
    newComment.value = '';
    parentId.value = 0;
    // Refresh comments
    queryCommentRequest();
};

const toggleComment = (comment) => {
    comment.expanded = !comment.expanded;
};

const replyToComment = (comment) => {
    newComment.value = `回复 ${comment.username}: `;
    parentId.value = comment.id;
};

onMounted(() => {
    queryCommentRequest();
});
</script>

<style scoped>
.panel-content {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
}

.panel-header {
    padding: 16px;
    border-bottom: 1px solid var(--border-color-light);
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    flex-shrink: 0;
}

.comment-panel .comments-list {
    overflow-y: auto;
    padding: 16px;
    flex: 1;
}

.comment-panel .comment-section {
    padding: 16px;
    border-bottom: 1px solid var(--border-color-light);
    margin-bottom: 0;
    flex-shrink: 0;
}

.comment-section {
    display: flex;
    gap: 16px;
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

.comment-details {
    flex: 1;
}

.comment-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
}

.comment-username {
    font-weight: bold;
    color: var(--text-color-primary);
}

.comment-time {
    font-size: 0.8rem;
    color: var(--text-color-secondary);
}

.comment-message p {
    margin-bottom: 8px;
    line-height: 1.6;
}

.comment-actions {
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

.sub-comment-item {
    display: flex;
    gap: 16px;
}
</style>
