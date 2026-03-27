<template>
  <div v-if="total > 0" class="pagination-bar">
    <p class="text-cb-text-secondary text-sm">
      第 <strong>{{ page }}</strong> / <strong>{{ totalPages }}</strong> 页，共 <strong>{{ total }}</strong> 条
    </p>

    <div class="pagination-actions">
      <button
        :class="['pagination-button', { 'is-disabled': page <= 1 }]"
        :disabled="page <= 1"
        type="button"
        @click="$emit('change', page - 1)"
      >
        上一页
      </button>
      <button
        :class="['pagination-button', { 'is-disabled': page >= totalPages }]"
        :disabled="page >= totalPages"
        type="button"
        @click="$emit('change', page + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  page: number;
  total: number;
  totalPages: number;
}>();

defineEmits<{
  change: [page: number];
}>();
</script>
