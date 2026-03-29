<template>
    <div class="panel-content b-comment-panel c-sidebar-panel">
        <div class="panel-header c-sidebar-panel__header">
            <span class="title">评论 <span class="count" v-if="totalComments > 0">({{ totalComments }})</span></span>
        </div>

        <div class="input-section c-sidebar-panel__section">
            <el-input v-model="mainCommentContent" type="textarea" :rows="3"
                placeholder="请输入您的评论... (Enter 发送，Shift+Enter 换行)" class="b-textarea" resize="none"
                @keydown="handleKeydown($event, true)" />
            <div class="input-actions">
                <el-button type="primary" size="small" :loading="submitting" @click="handleMainSubmit"
                    :disabled="!mainCommentContent.trim()">发布评论</el-button>
            </div>
        </div>

        <div class="comments-container c-sidebar-panel__body">
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

            <div v-else class="empty-state c-empty-panel c-empty-panel--plain">
                <span class="empty-text">暂无评论</span>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, toRef } from 'vue';
import { ElAvatar, ElInput, ElButton, ElEmpty, ElMessage, ElLink, ElDivider } from 'element-plus';
import { useSidebarComment } from '../_hooks/useSidebarComment.js';

const props = defineProps({
    articleId: {
        type: [Number, String],
        required: true
    }
});

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
const selfPhoto = localStorage.getItem('avatar');

const articleIdRef = toRef(props, 'articleId');

const {
    mainCommentContent,
    replyContent,
    activeReplyId,
    comments,
    submitting,
    totalComments,
    queryCommentRequest,
    handleKeydown,
    handleMainSubmit,
    submitReply,
    toggleExpand,
    toggleReply
} = useSidebarComment(articleIdRef);

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
