<template>
  <div class="emoji-picker">
    <div v-for="(category, index) in categories" :key="index" class="category">
      <span>{{ category.name }}</span>
      <div v-for="emoji in category.emojis" :key="emoji" @click="$emit('select', emoji)">
        {{ emoji }}
      </div>
    </div>
  </div>
</template>
<script setup>
const categories = [
  {
    name: '常用',
    emojis: ['😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇']
  },
  {
    name: '动物',
    emojis: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯']
  }
]
</script>

<style scoped>
.emoji-picker {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  padding: 10px;
}

.category {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 10px;
}

.category span {
  font-weight: bold;
  margin-bottom: 5px;
  display: block;
}
</style>
