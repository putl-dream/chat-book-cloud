<template>
    <div class="panel-content b-comment-panel">
        <div class="panel-header">
            <span class="title">评论 <span class="count" v-if="totalComments > 0">({{ totalComments }})</span></span>
        </div>

        <div class="input-section">
            <el-input v-model="mainCommentContent" type="textarea" :rows="3"
                placeholder="请输入您的评论... (Enter 发送，Shift+Enter 换行)" class="b-textarea" resize="none"
                @keydown="handleKeydown($event, true)" />
            <div class="input-actions">
                <el-button type="primary" size="small" :loading="submitting" @click="handleMainSubmit"
                    :disabled="!mainCommentContent.trim()">发布评论</el-button>
            </div>
        </div>

        <div class="comments-container">
            <div class="comments-list" v-if="comments.length > 0">
                <div v-for="(comment, index) in comments" :key="comment.id || index" class="comment-item">
                    <div class="comment-main">
                        <div class="avatar-col">
                            <el-avatar :size="32" :src="comment.headerImg || defaultAvatar" shape="square" />
                        </div>
                        <div class="content-col">
                            <div class="info-row">
                                <span class="username">{{ comment.username }}</span>
                                <span class="time">{{ formatRelativeTime(comment.createTime) }}</span>
                            </div>
                            <div class="text-row">
                                {{ comment.content }}
                            </div>
                            <div class="action-row">
                                <el-link :underline="false" type="info" @click="toggleReply(comment.id)">
                                    {{ activeReplyId === comment.id ? '取消回复' : '回复' }}
                                </el-link>
                                <el-divider direction="vertical"
                                    v-if="comment.children && comment.children.length > 0" />
                                <el-link v-if="comment.children && comment.children.length > 0" :underline="false"
                                    type="primary" @click="toggleExpand(comment)">
                                    {{ comment.expanded ? '收起' : `查看回复 (${comment.children.length})` }}
                                </el-link>
                            </div>

                            <!-- Inline Reply Box -->
                            <div v-if="activeReplyId === comment.id" class="inline-reply-box">
                                <el-input v-model="replyContent" size="small"
                                    :placeholder="`回复 ${comment.username}... (Enter 发送)`" ref="replyInputRef"
                                    @keydown="handleKeydown($event)">
                                    <template #append>
                                        <el-button :loading="submitting" @click="submitReply">发送</el-button>
                                    </template>
                                </el-input>
                            </div>

                            <!-- Sub Comments -->
                            <div v-if="comment.expanded && comment.children && comment.children.length > 0"
                                class="sub-comments-wrapper">
                                <div v-for="(subComment, subIndex) in comment.children" :key="subComment.id || subIndex"
                                    class="sub-comment-item">
                                    <span class="sub-username">{{ subComment.username }}</span>
                                    <span v-if="subComment.replyToUser" class="reply-target"> 回复 {{
                                        subComment.replyToUser }}</span>
                                    <span class="sub-content">：{{ subComment.content }}</span>
                                    <div class="sub-meta">
                                        <span class="sub-time">{{ formatRelativeTime(subComment.createTime) }}</span>
                                        <el-link :underline="false" type="info" class="sub-action"
                                            @click="toggleReply(subComment.id)">
                                            {{ activeReplyId === subComment.id ? '取消' : '回复' }}
                                        </el-link>
                                    </div>

                                    <!-- Sub Comment Inline Reply -->
                                    <div v-if="activeReplyId === subComment.id" class="inline-reply-box sub-reply-box">
                                        <el-input v-model="replyContent" size="small"
                                            :placeholder="`回复 ${subComment.username}... (Enter 发送)`"
                                            @keydown="handleKeydown($event)">
                                            <template #append>
                                                <el-button :loading="submitting" @click="submitReply">发送</el-button>
                                            </template>
                                        </el-input>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div v-else class="empty-state">
                <span class="empty-text">暂无评论</span>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElAvatar, ElInput, ElButton, ElEmpty, ElMessage, ElLink, ElDivider } from 'element-plus';
import { getByArticleId } from "@/api/article.js";
import { saveReview } from "@/api/user.js";
import { checkLogin } from "@/utils/index.js";

const props = defineProps({
    articleId: {
        type: [Number, String],
        required: true
    }
});

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
const selfPhoto = localStorage.getItem('avatar');

// State
const mainCommentContent = ref('');
const replyContent = ref('');
const activeReplyId = ref(null); // ID of the comment/subcomment being replied to
const comments = ref([]);
const submitting = ref(false);

const totalComments = computed(() => {
    let count = comments.value.length;
    comments.value.forEach(c => {
        if (c.children) count += c.children.length;
    });
    return count;
});

// Recursively flatten children and sort by time
const flattenChildren = (nodes, parentName) => {
    if (!nodes || nodes.length === 0) return [];

    let result = [];
    nodes.forEach(node => {
        const { children, ...nodeData } = node;

        // If replyToUser is missing, infer it from the parent structure
        if (!nodeData.replyToUser && parentName) {
            nodeData.replyToUser = parentName;
        }

        result.push(nodeData);
        if (children && children.length > 0) {
            // Pass current node's username as the parent for its children
            result = result.concat(flattenChildren(children, nodeData.username));
        }
    });

    // Sort flattened children by createTime (oldest first)
    return result.sort((a, b) => new Date(a.createTime) - new Date(b.createTime));
};

