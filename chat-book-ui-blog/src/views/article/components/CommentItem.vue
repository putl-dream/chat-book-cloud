<template>
  <div class="comment-thread-item">
    <div class="comment-thread-avatar">
      <el-avatar :size="40" :src="comment.headerImg" />
    </div>
    <div class="comment-thread-details">
      <div class="comment-thread-header">
        <span class="comment-thread-username">{{ comment.username }}</span>
        <span class="comment-thread-time">{{ comment.createTime }}</span>
      </div>
      <div class="comment-thread-message">
        <p>{{ comment.content }}</p>
        <div class="comment-thread-actions">
          <el-button type="text" @click="toggleComment(comment)">
            {{ comment.expanded ? '收起' : '展开' }}
          </el-button>
          <el-button type="text" @click="replyToComment(comment)">回复</el-button>
        </div>
      </div>
    </div>
    <div v-if="comment.expanded && comment.children.length > 0" class="comment-thread-children">
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
