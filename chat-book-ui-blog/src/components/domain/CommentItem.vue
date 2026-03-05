<!-- src/components/widget/CommentItem.vue -->
<template>
  <div class="comment-item">
    <div class="comment-avatar">
      <el-avatar :size="40" :src="comment.headerImg"/>
    </div>
    <div class="comment-details">
      <div class="comment-header">
        <span class="comment-username">{{ comment.username }}</span>
        <span class="comment-time">{{ comment.createTime }}</span>
      </div>
      <div class="comment-message">
        <p>{{ comment.content }}</p>
        <div class="comment-actions">
          <el-button type="text" @click="toggleComment(comment)">
            {{ comment.expanded ? '收起' : '展开' }}
          </el-button>
          <el-button type="text" @click="replyToComment(comment)">回复</el-button>
        </div>
      </div>
    </div>
    <div v-if="comment.expanded && comment.children.length > 0" class="sub-comments">
      <CommentItem
        v-for="(subComment, subIndex) in comment.children"
        :key="subIndex"
        :comment="subComment"
        @toggleComment="toggleComment"
        @replyToComment="replyToComment"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  comment: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['toggleComment', 'replyToComment']);

const toggleComment = (comment) => {
  comment.expanded = !comment.expanded;
  emit('toggleComment', comment);
};

const replyToComment = (comment) => {
  emit('replyToComment', comment);
};
</script>

<style scoped>
.comment-item {
  padding-top: 5px;
  display: flex;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: #eaeaea 1px solid;
}

.comment-avatar {
  margin-right: 10px;
}

.comment-details {
  flex-grow: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.comment-username {
  font-weight: bold;
}

.comment-time {
  font-size: 0.8em;
  color: #999;
  margin-right: 15px;
}

.comment-message {
  display: flex;
  justify-content: space-between;
  margin: 0;
}

.comment-actions {
  display: flex;
  justify-content: end;
  margin-right: 15px;
}

.sub-comments {
  margin-top: 5px;
  padding-top: 5px;
  margin-left: 20px; /* 缩进子评论 */
}
</style>