const queryCommentRequest = async () => {
    try {
        const res = await getByArticleId(props.articleId);
        if (res) {
            const oldExpandedState = {};
            comments.value.forEach(c => {
                if (c.id) oldExpandedState[c.id] = c.expanded;
            });

            comments.value = res.map(comment => ({
                ...comment,
                expanded: oldExpandedState[comment.id] !== undefined ? oldExpandedState[comment.id] : true,
                // Pass the main comment's username as the parent for direct children
                children: flattenChildren(comment.children, comment.username)
            }));
        }
    } catch (error) {
        console.error("Failed to fetch comments", error);
    }
};

const handleKeydown = (e, isMain = false) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault(); // Prevent new line
        if (isMain) {
            handleMainSubmit();
        } else {
            submitReply();
        }
    }
};

const handleMainSubmit = () => {
    submitComment(0, mainCommentContent.value);
};

const submitReply = () => {
    const targetId = activeReplyId.value;
    const content = replyContent.value;
    if (targetId) {
        submitComment(targetId, content);
    }
};

const submitComment = async (parentId, content) => {
    if (!checkLogin()) return;

    if (!content || content.trim() === '') {
        ElMessage.warning('请输入评论内容');
        return;
    }

    submitting.value = true;
    try {
        await saveReview({
            articleId: props.articleId,
            parentId: parentId,
            content: content,
        });

        ElMessage.success('发布成功');

        // Reset inputs
        if (parentId === 0) {
            mainCommentContent.value = '';
        } else {
            replyContent.value = '';
            activeReplyId.value = null;
        }

        // Refresh comments
        await queryCommentRequest();
    } catch (error) {
        console.error("Publish comment failed", error);
    } finally {
        submitting.value = false;
    }
};

const toggleExpand = (comment) => {
    comment.expanded = !comment.expanded;
};

const toggleReply = (id) => {
    if (activeReplyId.value === id) {
        activeReplyId.value = null;
        replyContent.value = '';
    } else {
        activeReplyId.value = id;
        replyContent.value = '';
    }
};

// Utility: Relative Time
const formatRelativeTime = (timeStr) => {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;

    const minute = 60 * 1000;
    const hour = 60 * minute;
    const day = 24 * hour;

    if (diff < minute) return '刚刚';
    if (diff < hour) return Math.floor(diff / minute) + '分钟前';
    if (diff < day) return Math.floor(diff / hour) + '小时前';
    if (diff < 7 * day) return Math.floor(diff / day) + '天前';

    return timeStr.split(' ')[0];
};

onMounted(() => {
    queryCommentRequest();
});
</script>

<style scoped>
.b-comment-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: #fff;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.panel-header {
    padding: 12px 16px;
    border-bottom: 1px solid #ebeef5;
    flex-shrink: 0;
}

.title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
}

.count {
    color: #909399;
    font-weight: normal;
    margin-left: 4px;
}

.input-section {
    padding: 16px;
    border-bottom: 1px solid #ebeef5;
    background-color: #fff;
    flex-shrink: 0;
}

.b-textarea :deep(.el-textarea__inner) {
    border-radius: 4px;
    padding: 8px;
    font-size: 13px;
}

.input-actions {
    margin-top: 8px;
    display: flex;
    justify-content: flex-end;
}

.comments-container {
    flex: 1;
    overflow-y: auto;
    padding: 0;
}

.comments-list {
    display: flex;
    flex-direction: column;
}

.comment-item {
    padding: 16px;
    border-bottom: 1px solid #f2f6fc;
}

.comment-item:last-child {
    border-bottom: none;
}

.comment-main {
    display: flex;
    gap: 12px;
}

.avatar-col {
    flex-shrink: 0;
}

.content-col {
    flex: 1;
    min-width: 0;
}

.info-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
}

.username {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
}

.time {
    font-size: 12px;
    color: #909399;
}

.text-row {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 8px;
    word-break: break-all;
}

.action-row {
    display: flex;
    align-items: center;
    gap: 8px;
}

.action-row :deep(.el-link) {
    font-size: 12px;
}

.inline-reply-box {
    margin-top: 8px;
    margin-bottom: 8px;
}

/* Sub Comments - Compact Style */
.sub-comments-wrapper {
    margin-top: 10px;
    background-color: #f5f7fa;
    padding: 12px;
    border-radius: 4px;
    font-size: 13px;
}

.sub-comment-item {
    margin-bottom: 8px;
    line-height: 1.5;
}

.sub-comment-item:last-child {
    margin-bottom: 0;
}

.sub-username {
    color: #303133;
    font-weight: 600;
}

.reply-target {
    color: #909399;
    margin: 0 4px;
}

.sub-content {
    color: #606266;
}

.sub-meta {
    display: inline-block;
    margin-left: 8px;
}

.sub-time {
    color: #c0c4cc;
    font-size: 12px;
    margin-right: 8px;
}

.sub-action {
    font-size: 12px;
    vertical-align: baseline;
}

.sub-reply-box {
    margin-top: 8px;
}

.empty-state {
    padding: 32px 0;
    text-align: center;
}

.empty-text {
    font-size: 13px;
    color: #909399;
}

/* Scrollbar */
.comments-container::-webkit-scrollbar {
    width: 6px;
}

.comments-container::-webkit-scrollbar-thumb {
    background: #e4e7ed;
    border-radius: 3px;
}

.comments-container::-webkit-scrollbar-track {
    background: transparent;
}
</style>
